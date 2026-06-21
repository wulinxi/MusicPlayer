package com.example.musicplayer.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 用户收藏关联表
 * 用户和歌曲是多对多关系
 */
@Entity(tableName = "favorites",
        foreignKeys = {
            @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Song.class, parentColumns = "id", childColumns = "song_id", onDelete = ForeignKey.CASCADE)
        },
        indices = {
            @Index(value = "user_id"),
            @Index(value = "song_id"),
            @Index(value = {"user_id", "song_id"}, unique = true)
        })
public class Favorite {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "song_id")
    private long songId;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public Favorite() {
        this.createdAt = System.currentTimeMillis();
    }

    @Ignore
    public Favorite(long userId, long songId) {
        this.userId = userId;
        this.songId = songId;
        this.createdAt = System.currentTimeMillis();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public long getSongId() { return songId; }
    public void setSongId(long songId) { this.songId = songId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
