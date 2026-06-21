package com.example.musicplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.R;

/**
 * 启动页 —— 2秒后自动跳转到登录页
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 2000; // 2秒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView tvAppName = findViewById(R.id.tv_app_name);
        // 简单动画：淡入效果
        tvAppName.setAlpha(0f);
        tvAppName.animate()
                .alpha(1f)
                .setDuration(1000)
                .start();

        // 延时跳转
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, SPLASH_DURATION);
    }
}
