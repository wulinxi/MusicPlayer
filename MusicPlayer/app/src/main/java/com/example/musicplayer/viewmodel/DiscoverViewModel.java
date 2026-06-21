package com.example.musicplayer.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.model.Song;

import java.util.List;

/**
 * 发现页 ViewModel
 */
public class DiscoverViewModel extends AndroidViewModel {

    private final LiveData<List<Song>> hotSongs;
    private final LiveData<List<Song>> recentSongs;

    public DiscoverViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        hotSongs = db.songDao().getHotSongs();
        recentSongs = db.songDao().getRecentSongs();
    }

    public LiveData<List<Song>> getHotSongs() { return hotSongs; }
    public LiveData<List<Song>> getRecentSongs() { return recentSongs; }
}
