package com.example.musicplayer.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;
import com.example.musicplayer.activity.PlayerActivity;
import com.example.musicplayer.activity.SongListActivity;
import com.example.musicplayer.adapter.SongItemAdapter;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.model.Song;
import com.example.musicplayer.util.NcmDecoder;
import com.example.musicplayer.viewmodel.MusicViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.Executors;

/**
 * 我的音乐 Fragment —— Tab2
 * 支持直接导入本地 MP3/NCM 文件
 */
public class MusicFragment extends Fragment {

    private static final String ARG_USER_ID = "user_id";

    private long userId;
    private MusicViewModel viewModel;
    private SongItemAdapter allSongsAdapter, recentAdapter;

    private final ActivityResultLauncher<String[]> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onFilePicked);

    public static MusicFragment newInstance(long userId) {
        MusicFragment fragment = new MusicFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getLong(ARG_USER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MusicViewModel.class);
        observeData();
    }

    private void initViews(View view) {
        // 查看全部
        view.findViewById(R.id.btn_view_all).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SongListActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        // 导入本地歌曲 → 直接打开文件选择器
        view.findViewById(R.id.btn_import_local).setOnClickListener(v -> {
            filePickerLauncher.launch(new String[]{"audio/*", "*/*"});
        });

        // 在线搜索添加歌曲
        view.findViewById(R.id.btn_add_song).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SongListActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("open_search", true);
            startActivity(intent);
        });

        // 全部歌曲列表
        RecyclerView rvAll = view.findViewById(R.id.rv_all_songs);
        rvAll.setLayoutManager(new LinearLayoutManager(getContext()));
        allSongsAdapter = new SongItemAdapter(song -> playSong(song));
        rvAll.setAdapter(allSongsAdapter);

        // 最近播放列表
        RecyclerView rvRecent = view.findViewById(R.id.rv_recent_play);
        rvRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        recentAdapter = new SongItemAdapter(song -> playSong(song));
        rvRecent.setAdapter(recentAdapter);
    }

    private void observeData() {
        viewModel.getAllSongs().observe(getViewLifecycleOwner(), songs -> {
            allSongsAdapter.submitList(songs);
        });
        viewModel.getRecentPlays(userId).observe(getViewLifecycleOwner(), songs -> {
            recentAdapter.submitList(songs);
        });
    }

    private void playSong(Song song) {
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.putExtra("song_id", song.getId());
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    /** 文件选择器回调 */
    private void onFilePicked(Uri uri) {
        if (uri == null || getContext() == null) return;
        String fileName = getFileName(uri);
        if (fileName == null) fileName = "unknown.mp3";
        final boolean isNcm = fileName.toLowerCase().endsWith(".ncm");
        final String fName = fileName;

        Toast.makeText(getContext(), "正在导入 " + fName + "...", Toast.LENGTH_SHORT).show();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                File musicDir = new File(getContext().getFilesDir(), "music");
                if (!musicDir.exists()) musicDir.mkdirs();

                String[] outName = new String[1];
                byte[][] coverBytes = new byte[1][];

                if (isNcm) {
                    try (InputStream in = getContext().getContentResolver().openInputStream(uri)) {
                        NcmDecoder.DecodeResult result = NcmDecoder.decode(in);
                        // 从文件名提取歌名
                        String title = fName.replace(".ncm", "").trim();
                        outName[0] = title + "." + result.format;
                        File target = new File(musicDir, outName[0]);
                        try (FileOutputStream out = new FileOutputStream(target)) {
                            out.write(result.musicData);
                        }
                        coverBytes[0] = result.coverData;
                    }
                } else {
                    outName[0] = fName;
                    File target = new File(musicDir, outName[0]);
                    try (InputStream in = getContext().getContentResolver().openInputStream(uri);
                         FileOutputStream out = new FileOutputStream(target)) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                    }
                }

                // 从文件名提取歌名和歌手（格式：歌手 - 歌名.mp3）
                String songTitle = outName[0];
                String songArtist = "未知歌手";
                String baseName = songTitle.replaceAll("\\.(mp3|flac|wav|ogg|aac)$", "");
                if (baseName.contains(" - ")) {
                    String[] parts = baseName.split(" - ", 2);
                    songArtist = parts[0].trim();
                    songTitle = parts[1].trim();
                }

                // 保存封面
                String coverPath = null;
                if (coverBytes[0] != null && coverBytes[0].length > 0) {
                    File coversDir = new File(getContext().getFilesDir(), "covers");
                    if (!coversDir.exists()) coversDir.mkdirs();
                    File coverFile = new File(coversDir, songTitle + "_cover.jpg");
                    try (FileOutputStream out = new FileOutputStream(coverFile)) {
                        out.write(coverBytes[0]);
                    }
                    coverPath = coverFile.getAbsolutePath();
                }

                // 写入数据库
                AppDatabase db = AppDatabase.getInstance(getContext());
                Song song = new Song(songTitle, songArtist, "");
                song.setLocalPath(outName[0]);
                if (coverPath != null) song.setCoverPath(coverPath);
                db.songDao().insert(song);

                final String title = songTitle;
                final String tag = isNcm ? " [NCM已解密]" : "";
                requireActivity().runOnUiThread(() -> {
                    viewModel.getAllSongs();
                    Toast.makeText(getContext(), "✅ " + title + tag, Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "导入失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private String getFileName(Uri uri) {
        if (getContext() == null) return null;
        try (Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return cursor.getString(idx);
            }
        } catch (Exception ignored) {}
        return uri.getLastPathSegment();
    }
}
