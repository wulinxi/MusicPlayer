package com.example.musicplayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;
import com.example.musicplayer.model.Song;

/**
 * 歌曲列表通用适配器
 */
public class SongItemAdapter extends ListAdapter<Song, SongItemAdapter.ViewHolder> {

    private final OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    public SongItemAdapter(OnSongClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Song> DIFF_CALLBACK = new DiffUtil.ItemCallback<Song>() {
        @Override
        public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
            return oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getArtist().equals(newItem.getArtist())
                    && oldItem.isFavorite() == newItem.isFavorite();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = getItem(position);
        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        holder.tvIndex.setText(String.valueOf(position + 1));

        // 无音频源时显示标记
        boolean hasAudio = (song.getRemoteUrl() != null && !song.getRemoteUrl().isEmpty())
                || (song.getLocalPath() != null && !song.getLocalPath().isEmpty());
        holder.tvNoAudio.setVisibility(hasAudio ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> listener.onSongClick(song));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIndex, tvTitle, tvArtist, tvNoAudio;

        ViewHolder(View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tv_index);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvArtist = itemView.findViewById(R.id.tv_artist);
            tvNoAudio = itemView.findViewById(R.id.tv_no_audio);
        }
    }
}
