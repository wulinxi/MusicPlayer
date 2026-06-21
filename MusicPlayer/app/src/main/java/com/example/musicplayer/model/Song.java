package com.example.musicplayer.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * 歌曲实体类
 */
@Entity(tableName = "songs")
public class Song {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "artist")
    private String artist;

    @ColumnInfo(name = "album")
    private String album;

    /** 本地 mp3 文件路径 */
    @ColumnInfo(name = "local_path")
    private String localPath;

    /** 网络 mp3 地址 */
    @ColumnInfo(name = "remote_url")
    private String remoteUrl;

    /** 封面图片本地路径 */
    @ColumnInfo(name = "cover_path")
    private String coverPath;

    /** 封面图片网络地址 */
    @ColumnInfo(name = "cover_url")
    private String coverUrl;

    /** 歌曲时长（秒） */
    @ColumnInfo(name = "duration")
    private int duration;

    /** 是否已收藏 */
    @ColumnInfo(name = "is_favorite")
    private boolean isFavorite;

    /** 播放次数 */
    @ColumnInfo(name = "play_count")
    private int playCount;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    // ===== 构造方法 =====

    public Song() {
        this.createdAt = System.currentTimeMillis();
    }

    @Ignore
    public Song(String title, String artist, String album) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.createdAt = System.currentTimeMillis();
    }

    // ===== Getter / Setter =====

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }

    public String getRemoteUrl() { return remoteUrl; }
    public void setRemoteUrl(String remoteUrl) { this.remoteUrl = remoteUrl; }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public int getPlayCount() { return playCount; }
    public void setPlayCount(int playCount) { this.playCount = playCount; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return title + " - " + artist;
    }
}
