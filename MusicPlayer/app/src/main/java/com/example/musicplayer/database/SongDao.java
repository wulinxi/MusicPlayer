package com.example.musicplayer.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.musicplayer.model.Song;

import java.util.List;

/**
 * 歌曲数据访问对象
 */
@Dao
public interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Song song);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(Song... songs);

    @Update
    void update(Song song);

    @Delete
    void delete(Song song);

    /** 获取所有歌曲 */
    @Query("SELECT * FROM songs ORDER BY created_at DESC")
    LiveData<List<Song>> getAllSongs();

    /** 获取所有歌曲（同步） */
    @Query("SELECT * FROM songs ORDER BY created_at DESC")
    List<Song> getAllSongsSync();

    /** 根据ID查询歌曲 */
    @Query("SELECT * FROM songs WHERE id = :songId LIMIT 1")
    LiveData<Song> findById(long songId);

    /** 根据ID查询歌曲（同步） */
    @Query("SELECT * FROM songs WHERE id = :songId LIMIT 1")
    Song findByIdSync(long songId);

    /** 模糊搜索：歌名或歌手 */
    @Query("SELECT * FROM songs WHERE title LIKE '%' || :keyword || '%' OR artist LIKE '%' || :keyword || '%' ORDER BY created_at DESC")
    LiveData<List<Song>> searchSongs(String keyword);

    /** 获取收藏的歌曲 */
    @Query("SELECT s.* FROM songs s INNER JOIN favorites f ON s.id = f.song_id WHERE f.user_id = :userId ORDER BY f.created_at DESC")
    LiveData<List<Song>> getFavoriteSongs(long userId);

    /** 获取相似歌曲（同歌手，排除当前） */
    @Query("SELECT * FROM songs WHERE artist = :artist AND id != :excludeId LIMIT 5")
    LiveData<List<Song>> getSimilarSongs(String artist, long excludeId);

    /** 按艺术家分组获取歌曲 */
    @Query("SELECT DISTINCT artist FROM songs ORDER BY artist")
    LiveData<List<String>> getAllArtists();

    /** 更新播放次数 */
    @Query("UPDATE songs SET play_count = play_count + 1 WHERE id = :songId")
    void incrementPlayCount(long songId);

    /** 更新收藏状态 */
    @Query("UPDATE songs SET is_favorite = :favorite WHERE id = :songId")
    void updateFavoriteStatus(long songId, boolean favorite);

    /** 获取热门歌曲（按播放次数排序） */
    @Query("SELECT * FROM songs ORDER BY play_count DESC LIMIT 20")
    LiveData<List<Song>> getHotSongs();

    /** 获取最近新增歌曲 */
    @Query("SELECT * FROM songs ORDER BY created_at DESC LIMIT 20")
    LiveData<List<Song>> getRecentSongs();

    /** 删除所有无音频源的歌曲（无 remoteUrl 且无 localPath） */
    @Query("DELETE FROM songs WHERE (remote_url IS NULL OR remote_url = '') AND (local_path IS NULL OR local_path = '')")
    int deleteSongsWithoutAudio();
}
