package com.example.musicplayer.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.musicplayer.model.PlayRecord;
import com.example.musicplayer.model.Song;

import java.util.List;

/**
 * 播放记录数据访问对象
 */
@Dao
public interface PlayRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PlayRecord record);

    /** 获取用户最近播放记录（去重，取最新一次） */
    @Query("SELECT DISTINCT s.* FROM songs s INNER JOIN " +
           "(SELECT song_id, MAX(play_time) AS max_time FROM play_records " +
           "WHERE user_id = :userId GROUP BY song_id ORDER BY max_time DESC LIMIT 50) pr " +
           "ON s.id = pr.song_id ORDER BY pr.max_time DESC")
    LiveData<List<Song>> getRecentPlaySongs(long userId);

    /** 获取用户的播放记录数 */
    @Query("SELECT COUNT(DISTINCT song_id) FROM play_records WHERE user_id = :userId")
    LiveData<Integer> getPlayCount(long userId);

    /** 删除所有播放记录 */
    @Query("DELETE FROM play_records WHERE user_id = :userId")
    void deleteAllByUserId(long userId);
}
