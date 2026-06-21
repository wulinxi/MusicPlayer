package com.example.musicplayer.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.musicplayer.model.Comment;

import java.util.List;

/**
 * 评论数据访问对象
 */
@Dao
public interface CommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Comment comment);

    /** 获取某首歌的评论列表（按时间倒序） */
    @Query("SELECT * FROM comments WHERE song_id = :songId ORDER BY created_at DESC")
    LiveData<List<Comment>> getBySongId(long songId);

    /** 获取用户的所有评论 */
    @Query("SELECT * FROM comments WHERE user_id = :userId ORDER BY created_at DESC")
    LiveData<List<Comment>> getByUserId(long userId);

    /** 获取歌曲评论数 */
    @Query("SELECT COUNT(*) FROM comments WHERE song_id = :songId")
    LiveData<Integer> getCommentCount(long songId);

    /** 删除评论 */
    @Query("DELETE FROM comments WHERE id = :commentId AND user_id = :userId")
    void deleteById(long commentId, long userId);
}
