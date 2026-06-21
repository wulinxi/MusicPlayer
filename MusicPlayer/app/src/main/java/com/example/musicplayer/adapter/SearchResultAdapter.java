package com.example.musicplayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;
import com.example.musicplayer.api.OnlineSearchHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 在线搜索结果适配器
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private final List<OnlineSearchHelper.OnlineSongResult> results = new ArrayList<>();
    private final OnAddClickListener listener;

    public interface OnAddClickListener {
        void onAddClick(OnlineSearchHelper.OnlineSongResult result);
    }

    public SearchResultAdapter(OnAddClickListener listener) {
        this.listener = listener;
    }

    public void setResults(List<OnlineSearchHelper.OnlineSongResult> results) {
        this.results.clear();
        if (results != null) this.results.addAll(results);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OnlineSearchHelper.OnlineSongResult result = results.get(position);
        holder.tvTitle.setText(result.title);
        holder.tvArtist.setText(result.artist + " · " + result.album);
        holder.btnAdd.setOnClickListener(v -> listener.onAddClick(result));
    }

    @Override
    public int getItemCount() { return results.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist;
        Button btnAdd;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_result_title);
            tvArtist = itemView.findViewById(R.id.tv_result_artist);
            btnAdd = itemView.findViewById(R.id.btn_add_result);
        }
    }
}
