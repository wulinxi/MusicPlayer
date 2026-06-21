package com.example.musicplayer.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.musicplayer.R;
import com.example.musicplayer.viewmodel.RegisterViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * 注册页面
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etNickname, etPassword, etConfirmPassword;
    private TextInputLayout tilUsername, tilPassword, tilConfirmPassword;
    private MaterialButton btnRegister;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        initViews();
        setupListeners();
        observeData();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etNickname = findViewById(R.id.et_nickname);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        findViewById(R.id.tv_go_login).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        tilUsername.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        String username = etUsername.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            tilUsername.setError(getString(R.string.input_username));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.input_password));
            return;
        }
        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.password_not_match));
            return;
        }
        if (TextUtils.isEmpty(nickname)) {
            nickname = username; // 昵称默认用用户名
        }

        btnRegister.setEnabled(false);
        btnRegister.setText(R.string.loading);

        viewModel.register(username, password, nickname);
    }

    private void observeData() {
        viewModel.getRegisterResult().observe(this, result -> {
            btnRegister.setEnabled(true);
            btnRegister.setText(R.string.register);

            switch (result) {
                case SUCCESS:
                    Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
                    finish(); // 返回登录页
                    break;
                case USERNAME_EXISTS:
                    tilUsername.setError(getString(R.string.username_exists));
                    break;
                case ERROR:
                    Toast.makeText(this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}
