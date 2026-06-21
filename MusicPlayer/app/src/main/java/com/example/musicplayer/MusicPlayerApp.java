package com.example.musicplayer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.model.Song;
import com.example.musicplayer.model.User;
import com.example.musicplayer.util.MusicFileHelper;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * Application 类 —— App 启动入口
 */
public class MusicPlayerApp extends Application {

    public static final String CHANNEL_ID = "music_playback";
    public static final String CHANNEL_NAME = "音乐播放";

    @Override
    public void onCreate() {
        super.onCreate();

        // 创建通知渠道
        createNotificationChannel();

        // 将 assets 中的 mp3 文件复制到内部存储
        MusicFileHelper.copyAssetsMusicToInternal(this);

        // 初始化数据库（确保有预置数据）
        initDatabase();
    }

    private void initDatabase() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            // 检查并创建测试用户（如果不存在）
            User existingUser = db.userDao().findByUsername("admin");
            if (existingUser == null) {
                User testUser = new User("admin", "123456", "音乐爱好者");
                testUser.setSignature("爱音乐，爱生活");
                db.userDao().insert(testUser);
            }

            // 强制更新预置歌曲（修复旧数据库数据）
            String defaultCover = MusicFileHelper.getDefaultCoverPath(MusicPlayerApp.this);
            List<Song> songs = db.songDao().getAllSongsSync();

            if (songs == null || songs.isEmpty()) {
                // 首次：插入歌曲
                Song s1 = new Song("孤独患者", "陈奕迅", "?");
                s1.setLocalPath("陈奕迅 - 孤独患者.mp3");
                s1.setCoverPath(defaultCover);
                Song s2 = new Song("最佳损友", "陈奕迅", "Life Continues...");
                s2.setLocalPath("陈奕迅 - 最佳损友.mp3");
                s2.setCoverPath(defaultCover);
                Song s3 = new Song("盲婚哑嫁", "陈奕迅", "The Code");
                s3.setLocalPath("陈奕迅 - 盲婚哑嫁 The Code.mp3");
                s3.setCoverPath(defaultCover);
                Song s4 = new Song("粤语残片", "陈奕迅", "What's Going On...?");
                s4.setLocalPath("陈奕迅 - 粤语残片.mp3");
                s4.setCoverPath(defaultCover);
                db.songDao().insertAll(s1, s2, s3, s4);
            } else {
                // 已有数据：修补封面和路径
                for (Song s : songs) {
                    if (s.getCoverPath() == null || s.getCoverPath().isEmpty()) {
                        s.setCoverPath(defaultCover);
                    }
                    // 修正旧的不完整文件名
                    if (s.getLocalPath() != null && !s.getLocalPath().contains(" - ") && !s.getLocalPath().startsWith("陈奕迅")) {
                        s.setLocalPath("陈奕迅 - " + s.getLocalPath());
                    }
                    db.songDao().update(s);
                }
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("音乐播放控制通知");
            channel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
