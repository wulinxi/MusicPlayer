package com.example.musicplayer.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.model.Song;

import java.util.List;

/**
 * 个人中心 ViewModel
 */
public class ProfileViewModel extends AndroidViewModel {

    private final AppDatabase db;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
    }

    public LiveData<List<Song>> getFavoriteSongs(long userId) {
        return db.songDao().getFavoriteSongs(userId);
    }

    public LiveData<List<Song>> getRecentPlaySongs(long userId) {
        return db.playRecordDao().getRecentPlaySongs(userId);
    }

    public LiveData<Integer> getPlayCount(long userId) {
        return db.playRecordDao().getPlayCount(userId);
    }
}
