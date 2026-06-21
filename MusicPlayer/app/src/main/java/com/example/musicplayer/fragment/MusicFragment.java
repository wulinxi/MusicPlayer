package com.example.musicplayer.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.example.musicplayer.model.Song;
import com.example.musicplayer.viewmodel.MusicViewModel;

/**
 * 我的音乐 Fragment —— Tab2
 */
public class MusicFragment extends Fragment {

    private static final String ARG_USER_ID = "user_id";

    private long userId;
    private MusicViewModel viewModel;
    private SongItemAdapter allSongsAdapter, recentAdapter;

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
        // 全部歌曲按钮
        view.findViewById(R.id.btn_view_all).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SongListActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
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
}
