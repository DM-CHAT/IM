/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.app.login;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.wildfire.chat.app.AppService;
import cn.wildfire.chat.app.MyApp;
import cn.wildfire.chat.app.login.model.LoginResult;
import cn.wildfire.chat.app.main.MainActivity;
import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.remote.ChatManager;

public class LoginActivity extends WfcBaseActivity {
    @BindView(R.id.loginButton)
    Button loginButton;
    @BindView(R.id.accountEditText)
    EditText accountEditText;
    @BindView(R.id.passwordEditText)
    EditText passwordEditText;
    @BindView(R.id.ipAddr)
    EditText ipEditText;
    @BindView(R.id.resetHost)
    Button resetHostButton;
    @BindView(R.id.toRegister)
    TextView toRegister;

    @Override
    protected int contentLayout() {
        return R.layout.login_activity_account;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        boolean privateAgree = sp.getBoolean("privateAgree", false);

        String hostip = sp.getString("hostip", "");
        ipEditText.setText(hostip);

        if(!privateAgree){
            View view = View.inflate(this, R.layout.private_policy, null);
            WebView webView = view.findViewById(R.id.privateText);
            webView.loadUrl("file:///android_asset/privacy1.html");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(view);
            builder.setCancelable(false);
            builder.setPositiveButton("同意", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sp.edit().putBoolean("privateAgree", true).apply();
                    runOnUiThread(()->{checkNeedPermission();});
                }
            });
            builder.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            AlertDialog dialog = builder.create();
            WindowManager windowManager = getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            android.view.WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
            layoutParams.height = (int) (display.getHeight() * 0.8);
            layoutParams.width = (int) (display.getWidth() * 0.8);
            dialog.getWindow().setAttributes(layoutParams);
            dialog.show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkNeedPermission() {
        boolean granted = true;
        String[] permissions = {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                granted = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
                if (!granted) {
                    break;
                }
            }
        }
        if(!granted){
            requestPermissions(permissions, 100);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "需要相关权限才能正常使用", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == 100) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "授权失败", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    @Override
    protected boolean showHomeMenuItem() {
        return false;
    }

    @OnTextChanged(value = R.id.accountEditText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void inputAccount(Editable editable) {
        if (!TextUtils.isEmpty(passwordEditText.getText()) && !TextUtils.isEmpty(editable)) {
            loginButton.setEnabled(true);
        } else {
            loginButton.setEnabled(false);
        }
    }

    @OnTextChanged(value = R.id.passwordEditText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void inputPassword(Editable editable) {
        if (!TextUtils.isEmpty(accountEditText.getText()) && !TextUtils.isEmpty(editable)) {
            loginButton.setEnabled(true);
        } else {
            loginButton.setEnabled(false);
        }
    }

    @OnClick(R.id.resetHost)
    void resetHost(){
        String hostip = ipEditText.getText().toString();
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        sp.edit().putString("hostip",hostip).commit();
        ChatManager.Instance().setHost(hostip);
        Toast.makeText(this, "切换服务器成功", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.toRegister)
    void toRegister(){
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.loginButton)
    void login() {

        String account = accountEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        MaterialDialog dialog = new MaterialDialog.Builder(this)
            .content("登录中...")
            .progress(true, 10)
            .cancelable(false)
            .build();
        dialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean result = ChatManagerHolder.gChatManager.connect(account, password);
                dialog.dismiss();
                if(result){
                    String userID = ChatManagerHolder.gChatManager.getUserId();
                    SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
                    sp.edit()
                            .putString("id", userID)
                            .putString("token", userID)
                            .apply();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}
