package com.example.musicplayer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.example.musicplayer.MusicPlayerApp;
import com.example.musicplayer.R;
import com.example.musicplayer.activity.PlayerActivity;
import com.example.musicplayer.model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * 后台音乐播放 Service
 */
public class MusicService extends Service {

    private static final int NOTIFICATION_ID = 1001;

    private ExoPlayer player;
    private final IBinder binder = new MusicBinder();
    private List<Song> playlist = new ArrayList<>();
    private int currentIndex = -1;
    private PlaybackCallback callback;

    public interface PlaybackCallback {
        void onPlaybackStateChanged(boolean isPlaying);
        void onProgressChanged(long position, long duration);
        void onSongChanged(Song song);
        /** 播放失败（无可用音频源） */
        void onPlaybackError(Song song, String reason);
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = new ExoPlayer.Builder(this).build();
        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (callback != null) {
                    callback.onPlaybackStateChanged(isPlaying);
                }
                updateNotification(isPlaying);
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    playNext();
                }
            }
        });
    }

    /** 设置歌曲列表并播放指定位置 */
    public void setPlaylist(List<Song> songs, int startIndex, long userId) {
        this.playlist = songs != null ? songs : new ArrayList<>();
        this.currentIndex = Math.max(0, Math.min(startIndex, playlist.size() - 1));
        playCurrent();
    }

    /** 播放当前歌曲 */
    public void playCurrent() {
        if (playlist.isEmpty() || currentIndex < 0 || currentIndex >= playlist.size()) return;

        Song song = playlist.get(currentIndex);
        String path = song.getLocalPath();

        String mediaUri = findMediaUri(path, song.getTitle());
        if (mediaUri == null) {
            // 通知 UI 层无可用音频源
            if (callback != null) {
                callback.onPlaybackError(song, "该歌曲无可用音频源");
            }
            return;
        }

        MediaItem mediaItem = MediaItem.fromUri(mediaUri);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();

        if (callback != null) {
            callback.onSongChanged(song);
        }
    }

    /** 智能查找歌曲文件路径 */
    private String findMediaUri(String path, String title) {
        // 1. 远程URL（来自在线搜索的歌曲）
        if (path != null && !path.isEmpty() && path.startsWith("http")) {
            return path;
        }

        // 1.5 也尝试 remoteUrl 字段
        Song song = getCurrentSong();
        if (song != null && song.getRemoteUrl() != null && !song.getRemoteUrl().isEmpty() && song.getRemoteUrl().startsWith("http")) {
            return song.getRemoteUrl();
        }

        // 2. 本地文件：先尝试精确匹配
        if (path != null && !path.isEmpty()) {
            java.io.File exactFile = new java.io.File(getFilesDir(), "music/" + path);
            if (exactFile.exists()) {
                return exactFile.getAbsolutePath();
            }
            // 也尝试直接用path作为绝对路径
            java.io.File absFile = new java.io.File(path);
            if (absFile.exists()) {
                return absFile.getAbsolutePath();
            }
        }

        // 3. 模糊搜索：在music目录下找包含歌名的文件
        java.io.File musicDir = new java.io.File(getFilesDir(), "music");
        if (musicDir.exists() && musicDir.isDirectory()) {
            java.io.File[] files = musicDir.listFiles();
            if (files != null) {
                for (java.io.File f : files) {
                    if (f.getName().toLowerCase().endsWith(".mp3")
                            && f.getName().contains(title)) {
                        return f.getAbsolutePath();
                    }
                }
            }
        }

        // 4. 兜底：看看assets目录 (这个不适用因为assets不能直接给uri)
        return null;
    }

    public void play() {
        if (player != null) player.play();
    }

    public void pause() {
        if (player != null) player.pause();
    }

    public void stop() {
        if (player != null) {
            player.stop();
            player.clearMediaItems();
        }
        stopForeground(true);
        stopSelf();
    }

    public void playNext() {
        if (playlist.isEmpty()) return;
        currentIndex = (currentIndex + 1) % playlist.size();
        playCurrent();
    }

    public void playPrev() {
        if (playlist.isEmpty()) return;
        currentIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
        playCurrent();
    }

    public void seekTo(long positionMs) {
        if (player != null) player.seekTo(positionMs);
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    public long getCurrentPosition() {
        return player != null ? player.getCurrentPosition() : 0;
    }

    public long getDuration() {
        return player != null ? player.getDuration() : 0;
    }

    public Song getCurrentSong() {
        if (currentIndex >= 0 && currentIndex < playlist.size()) {
            return playlist.get(currentIndex);
        }
        return null;
    }

    public int getCurrentIndex() { return currentIndex; }
    public List<Song> getPlaylist() { return playlist; }

    public void setCallback(PlaybackCallback callback) {
        this.callback = callback;
    }

    /** 前台通知（带播放控制） */
    public void showNotification(boolean isPlaying) {
        Song song = getCurrentSong();
        if (song == null) return;

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, MusicPlayerApp.CHANNEL_ID)
                .setContentTitle(song.getTitle())
                .setContentText(song.getArtist())
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentIntent(pendingIntent)
                .setOngoing(isPlaying)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2))
                .build();

        if (isPlaying) {
            startForeground(NOTIFICATION_ID, notification);
        } else {
            stopForeground(STOP_FOREGROUND_DETACH);
            // 暂停时更新通知为非前台模式
            android.app.NotificationManager nm =
                    (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_ID, notification);
        }
    }

    private void updateNotification(boolean isPlaying) {
        showNotification(isPlaying);
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
