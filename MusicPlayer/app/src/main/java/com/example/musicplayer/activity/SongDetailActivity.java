package com.example.musicplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.CommentAdapter;
import com.example.musicplayer.adapter.SongItemAdapter;
import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.model.Comment;
import com.example.musicplayer.model.Favorite;
import com.example.musicplayer.model.Song;
import com.example.musicplayer.model.User;
import com.example.musicplayer.viewmodel.SongDetailViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.Executors;

/**
 * 歌曲详情页
 */
public class SongDetailActivity extends AppCompatActivity {

    private long userId = -1;
    private long songId = -1;
    private SongDetailViewModel viewModel;
    private boolean isFavorite = false;

    private TextView tvTitle, tvArtist, tvAlbum, tvPlayCount, tvCommentCount;
    private ImageView btnFavorite, btnBack;
    private EditText etComment;
    private MaterialButton btnSend;
    private CommentAdapter commentAdapter;
    private SongItemAdapter similarAdapter;
    private RecyclerView rvSimilar, rvComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail);

        userId = getIntent().getLongExtra("user_id", -1);
        songId = getIntent().getLongExtra("song_id", -1);

        if (songId == -1) {
            finish();
            return;
        }

        initViews();
        viewModel = new ViewModelProvider(this).get(SongDetailViewModel.class);
        observeData();
        loadSongInfo();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_detail_title);
        tvArtist = findViewById(R.id.tv_detail_artist);
        tvAlbum = findViewById(R.id.tv_album);
        tvPlayCount = findViewById(R.id.tv_play_count);
        tvCommentCount = findViewById(R.id.tv_comment_count);
        btnFavorite = findViewById(R.id.btn_detail_favorite);
        btnBack = findViewById(R.id.btn_back);
        etComment = findViewById(R.id.et_comment);
        btnSend = findViewById(R.id.btn_send_comment);

        btnBack.setOnClickListener(v -> finish());
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        // 分享
        findViewById(R.id.btn_detail_share).setOnClickListener(v -> {
            Song song = viewModel.getCurrentSong();
            if (song != null) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT,
                        "我在听《" + song.getTitle() + "》- " + song.getArtist() +
                                "，来自音乐星球App 🎵");
                startActivity(Intent.createChooser(intent, "分享到"));
            }
        });

        // 相似歌曲
        rvSimilar = findViewById(R.id.rv_similar_songs);
        rvSimilar.setLayoutManager(new LinearLayoutManager(this));
        similarAdapter = new SongItemAdapter(song -> playSong(song));
        rvSimilar.setAdapter(similarAdapter);

        // 评论
        rvComments = findViewById(R.id.rv_comments);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter();
        rvComments.setAdapter(commentAdapter);

        // 发送评论
        btnSend.setOnClickListener(v -> sendComment());
    }

    private void loadSongInfo() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Song song = db.songDao().findByIdSync(songId);
            if (song == null) return;

            int favSync = db.favoriteDao().isFavoriteSync(userId, songId);
            isFavorite = favSync > 0;

            runOnUiThread(() -> {
                tvTitle.setText(song.getTitle());
                tvArtist.setText(song.getArtist());
                tvAlbum.setText("专辑：" + (song.getAlbum() != null ? song.getAlbum() : "未知"));
                tvPlayCount.setText("播放次数：" + song.getPlayCount());
                updateFavoriteIcon();
            });
        });
    }

    private void observeData() {
        // 相似歌曲
        viewModel.getSimilarSongs(songId).observe(this, songs -> {
            similarAdapter.submitList(songs);
        });

        // 评论列表
        viewModel.getComments(songId).observe(this, comments -> {
            commentAdapter.submitList(comments);
            tvCommentCount.setText("评论：" + comments.size());
        });

        // 评论数
        viewModel.getCommentCount(songId).observe(this, count -> {
            if (count != null) {
                tvCommentCount.setText("评论：" + count);
            }
        });
    }

    private void toggleFavorite() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            if (isFavorite) {
                db.favoriteDao().removeByUserAndSong(userId, songId);
                db.songDao().updateFavoriteStatus(songId, false);
            } else {
                db.favoriteDao().insert(new Favorite(userId, songId));
                db.songDao().updateFavoriteStatus(songId, true);
            }
            isFavorite = !isFavorite;
            runOnUiThread(() -> {
                updateFavoriteIcon();
                Toast.makeText(this, isFavorite ? "已收藏" : "已取消收藏", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateFavoriteIcon() {
        btnFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
    }

    private void sendComment() {
        String content = etComment.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            User user = db.userDao().findByIdSync(userId);
            String username = user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : "匿名";

            Comment comment = new Comment(userId, songId, content, username);
            db.commentDao().insert(comment);

            runOnUiThread(() -> {
                etComment.setText("");
                Toast.makeText(this, "评论发送成功", Toast.LENGTH_SHORT).show();
                // 刷新评论列表
                viewModel.refreshComments(songId);
            });
        });
    }

    private void playSong(Song song) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("song_id", song.getId());
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }
}
