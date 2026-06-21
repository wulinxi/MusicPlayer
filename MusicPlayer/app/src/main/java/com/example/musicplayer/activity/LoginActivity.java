package com.example.musicplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.musicplayer.R;
import com.example.musicplayer.viewmodel.LoginViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * 登录页面
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private TextInputLayout tilUsername, tilPassword;
    private MaterialButton btnLogin, btnQQLogin;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        initViews();
        setupListeners();
        observeData();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        btnLogin = findViewById(R.id.btn_login);
        btnQQLogin = findViewById(R.id.btn_qq_login);
        findViewById(R.id.tv_go_register).setOnClickListener(v ->
            startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        btnQQLogin.setOnClickListener(v -> showQQLoginDialog());
    }

    private void attemptLogin() {
        tilUsername.setError(null);
        tilPassword.setError(null);

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            tilUsername.setError(getString(R.string.input_username));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.input_password));
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText(R.string.loading);

        viewModel.login(username, password);
    }

    /** QQ 授权登录弹窗 */
    private void showQQLoginDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_qq_login, null);
        TextInputEditText etQQ = dialogView.findViewById(R.id.et_qq_number);
        TextInputEditText etQQPwd = dialogView.findViewById(R.id.et_qq_password);

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // 用自定义按钮
        final androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();
        dialog.show();

        dialogView.findViewById(R.id.btn_qq_confirm).setOnClickListener(v -> {
            String qqNumber = etQQ.getText().toString().trim();
            String qqPwd = etQQPwd.getText().toString().trim();

            if (TextUtils.isEmpty(qqNumber)) {
                Toast.makeText(this, "请输入QQ号", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(qqPwd)) {
                Toast.makeText(this, "请输入QQ密码", Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();

            // QQ 登录：用 QQ 号作为用户名，QQ 号 + "qq" 作为密码
            btnLogin.setEnabled(false);
            btnLogin.setText(R.string.loading);
            viewModel.qqLogin(qqNumber, qqPwd);
        });
    }

    private void observeData() {
        viewModel.getLoginResult().observe(this, loginResult -> {
            btnLogin.setEnabled(true);
            btnLogin.setText(R.string.login);

            switch (loginResult.status) {
                case SUCCESS:
                    Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("user_id", loginResult.userId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    break;

                case USER_NOT_FOUND:
                    tilUsername.setError(getString(R.string.account_not_exist));
                    break;

                case PASSWORD_ERROR:
                    tilPassword.setError(getString(R.string.password_error));
                    break;

                case ERROR:
                    Toast.makeText(this, "登录失败，请重试", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}
