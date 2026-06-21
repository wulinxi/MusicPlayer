package com.example.musicplayer.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.musicplayer.model.User;

/**
 * 用户数据访问对象
 */
@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    /** 根据用户名查询用户（登录验证用） */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);

    /** 根据ID查询用户 */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    LiveData<User> findById(long userId);

    /** 根据ID查询用户（同步） */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User findByIdSync(long userId);

    /** 登录验证：用户名 + 密码 */
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

    /** 检查用户名是否已存在 */
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    int countByUsername(String username);
}
