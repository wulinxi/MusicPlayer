package com.example.musicplayer.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 歌曲评论表
 */
@Entity(tableName = "comments",
        foreignKeys = {
            @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Song.class, parentColumns = "id", childColumns = "song_id", onDelete = ForeignKey.CASCADE)
        },
        indices = {
            @Index(value = "song_id"),
            @Index(value = "user_id")
        })
public class Comment {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "song_id")
    private long songId;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "username")
    private String username;  // 冗余显示用的用户名

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public Comment() {
        this.createdAt = System.currentTimeMillis();
    }

    @Ignore
    public Comment(long userId, long songId, String content, String username) {
        this.userId = userId;
        this.songId = songId;
        this.content = content;
        this.username = username;
        this.createdAt = System.currentTimeMillis();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public long getSongId() { return songId; }
    public void setSongId(long songId) { this.songId = songId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
