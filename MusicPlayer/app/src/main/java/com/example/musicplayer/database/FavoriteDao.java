package com.example.musicplayer.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.musicplayer.model.Favorite;

import java.util.List;

/**
 * 收藏数据访问对象
 */
@Dao
public interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Favorite favorite);

    @Delete
    void delete(Favorite favorite);

    /** 取消收藏（按userId + songId） */
    @Query("DELETE FROM favorites WHERE user_id = :userId AND song_id = :songId")
    void removeByUserAndSong(long userId, long songId);

    /** 检查是否已收藏 */
    @Query("SELECT COUNT(*) FROM favorites WHERE user_id = :userId AND song_id = :songId")
    LiveData<Integer> isFavorite(long userId, long songId);

    /** 检查是否已收藏（同步） */
    @Query("SELECT COUNT(*) FROM favorites WHERE user_id = :userId AND song_id = :songId")
    int isFavoriteSync(long userId, long songId);

    /** 获取用户收藏的所有记录 */
    @Query("SELECT * FROM favorites WHERE user_id = :userId ORDER BY created_at DESC")
    LiveData<List<Favorite>> getByUserId(long userId);

    /** 获取某首歌曲被收藏次数 */
    @Query("SELECT COUNT(*) FROM favorites WHERE song_id = :songId")
    LiveData<Integer> getFavoriteCount(long songId);
}
