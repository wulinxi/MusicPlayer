package com.example.musicplayer.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.model.User;

import java.util.concurrent.Executors;

/**
 * 登录 ViewModel
 */
public class LoginViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
    }

    public MutableLiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        Executors.newSingleThreadExecutor().execute(() -> {
            User user = db.userDao().findByUsername(username);
            if (user == null) {
                loginResult.postValue(new LoginResult(LoginStatus.USER_NOT_FOUND, -1));
                return;
            }
            if (!user.getPassword().equals(password)) {
                loginResult.postValue(new LoginResult(LoginStatus.PASSWORD_ERROR, -1));
                return;
            }
            loginResult.postValue(new LoginResult(LoginStatus.SUCCESS, user.getId()));
        });
    }

    /** QQ 授权登录：首次自动注册，后续直接登录 */
    public void qqLogin(String qqNumber, String qqPassword) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 查找该QQ号是否已有账号
            User user = db.userDao().findByUsername(qqNumber);
            if (user != null) {
                // 已有账号，验证密码
                if (!user.getPassword().equals(qqPassword)) {
                    loginResult.postValue(new LoginResult(LoginStatus.PASSWORD_ERROR, -1));
                    return;
                }
                loginResult.postValue(new LoginResult(LoginStatus.SUCCESS, user.getId()));
            } else {
                // 首次QQ登录，自动注册
                User newUser = new User(qqNumber, qqPassword, "QQ用户" + qqNumber);
                newUser.setAvatarPath("https://q1.qlogo.cn/g?b=qq&nk=" + qqNumber + "&s=100");
                newUser.setSignature("QQ音乐爱好者");
                long id = db.userDao().insert(newUser);
                if (id > 0) {
                    loginResult.postValue(new LoginResult(LoginStatus.SUCCESS, id));
                } else {
                    loginResult.postValue(new LoginResult(LoginStatus.ERROR, -1));
                }
            }
        });
    }

    public enum LoginStatus {
        SUCCESS,
        USER_NOT_FOUND,
        PASSWORD_ERROR,
        ERROR
    }

    public static class LoginResult {
        public final LoginStatus status;
        public final long userId;

        public LoginResult(LoginStatus status, long userId) {
            this.status = status;
            this.userId = userId;
        }
    }
}
