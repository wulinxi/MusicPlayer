package com.example.musicplayer.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.model.Comment;
import com.example.musicplayer.model.Song;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * 歌曲详情 ViewModel
 */
public class SongDetailViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private Song currentSong;

    public SongDetailViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
    }

    public void setCurrentSong(Song song) { this.currentSong = song; }
    public Song getCurrentSong() { return currentSong; }

    public LiveData<List<Song>> getSimilarSongs(long songId) {
        Song song = db.songDao().findByIdSync(songId);
        if (song != null) {
            return db.songDao().getSimilarSongs(song.getArtist(), songId);
        }
        return null;
    }

    public LiveData<List<Comment>> getComments(long songId) {
        return db.commentDao().getBySongId(songId);
    }

    public LiveData<Integer> getCommentCount(long songId) {
        return db.commentDao().getCommentCount(songId);
    }

    public void refreshComments(long songId) {
        // LiveData 自动更新，无需手动刷新
    }
}
