package com.example.musicplayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;

/**
 * Banner 轮播适配器（简单版：文字卡片）
 */
public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {

    private final String[] titles = {
            "🎵 用心聆听，音乐让生活更美好",
            "🔥 热门推荐：陈奕迅经典合集",
            "✨ 发现新音乐，探索无限可能"
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvBanner.setText(titles[position % titles.length]);
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE; // 无限轮播
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBanner;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBanner = itemView.findViewById(R.id.tv_banner_text);
        }
    }
}
