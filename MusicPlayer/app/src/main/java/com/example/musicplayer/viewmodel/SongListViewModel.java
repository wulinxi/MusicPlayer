package com.example.musicplayer.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.model.Song;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * 歌单列表 ViewModel
 */
public class SongListViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final MutableLiveData<List<Song>> songs = new MutableLiveData<>();

    public SongListViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
    }

    public LiveData<List<Song>> getSongs() { return songs; }

    public void loadAllSongs() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Song> list = db.songDao().getAllSongsSync();
            songs.postValue(list);
        });
    }

    public void searchSongs(String keyword) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Song> result = db.songDao().searchSongs(keyword).getValue();
            // 由于 searchSongs 返回 LiveData，这里直接在后台线程用同步方法
            // 我们直接用 getAllSongsSync 然后本地过滤作为简化实现
            List<Song> all = db.songDao().getAllSongsSync();
            List<Song> filtered = new java.util.ArrayList<>();
            String kw = keyword.toLowerCase();
            for (Song s : all) {
                if (s.getTitle().toLowerCase().contains(kw) ||
                        s.getArtist().toLowerCase().contains(kw)) {
                    filtered.add(s);
                }
            }
            songs.postValue(filtered);
        });
    }
}
