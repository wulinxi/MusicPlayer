package com.example.musicplayer.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.model.Song;

import java.util.List;

/**
 * 我的音乐 ViewModel
 */
public class MusicViewModel extends AndroidViewModel {

    private final LiveData<List<Song>> allSongs;
    private final AppDatabase db;

    public MusicViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        allSongs = db.songDao().getAllSongs();
    }

    public LiveData<List<Song>> getAllSongs() { return allSongs; }

    public LiveData<List<Song>> getRecentPlays(long userId) {
        return db.playRecordDao().getRecentPlaySongs(userId);
    }
}
