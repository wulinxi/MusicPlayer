package com.example.musicplayer.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

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
import com.example.musicplayer.viewmodel.DiscoverViewModel;

/**
 * 发现音乐 Fragment —— Tab1
 */
public class DiscoverFragment extends Fragment {

    private static final String ARG_USER_ID = "user_id";
    private long userId;
    private DiscoverViewModel viewModel;
    private RecyclerView rvHotSongs;
    private SongItemAdapter hotAdapter;

    public static DiscoverFragment newInstance(long userId) {
        DiscoverFragment fragment = new DiscoverFragment();
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
        try {
            View view = inflater.inflate(R.layout.fragment_discover, container, false);
            initViews(view);
            return view;
        } catch (Exception e) {
            Log.e("DiscoverFragment", "Crash inflating layout", e);
            if (getActivity() != null) {
                Toast.makeText(getActivity(), "发现页加载失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            // 返回一个空布局兜底
            return new View(getActivity());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            viewModel = new ViewModelProvider(this).get(DiscoverViewModel.class);
            viewModel.getHotSongs().observe(getViewLifecycleOwner(), songs -> {
                if (songs != null) {
                    hotAdapter.submitList(songs);
                }
            });
        } catch (Exception e) {
            Log.e("DiscoverFragment", "Crash in onViewCreated", e);
        }
    }

    private void initViews(View view) {
        try {
            // 搜索框
            SearchView searchView = view.findViewById(R.id.search_view);
            if (searchView != null) {
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        Intent intent = new Intent(getActivity(), SongListActivity.class);
                        intent.putExtra("user_id", userId);
                        intent.putExtra("keyword", query);
                        startActivity(intent);
                        return true;
                    }
                    @Override
                    public boolean onQueryTextChange(String newText) { return false; }
                });
            }

            // 热门歌曲列表
            rvHotSongs = view.findViewById(R.id.rv_hot_songs);
            if (rvHotSongs != null) {
                rvHotSongs.setLayoutManager(new LinearLayoutManager(getContext()));
                hotAdapter = new SongItemAdapter(song -> playSong(song));
                rvHotSongs.setAdapter(hotAdapter);
            }
        } catch (Exception e) {
            Log.e("DiscoverFragment", "Crash in initViews", e);
        }
    }

    private void playSong(Song song) {
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.putExtra("song_id", song.getId());
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }
}
