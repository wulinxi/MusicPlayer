package com.example.musicplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.SearchResultAdapter;
import com.example.musicplayer.adapter.SongItemAdapter;
import com.example.musicplayer.api.ApiClient;
import com.example.musicplayer.api.OnlineSearchHelper;
import com.example.musicplayer.api.SearchResponse;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.model.Song;
import com.example.musicplayer.viewmodel.SongListViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * 歌单列表页 —— 浏览/搜索/添加歌曲
 */
public class SongListActivity extends AppCompatActivity {

    private long userId = -1;
    private String keyword;
    private boolean showAdd;
    private SongListViewModel viewModel;
    private SongItemAdapter adapter;
    private RecyclerView rvSongs;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        userId = getIntent().getLongExtra("user_id", -1);
        keyword = getIntent().getStringExtra("keyword");
        showAdd = getIntent().getBooleanExtra("show_add", false);
        boolean openSearch = getIntent().getBooleanExtra("open_search", false);

        initViews();

        viewModel = new ViewModelProvider(this).get(SongListViewModel.class);
        observeData();

        if (!TextUtils.isEmpty(keyword)) {
            SearchView sv = findViewById(R.id.search_view);
            sv.setQuery(keyword, true);
        }

        if (showAdd) {
            showAddSongDialog();
        }
        if (openSearch) {
            showOnlineSearchDialog();
        }
    }

    private void initViews() {
        tvEmpty = findViewById(R.id.tv_empty);
        rvSongs = findViewById(R.id.rv_songs);
        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SongItemAdapter(song -> playSong(song));
        rvSongs.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> showOnlineSearchDialog());

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    viewModel.searchSongs(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    viewModel.loadAllSongs();
                } else {
                    viewModel.searchSongs(newText);
                }
                return true;
            }
        });
    }

    private void observeData() {
        viewModel.getSongs().observe(this, songs -> {
            adapter.submitList(songs);
            if (songs.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvSongs.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvSongs.setVisibility(View.VISIBLE);
            }
        });

        if (!TextUtils.isEmpty(keyword)) {
            viewModel.searchSongs(keyword);
        } else {
            viewModel.loadAllSongs();
        }
    }

    private void playSong(Song song) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("song_id", song.getId());
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    /** 手动添加歌曲对话框 */
    private void showAddSongDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_song, null);
        EditText etTitle = dialogView.findViewById(R.id.et_song_title);
        EditText etArtist = dialogView.findViewById(R.id.et_song_artist);
        EditText etAlbum = dialogView.findViewById(R.id.et_song_album);
        EditText etUrl = dialogView.findViewById(R.id.et_song_url);

        new MaterialAlertDialogBuilder(this)
                .setTitle("添加歌曲")
                .setView(dialogView)
                .setPositiveButton("添加", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String artist = etArtist.getText().toString().trim();
                    String album = etAlbum.getText().toString().trim();
                    String url = etUrl.getText().toString().trim();

                    if (TextUtils.isEmpty(title)) {
                        Toast.makeText(this, "请输入歌曲名", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(artist)) {
                        Toast.makeText(this, "请输入歌手名", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addSongToDb(title, artist, album, url);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /** 在线搜索对话框 —— API 优先，离线兜底 */
    private void showOnlineSearchDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_online_search, null);
        EditText etKeyword = dialogView.findViewById(R.id.et_search_keyword);
        RecyclerView rvResults = dialogView.findViewById(R.id.rv_search_results);
        ProgressBar progress = dialogView.findViewById(R.id.progress_search);

        rvResults.setLayoutManager(new LinearLayoutManager(this));

        // 点击"添加"按钮 → 仅保存元数据，不分配假音频
        SearchResultAdapter adapter = new SearchResultAdapter(result -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                Song song = new Song(result.title, result.artist, result.album);
                // 有封面URL则设置
                if (result.coverUrl != null && !result.coverUrl.isEmpty()) {
                    song.setCoverUrl(result.coverUrl);
                }
                // 不设置 remoteUrl / localPath —— 无音频源，仅保存元数据
                long id = AppDatabase.getInstance(SongListActivity.this).songDao().insert(song);
                runOnUiThread(() -> {
                    Toast.makeText(SongListActivity.this,
                            "已添加：" + result.title + "（无试听音频）", Toast.LENGTH_SHORT).show();
                    viewModel.loadAllSongs();
                });
            });
        });
        rvResults.setAdapter(adapter);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("在线搜索歌曲")
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .create();
        dialog.show();

        // 搜索按钮
        dialogView.findViewById(R.id.btn_search).setOnClickListener(v -> {
            String kw = etKeyword.getText().toString().trim();
            if (TextUtils.isEmpty(kw)) {
                Toast.makeText(this, "请输入关键词", Toast.LENGTH_SHORT).show();
                return;
            }
            progress.setVisibility(View.VISIBLE);

            // 先尝试公网 API（真机可用），失败则用本地离线库
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    ApiClient.getMusicApi().searchSongs(kw, 20).enqueue(new retrofit2.Callback<SearchResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<SearchResponse> call,
                                               retrofit2.Response<SearchResponse> response) {
                            runOnUiThread(() -> progress.setVisibility(View.GONE));
                            if (response.isSuccessful() && response.body() != null
                                    && response.body().result != null
                                    && response.body().result.songs != null
                                    && !response.body().result.songs.isEmpty()) {
                                // API 返回了真实结果
                                List<SearchResponse.Song> songs = response.body().result.songs;
                                List<OnlineSearchHelper.OnlineSongResult> results = new java.util.ArrayList<>();
                                for (SearchResponse.Song s : songs) {
                                    String cover = (s.al != null && s.al.picUrl != null) ? s.al.picUrl : "";
                                    results.add(new OnlineSearchHelper.OnlineSongResult(
                                            s.name, s.getArtistName(), s.getAlbumName(), cover));
                                }
                                runOnUiThread(() -> adapter.setResults(results));
                            } else {
                                // API 无结果，用本地库
                                fallbackToLocal(kw, adapter, progress);
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<SearchResponse> call, Throwable t) {
                            fallbackToLocal(kw, adapter, progress);
                        }
                    });
                } catch (Exception e) {
                    fallbackToLocal(kw, adapter, progress);
                }
            });
        });
    }

    /** 回退到本地离线歌曲库搜索 */
    private void fallbackToLocal(String kw, SearchResultAdapter adapter, ProgressBar progress) {
        List<OnlineSearchHelper.OnlineSongResult> results = OnlineSearchHelper.search(kw);
        runOnUiThread(() -> {
            progress.setVisibility(View.GONE);
            adapter.setResults(results);
            if (results.isEmpty()) {
                Toast.makeText(SongListActivity.this, "未找到相关歌曲", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SongListActivity.this,
                        "本地匹配到 " + results.size() + " 首歌", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 将歌曲元数据写入数据库 */
    private void addSongToDb(String title, String artist, String album, String url) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(SongListActivity.this);
            Song song = new Song(title, artist, album);
            if (!TextUtils.isEmpty(url)) {
                song.setRemoteUrl(url);
                song.setLocalPath(url); // 如果是 http 链接，MusicService 会直接播放
            }
            db.songDao().insert(song);

            runOnUiThread(() -> {
                Toast.makeText(SongListActivity.this, "歌曲添加成功", Toast.LENGTH_SHORT).show();
                viewModel.loadAllSongs();
            });
        });
    }
}
