package com.example.musicplayer.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 播放记录表
 */
@Entity(tableName = "play_records",
        foreignKeys = {
            @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Song.class, parentColumns = "id", childColumns = "song_id", onDelete = ForeignKey.CASCADE)
        },
        indices = {
            @Index(value = "user_id"),
            @Index(value = "song_id")
        })
public class PlayRecord {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "song_id")
    private long songId;

    @ColumnInfo(name = "play_time")
    private long playTime;

    public PlayRecord() {
        this.playTime = System.currentTimeMillis();
    }

    @Ignore
    public PlayRecord(long userId, long songId) {
        this.userId = userId;
        this.songId = songId;
        this.playTime = System.currentTimeMillis();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public long getSongId() { return songId; }
    public void setSongId(long songId) { this.songId = songId; }

    public long getPlayTime() { return playTime; }
    public void setPlayTime(long playTime) { this.playTime = playTime; }
}
