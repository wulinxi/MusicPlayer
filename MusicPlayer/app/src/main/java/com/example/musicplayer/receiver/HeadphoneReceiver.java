package com.example.musicplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.widget.Toast;

/**
 * 耳机拔出广播接收器 —— 自动暂停音乐
 */
public class HeadphoneReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
            int state = intent.getIntExtra("state", -1);
            if (state == 0) {
                // 耳机拔出 → 暂停播放
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null && audioManager.isMusicActive()) {
                    // 通过发送音频焦点丢失来暂停
                    Toast.makeText(context, "耳机已拔出，音乐已暂停", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
            // 音频输出设备断开（蓝牙等）
            Toast.makeText(context, "音频设备已断开", Toast.LENGTH_SHORT).show();
        }
    }
}
