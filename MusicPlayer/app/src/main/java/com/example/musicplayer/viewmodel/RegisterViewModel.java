package com.example.musicplayer.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.model.User;

import java.util.concurrent.Executors;

/**
 * 注册 ViewModel
 */
public class RegisterViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final MutableLiveData<RegisterStatus> registerResult = new MutableLiveData<>();

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
    }

    public MutableLiveData<RegisterStatus> getRegisterResult() {
        return registerResult;
    }

    public void register(String username, String password, String nickname) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 检查用户名是否存在
            int count = db.userDao().countByUsername(username);
            if (count > 0) {
                registerResult.postValue(RegisterStatus.USERNAME_EXISTS);
                return;
            }
            // 插入新用户
            User user = new User(username, password, nickname);
            long id = db.userDao().insert(user);
            if (id > 0) {
                registerResult.postValue(RegisterStatus.SUCCESS);
            } else {
                registerResult.postValue(RegisterStatus.ERROR);
            }
        });
    }

    public enum RegisterStatus {
        SUCCESS,
        USERNAME_EXISTS,
        ERROR
    }
}
