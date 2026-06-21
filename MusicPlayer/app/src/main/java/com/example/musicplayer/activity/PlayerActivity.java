package com.example.musicplayer.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.R;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.model.Favorite;
import com.example.musicplayer.model.PlayRecord;
import com.example.musicplayer.model.Song;
import com.example.musicplayer.service.MusicService;
import com.example.musicplayer.util.LyricsHelper;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * 音乐播放器页面
 */
public class PlayerActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private ImageView ivCover, btnPlayPause, btnFavorite, btnBack, btnPrev, btnNext, btnStop, btnList, btnShare;
    private TextView tvTitle, tvArtist, tvCurrentTime, tvTotalTime, tvLyrics;
    private SeekBar seekBar;

    private MusicService musicService;
    private boolean isBound = false;
    private boolean isFavorite = false;
    private long userId = -1;
    private long songId = -1;
    private boolean isUserSeeking = false;

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private ObjectAnimator coverRotation;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;

            // 设置回调
            musicService.setCallback(new MusicService.PlaybackCallback() {
                @Override
                public void onPlaybackStateChanged(boolean playing) {
                    runOnUiThread(() -> updatePlayPauseButton(playing));
                    if (playing) {
                        startCoverRotation();
                        musicService.showNotification(true);
                    } else {
                        pauseCoverRotation();
                        musicService.showNotification(false);
                    }
                }

                @Override
                public void onProgressChanged(long position, long duration) {
                    // 通过 Handler 轮询更新
                }

                @Override
                public void onSongChanged(Song song) {
                    runOnUiThread(() -> updateSongInfo(song));
                }

                @Override
                public void onPlaybackError(Song song, String reason) {
                    runOnUiThread(() -> {
                        Toast.makeText(PlayerActivity.this,
                                "无法播放：" + song.getTitle() + " — " + reason, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });

            // 准备播放
            preparePlayback();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        userId = getIntent().getLongExtra("user_id", -1);
        songId = getIntent().getLongExtra("song_id", -1);

        initViews();

        // 绑定 Service
        Intent serviceIntent = new Intent(this, MusicService.class);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void initViews() {
        ivCover = findViewById(R.id.iv_album_cover);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnBack = findViewById(R.id.btn_back);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        btnStop = findViewById(R.id.btn_stop);
        btnList = findViewById(R.id.btn_playlist);
        btnShare = findViewById(R.id.btn_share);
        tvTitle = findViewById(R.id.tv_song_title);
        tvArtist = findViewById(R.id.tv_song_artist);
        tvLyrics = findViewById(R.id.tv_lyrics);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        seekBar = findViewById(R.id.seekbar);

        seekBar.setOnSeekBarChangeListener(this);

        btnBack.setOnClickListener(v -> finish());
        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnPrev.setOnClickListener(v -> playPrev());
        btnNext.setOnClickListener(v -> playNext());
        btnStop.setOnClickListener(v -> stopMusic());
        btnList.setOnClickListener(v -> openSongList());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnShare.setOnClickListener(v -> shareSong());

        // CD 封面点击 → 歌曲详情
        ivCover.setOnClickListener(v -> {
            Song song = musicService != null ? musicService.getCurrentSong() : null;
            if (song != null) {
                Intent intent = new Intent(this, SongDetailActivity.class);
                intent.putExtra("song_id", song.getId());
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });
    }

    private void preparePlayback() {
        if (!isBound || musicService == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Song song = db.songDao().findByIdSync(songId);
            if (song == null) return;

            List<Song> allSongs = db.songDao().getAllSongsSync();
            int index = -1;
            for (int i = 0; i < allSongs.size(); i++) {
                if (allSongs.get(i).getId() == songId) {
                    index = i;
                    break;
                }
            }
            if (index == -1) index = 0;

            int finalIndex = index;
            runOnUiThread(() -> {
                musicService.setPlaylist(allSongs, finalIndex, userId);
                updateSongInfo(song);
                updatePlayPauseButton(true);
                startCoverRotation();
                startProgressUpdate();

                // 记录播放
                recordPlay(song.getId());
            });
        });
    }

    private void updateSongInfo(Song song) {
        tvTitle.setText(song.getTitle());
        tvArtist.setText(song.getArtist());
        updateFavoriteIcon();

        // 封面图：圆形裁剪
        if (song.getCoverPath() != null && !song.getCoverPath().isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(song.getCoverPath())
                    .placeholder(R.drawable.default_album)
                    .override(600, 600)
                    .circleCrop()
                    .into(ivCover);
        } else {
            String defaultCover = com.example.musicplayer.util.MusicFileHelper.getDefaultCoverPath(this);
            if (defaultCover != null) {
                com.bumptech.glide.Glide.with(this)
                        .load(defaultCover)
                        .placeholder(R.drawable.default_album)
                        .override(600, 600)
                        .circleCrop()
                        .into(ivCover);
            } else {
                ivCover.setImageResource(R.drawable.default_album);
            }
        }
        // Glide 加载后重新确保旋转（防止被重置）
        ivCover.post(() -> {
            if (coverRotation == null || !coverRotation.isRunning()) {
                startCoverRotation();
            }
        });
    }

    // ===== 播放控制 =====

    private void togglePlayPause() {
        if (!isBound || musicService == null) return;
        if (musicService.isPlaying()) {
            musicService.pause();
        } else {
            musicService.play();
        }
    }

    private void playPrev() {
        if (!isBound || musicService == null) return;
        musicService.playPrev();
        Song song = musicService.getCurrentSong();
        if (song != null) {
            updateSongInfo(song);
            recordPlay(song.getId());
        }
        updatePlayPauseButton(true);
    }

    private void playNext() {
        if (!isBound || musicService == null) return;
        musicService.playNext();
        Song song = musicService.getCurrentSong();
        if (song != null) {
            updateSongInfo(song);
            recordPlay(song.getId());
        }
        updatePlayPauseButton(true);
    }

    private void stopMusic() {
        if (!isBound || musicService == null) return;
        musicService.stop();
        stopCoverRotation();
        finish();
    }

    private void updatePlayPauseButton(boolean isPlaying) {
        btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    // ===== 封面旋转动画 =====

    private void startCoverRotation() {
        if (coverRotation != null && coverRotation.isRunning()) return;
        coverRotation = ObjectAnimator.ofFloat(ivCover, "rotation", 0f, 360f);
        coverRotation.setDuration(20000); // 20秒/圈
        coverRotation.setRepeatCount(ValueAnimator.INFINITE);
        coverRotation.setInterpolator(new LinearInterpolator());
        coverRotation.start();
    }

    private void pauseCoverRotation() {
        if (coverRotation != null && coverRotation.isRunning()) {
            coverRotation.pause();
        }
    }

    private void stopCoverRotation() {
        if (coverRotation != null) {
            coverRotation.cancel();
            coverRotation = null;
        }
    }

    // ===== 收藏 =====

    private void toggleFavorite() {
        if (!isBound || musicService == null) return;
        Song song = musicService.getCurrentSong();
        if (song == null || userId == -1) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            if (isFavorite) {
                db.favoriteDao().removeByUserAndSong(userId, song.getId());
                db.songDao().updateFavoriteStatus(song.getId(), false);
            } else {
                db.favoriteDao().insert(new Favorite(userId, song.getId()));
                db.songDao().updateFavoriteStatus(song.getId(), true);
            }
            boolean newState = !isFavorite;
            runOnUiThread(() -> {
                isFavorite = newState;
                updateFavoriteIcon();
                Toast.makeText(this, isFavorite ? "已收藏" : "已取消收藏", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateFavoriteIcon() {
        btnFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
    }

    // ===== 分享 =====

    private void shareSong() {
        Song song = musicService != null ? musicService.getCurrentSong() : null;
        if (song == null) return;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "我正在听《" + song.getTitle() + "》- " + song.getArtist() +
                        "，来自音乐星球App，一起来听吧！🎵");
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    // ===== 播放记录 =====

    private void recordPlay(long songId) {
        if (userId == -1) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.songDao().incrementPlayCount(songId);
            db.playRecordDao().insert(new PlayRecord(userId, songId));
        });
    }

    // ===== 进度条 =====

    private void startProgressUpdate() {
        progressHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isBound && musicService != null && !isUserSeeking) {
                    long pos = musicService.getCurrentPosition();
                    long dur = musicService.getDuration();
                    if (dur > 0) {
                        seekBar.setMax((int) dur);
                        seekBar.setProgress((int) pos);
                        tvCurrentTime.setText(formatTime(pos));
                        tvTotalTime.setText(formatTime(dur));
                        // 歌词
                        Song s = musicService.getCurrentSong();
                        if (s != null) {
                            String line = LyricsHelper.getLyricsLine(s.getTitle(), (int)(pos/1000));
                            tvLyrics.setText(line);
                        }
                    }
                }
                progressHandler.postDelayed(this, 500);
            }
        }, 500);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser && isBound && musicService != null) {
            musicService.seekTo(progress);
            tvCurrentTime.setText(formatTime(progress));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isUserSeeking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isUserSeeking = false;
        if (isBound && musicService != null) {
            musicService.seekTo(seekBar.getProgress());
        }
    }

    // ===== 跳转歌单 =====

    private void openSongList() {
        Intent intent = new Intent(this, SongListActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    // ===== 工具方法 =====

    private String formatTime(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCoverRotation();
        progressHandler.removeCallbacksAndMessages(null);
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }
}
