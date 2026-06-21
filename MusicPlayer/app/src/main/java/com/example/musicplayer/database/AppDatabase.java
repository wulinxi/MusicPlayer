package com.example.musicplayer.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.musicplayer.model.Comment;
import com.example.musicplayer.model.Favorite;
import com.example.musicplayer.model.PlayRecord;
import com.example.musicplayer.model.Song;
import com.example.musicplayer.model.User;

import java.util.concurrent.Executors;

/**
 * Room 数据库类 —— 整个 App 的核心数据层
 */
@Database(
    entities = {User.class, Song.class, Favorite.class, PlayRecord.class, Comment.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "music_player.db";
    private static volatile AppDatabase INSTANCE;

    // ===== 抽象DAO方法 =====
    public abstract UserDao userDao();
    public abstract SongDao songDao();
    public abstract FavoriteDao favoriteDao();
    public abstract PlayRecordDao playRecordDao();
    public abstract CommentDao commentDao();

    // ===== 单例获取 =====
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME)
                            .addCallback(new RoomDatabaseCallback())
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // ===== 首次创建数据库时预置数据 =====
    private static class RoomDatabaseCallback extends Callback {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase database = INSTANCE;
                if (database == null) return;

                SongDao songDao = database.songDao();

                // ====== 预置4首歌曲（你的 mp3 文件） ======
                // 注意：这些mp3文件会在App首次启动时从assets/music/复制到files/music/
                // local_path 设为空字符串，运行时动态设置真实路径

                Song song1 = new Song("孤独患者", "陈奕迅", "?");
                song1.setLocalPath("陈奕迅 - 孤独患者.mp3");

                Song song2 = new Song("最佳损友", "陈奕迅", "Life Continues...");
                song2.setLocalPath("陈奕迅 - 最佳损友.mp3");

                Song song3 = new Song("盲婚哑嫁", "陈奕迅", "The Code");
                song3.setLocalPath("陈奕迅 - 盲婚哑嫁 The Code.mp3");

                Song song4 = new Song("粤语残片", "陈奕迅", "What's Going On...?");
                song4.setLocalPath("陈奕迅 - 粤语残片.mp3");

                songDao.insertAll(song1, song2, song3, song4);

                // 预置一个测试用户
                UserDao userDao = database.userDao();
                User testUser = new User("admin", "123456", "音乐爱好者");
                testUser.setSignature("爱音乐，爱生活");
                userDao.insert(testUser);
            });
        }
    }
}
