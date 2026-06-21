package com.example.musicplayer.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.musicplayer.util.NcmDecoder;
import com.example.musicplayer.viewmodel.SongListViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * 歌单列表页 —— 浏览/搜索/添加歌曲
 *
 * 添加流程：搜歌 → 点"添加" → 选本地MP3文件 → 复制到内部存储 → 可播放
 */
public class SongListActivity extends AppCompatActivity {

    private long userId = -1;
    private String keyword;
    private boolean showAdd;
    private SongListViewModel viewModel;
    private SongItemAdapter adapter;
    private RecyclerView rvSongs;
    private TextView tvEmpty;

    // 待绑定音频的歌曲（添加时暂存，文件选择器回调后更新）
    private Song pendingSong;

    // 文件选择器
    private final ActivityResultLauncher<String[]> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onFilePicked);

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

    /** 手动添加歌曲 */
    private void showAddSongDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_song, null);
        EditText etTitle = dialogView.findViewById(R.id.et_song_title);
        EditText etArtist = dialogView.findViewById(R.id.et_song_artist);
        EditText etAlbum = dialogView.findViewById(R.id.et_song_album);
        EditText etUrl = dialogView.findViewById(R.id.et_song_url);

        new MaterialAlertDialogBuilder(this)
                .setTitle("添加歌曲")
                .setView(dialogView)
                .setPositiveButton("下一步：选择MP3文件", (dialog, which) -> {
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

                    // 先存数据库，再弹文件选择器
                    pendingSong = new Song(title, artist, album);
                    if (!TextUtils.isEmpty(url)) {
                        pendingSong.setRemoteUrl(url);
                    }
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase.getInstance(this).songDao().insert(pendingSong);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "请选择 " + title + " 的MP3文件", Toast.LENGTH_LONG).show();
                            filePickerLauncher.launch(new String[]{"audio/*", "application/octet-stream", "*/*"});
                        });
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /** 在线搜索对话框 */
    private void showOnlineSearchDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_online_search, null);
        EditText etKeyword = dialogView.findViewById(R.id.et_search_keyword);
        RecyclerView rvResults = dialogView.findViewById(R.id.rv_search_results);
        ProgressBar progress = dialogView.findViewById(R.id.progress_search);

        rvResults.setLayoutManager(new LinearLayoutManager(this));

        // 点击"添加" → 存元数据 → 弹文件选择器
        SearchResultAdapter adapter = new SearchResultAdapter(result -> {
            Song song = new Song(result.title, result.artist, result.album);
            if (result.coverUrl != null && !result.coverUrl.isEmpty()) {
                song.setCoverUrl(result.coverUrl);
            }

            pendingSong = song;
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(SongListActivity.this).songDao().insert(pendingSong);
                runOnUiThread(() -> {
                    Toast.makeText(SongListActivity.this,
                            "请选择 " + result.title + " 的MP3文件", Toast.LENGTH_LONG).show();
                    filePickerLauncher.launch(new String[]{"audio/*", "application/octet-stream", "*/*"});
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

            // 直接使用本地库（快速、始终可用、160+首歌曲）
            Executors.newSingleThreadExecutor().execute(() -> {
                List<OnlineSearchHelper.OnlineSongResult> results = OnlineSearchHelper.search(kw);
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    adapter.setResults(results);
                    Toast.makeText(SongListActivity.this,
                            results.isEmpty() ? "未找到相关歌曲" : "找到 " + results.size() + " 首歌",
                            Toast.LENGTH_SHORT).show();
                });
            });
        });
    }

    /** 文件选择器回调：复制MP3 或 解密NCM */
    private void onFilePicked(Uri uri) {
        if (uri == null || pendingSong == null) return;

        String fileName = getFileName(uri);
        final boolean isNcm = fileName != null && fileName.toLowerCase().endsWith(".ncm");
        final String fName = fileName;

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                File musicDir = new File(getFilesDir(), "music");
                if (!musicDir.exists()) musicDir.mkdirs();

                final String[] outFileName = new String[1];
                final byte[][] coverBytes = new byte[1][];

                if (isNcm) {
                    // === 解密 .ncm → .mp3/flac ===
                    try (InputStream in = getContentResolver().openInputStream(uri)) {
                        NcmDecoder.DecodeResult result = NcmDecoder.decode(in);
                        outFileName[0] = pendingSong.getTitle() + " - " + pendingSong.getArtist() + "." + result.format;
                        File targetFile = new File(musicDir, outFileName[0]);
                        try (FileOutputStream out = new FileOutputStream(targetFile)) {
                            out.write(result.musicData);
                        }
                        coverBytes[0] = result.coverData;
                    }
                } else {
                    // === 直接复制音频文件 ===
                    String name = (fName != null) ? fName
                            : pendingSong.getTitle() + " - " + pendingSong.getArtist() + ".mp3";
                    outFileName[0] = name;
                    File targetFile = new File(musicDir, outFileName[0]);
                    try (InputStream in = getContentResolver().openInputStream(uri);
                         FileOutputStream out = new FileOutputStream(targetFile)) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                    }
                }

                // NCM 内嵌封面
                if (coverBytes[0] != null && coverBytes[0].length > 0) {
                    File coversDir = new File(getFilesDir(), "covers");
                    if (!coversDir.exists()) coversDir.mkdirs();
                    String coverName = pendingSong.getTitle() + "_cover.jpg";
                    try (FileOutputStream out = new FileOutputStream(new File(coversDir, coverName))) {
                        out.write(coverBytes[0]);
                    }
                    pendingSong.setCoverPath(new File(coversDir, coverName).getAbsolutePath());
                }

                // 更新数据库
                AppDatabase db = AppDatabase.getInstance(SongListActivity.this);
                pendingSong.setLocalPath(outFileName[0]);
                pendingSong.setRemoteUrl(null);
                db.songDao().update(pendingSong);

                String tag = isNcm ? " [NCM已解密]" : "";
                String msg = "✅ " + pendingSong.getTitle() + tag + " 可以播放！";
                runOnUiThread(() -> {
                    Toast.makeText(SongListActivity.this, msg, Toast.LENGTH_SHORT).show();
                    viewModel.loadAllSongs();
                    pendingSong = null;
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(SongListActivity.this,
                            (isNcm ? "NCM解密" : "导入") + "失败: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /** 从 Uri 获取文件名 */
    private String getFileName(Uri uri) {
        String name = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) name = cursor.getString(idx);
            }
        } catch (Exception ignored) {}
        if (name == null) {
            name = uri.getLastPathSegment();
        }
        return name;
    }

    /** 将歌曲元数据写入数据库（不弹文件选择器时使用） */
    private void addSongToDb(String title, String artist, String album, String url) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(SongListActivity.this);
            Song song = new Song(title, artist, album);
            if (!TextUtils.isEmpty(url)) {
                song.setRemoteUrl(url);
                song.setLocalPath(url);
            }
            db.songDao().insert(song);

            runOnUiThread(() -> {
                Toast.makeText(SongListActivity.this, "歌曲添加成功", Toast.LENGTH_SHORT).show();
                viewModel.loadAllSongs();
            });
        });
    }
}
