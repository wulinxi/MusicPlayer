package com.example.musicplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
 * 歌单列表页
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

        // 如果有搜索关键词
        if (!TextUtils.isEmpty(keyword)) {
            SearchView sv = findViewById(R.id.search_view);
            sv.setQuery(keyword, true);
        }

        // 如果点了添加按钮
        if (showAdd) {
            showAddSongDialog();
        }
        // 直接打开在线搜索
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

        // 初始加载
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

    /**
     * 添加歌曲对话框
     */
    private void showAddSongDialog() {
        // 弹出表单：输入歌名、歌手、专辑、远程URL（可选）
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

                    addSong(title, artist, album, url);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /** 在线搜索对话框 —— 接入真实 API */
    private void showOnlineSearchDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_online_search, null);
        EditText etKeyword = dialogView.findViewById(R.id.et_search_keyword);
        RecyclerView rvResults = dialogView.findViewById(R.id.rv_search_results);
        ProgressBar progress = dialogView.findViewById(R.id.progress_search);

        rvResults.setLayoutManager(new LinearLayoutManager(this));
        SearchResultAdapter adapter = new SearchResultAdapter(result -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                Song song = new Song(result.title, result.artist, result.album);
                // 为每个在线歌曲分配不同 demo 音源（SoundHelix）
                String[] demoUrls = {
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3"
                };
                String demoUrl = demoUrls[(int)(Math.random() * demoUrls.length)];
                song.setRemoteUrl(demoUrl);
                song.setLocalPath(demoUrl);
                if (result.coverUrl != null && !result.coverUrl.isEmpty()) song.setCoverUrl(result.coverUrl);
                AppDatabase.getInstance(this).songDao().insert(song);
                runOnUiThread(() -> {
                    Toast.makeText(this, "已添加：" + result.title, Toast.LENGTH_SHORT).show();
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

        dialogView.findViewById(R.id.btn_search).setOnClickListener(v -> {
            String kw = etKeyword.getText().toString().trim();
            if (TextUtils.isEmpty(kw)) {
                Toast.makeText(this, "请输入关键词", Toast.LENGTH_SHORT).show();
                return;
            }
            progress.setVisibility(View.VISIBLE);

            // 优先使用真实 API，失败则用本地模拟
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    ApiClient.getMusicApi().searchSongs(kw, 20).enqueue(new retrofit2.Callback<SearchResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<SearchResponse> call, retrofit2.Response<SearchResponse> response) {
                            runOnUiThread(() -> progress.setVisibility(View.GONE));
                            if (response.isSuccessful() && response.body() != null && response.body().result != null) {
                                List<SearchResponse.Song> songs = response.body().result.songs;
                                List<OnlineSearchHelper.OnlineSongResult> results = new java.util.ArrayList<>();
                                for (SearchResponse.Song s : songs) {
                                    OnlineSearchHelper.OnlineSongResult r = new OnlineSearchHelper.OnlineSongResult(
                                        s.name, s.getArtistName(), s.getAlbumName(),
                                        s.al != null && s.al.picUrl != null ? s.al.picUrl : ""
                                    );
                                    results.add(r);
                                }
                                runOnUiThread(() -> adapter.setResults(results));
                            } else {
                                // API 失败，用本地模拟数据
                                List<OnlineSearchHelper.OnlineSongResult> results = OnlineSearchHelper.search(kw);
                                runOnUiThread(() -> {
                                    adapter.setResults(results);
                                    Toast.makeText(SongListActivity.this, "API 离线，使用本地数据", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<SearchResponse> call, Throwable t) {
                            runOnUiThread(() -> progress.setVisibility(View.GONE));
                            // 网络失败，用本地模拟
                            List<OnlineSearchHelper.OnlineSongResult> results = OnlineSearchHelper.search(kw);
                            runOnUiThread(() -> {
                                adapter.setResults(results);
                                Toast.makeText(SongListActivity.this, "服务器未启动，使用本地数据", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> progress.setVisibility(View.GONE));
                    List<OnlineSearchHelper.OnlineSongResult> results = OnlineSearchHelper.search(kw);
                    runOnUiThread(() -> adapter.setResults(results));
                }
            });
        });
    }

    private void addSong(String title, String artist, String album, String url) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Song song = new Song(title, artist, album);
            if (!TextUtils.isEmpty(url)) {
                song.setRemoteUrl(url);
            }
            song.setLocalPath(null); // 没有本地文件
            db.songDao().insert(song);

            runOnUiThread(() -> {
                Toast.makeText(this, "歌曲添加成功", Toast.LENGTH_SHORT).show();
                viewModel.loadAllSongs(); // 刷新列表
            });
        });
    }
}
