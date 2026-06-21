package com.example.musicplayer.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicplayer.R;
import com.example.musicplayer.activity.LoginActivity;
import com.example.musicplayer.activity.PlayerActivity;
import com.example.musicplayer.adapter.SongItemAdapter;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.model.Song;
import com.example.musicplayer.model.User;
import com.example.musicplayer.viewmodel.ProfileViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.concurrent.Executors;

/**
 * 个人中心 Fragment —— Tab3
 */
public class ProfileFragment extends Fragment {

    private static final String ARG_USER_ID = "user_id";

    private long userId;
    private ProfileViewModel viewModel;
    private TextView tvNickname, tvSignature, tvFavoriteCount, tvPlayCount;
    private ImageView ivAvatar;
    private SongItemAdapter favoriteAdapter, recentAdapter;

    public static ProfileFragment newInstance(long userId) {
        ProfileFragment fragment = new ProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        loadUserInfo();
        observeData();
    }

    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvNickname = view.findViewById(R.id.tv_nickname);
        tvSignature = view.findViewById(R.id.tv_signature);
        tvFavoriteCount = view.findViewById(R.id.tv_favorite_count);
        tvPlayCount = view.findViewById(R.id.tv_play_count);

        view.findViewById(R.id.btn_edit_profile).setOnClickListener(v -> showEditDialog());

        RecyclerView rvFavorites = view.findViewById(R.id.rv_favorites);
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        favoriteAdapter = new SongItemAdapter(song -> playSong(song));
        rvFavorites.setAdapter(favoriteAdapter);

        RecyclerView rvRecent = view.findViewById(R.id.rv_recent_full);
        rvRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        recentAdapter = new SongItemAdapter(song -> playSong(song));
        rvRecent.setAdapter(recentAdapter);

        view.findViewById(R.id.btn_clear_cache).setOnClickListener(v ->
            android.widget.Toast.makeText(getContext(), "缓存已清除", android.widget.Toast.LENGTH_SHORT).show()
        );

        view.findViewById(R.id.btn_logout).setOnClickListener(v ->
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("退出登录")
                    .setMessage("确定要退出登录吗？")
                    .setPositiveButton(R.string.confirm, (d, w) -> {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show()
        );
    }

    private void loadUserInfo() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            User user = db.userDao().findByIdSync(userId);
            if (user != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvNickname.setText(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    tvSignature.setText(user.getSignature() != null ? user.getSignature() : "这个人很懒，什么都没写");

                    if (user.getAvatarPath() != null && !user.getAvatarPath().isEmpty()) {
                        Glide.with(ProfileFragment.this)
                                .load(user.getAvatarPath())
                                .placeholder(R.drawable.bg_circle_primary)
                                .circleCrop()
                                .into(ivAvatar);
                    }
                });
            }
        });
    }

    private void observeData() {
        viewModel.getFavoriteSongs(userId).observe(getViewLifecycleOwner(), songs -> {
            favoriteAdapter.submitList(songs);
            tvFavoriteCount.setText(String.valueOf(songs.size()));
        });
        viewModel.getRecentPlaySongs(userId).observe(getViewLifecycleOwner(), songs -> {
            recentAdapter.submitList(songs);
        });
        viewModel.getPlayCount(userId).observe(getViewLifecycleOwner(), count -> {
            tvPlayCount.setText(String.valueOf(count != null ? count : 0));
        });
    }

    private void showEditDialog() {
        android.widget.EditText input = new android.widget.EditText(getContext());
        input.setText(tvNickname.getText());
        input.setHint("输入新昵称");
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.edit_profile)
                .setView(input)
                .setPositiveButton(R.string.confirm, (d, w) -> {
                    String newNick = input.getText().toString().trim();
                    if (!newNick.isEmpty()) {
                        tvNickname.setText(newNick);
                        updateNickname(newNick);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateNickname(String newNickname) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            User user = db.userDao().findByIdSync(userId);
            if (user != null) {
                user.setNickname(newNickname);
                db.userDao().update(user);
            }
        });
    }

    private void playSong(Song song) {
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.putExtra("song_id", song.getId());
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }
}
