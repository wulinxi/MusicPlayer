package com.example.musicplayer.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.musicplayer.R;
import com.example.musicplayer.fragment.DiscoverFragment;
import com.example.musicplayer.fragment.MusicFragment;
import com.example.musicplayer.fragment.ProfileFragment;
import com.example.musicplayer.util.MusicFileHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * 主界面 —— 底部导航栏管理三个Tab
 */
public class MainActivity extends AppCompatActivity {

    private long userId = -1;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);

            // 加载背景图
            ImageView ivBg = findViewById(R.id.iv_main_bg);
            String bgPath = MusicFileHelper.getMainBgPath(this);
            if (bgPath != null) {
                Glide.with(this).load(bgPath).centerCrop().into(ivBg);
            }

            userId = getIntent().getLongExtra("user_id", -1);
            Log.d("MainActivity", "userId = " + userId);

            bottomNav = findViewById(R.id.bottom_navigation);

            if (savedInstanceState == null) {
                loadFragment(DiscoverFragment.newInstance(userId));
            }

            bottomNav.setOnItemSelectedListener(item -> {
                Fragment fragment = null;
                int id = item.getItemId();

                if (id == R.id.nav_discover) {
                    fragment = DiscoverFragment.newInstance(userId);
                } else if (id == R.id.nav_my_music) {
                    fragment = MusicFragment.newInstance(userId);
                } else if (id == R.id.nav_profile) {
                    fragment = ProfileFragment.newInstance(userId);
                }

                if (fragment != null) {
                    loadFragment(fragment);
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Crash in onCreate", e);
            Toast.makeText(this, "加载失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadFragment(Fragment fragment) {
        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } catch (Exception e) {
            Log.e("MainActivity", "Crash loading fragment", e);
            Toast.makeText(this, "Fragment错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
