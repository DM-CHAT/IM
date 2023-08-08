package cn.wildfire.chat.app.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.wildfire.chat.app.AppService;
//import cn.wildfire.chat.app.Config;
import cn.wildfire.chat.app.MyApp;
import cn.wildfire.chat.app.login.model.LoginResult;
import cn.wildfire.chat.app.main.MainActivity;
import cn.wildfire.chat.app.setting.SettingActivity;
import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfirechat.chat.R;
//import cn.wildfirechat.remote.ConnectedCallback;
import cn.wildfirechat.remote.GeneralCallback;

public class RegisterActivity extends WfcBaseActivity {
    @BindView(R.id.registerButton)
    Button loginButton;
    @BindView(R.id.accountEditText)
    EditText accountEditText;
    @BindView(R.id.passwordEditText)
    EditText passwordEditText;
    @BindView(R.id.toLogin)
    TextView toLogin;

    @Override
    protected int contentLayout() {
        return R.layout.register_activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @OnClick(R.id.toLogin)
    void toLogin(){
        finish();
    }

    @OnClick(R.id.registerButton)
    void register() {

        String account = accountEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content("正在注册...")
                .progress(true, 10)
                .cancelable(false)
                .build();
        dialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                OKHttpHelper.get("http://" + MyApp.HOST_IP + ":8300/serviceid", null, new SimpleCallback<String>() {
                    @Override
                    public void onSuccess1(String t) {

                    }

                    @Override
                    public void onUiSuccess(String s) {
                        JSONObject json = JSON.parseObject(s);
                        String serviceID = json.getString("serviceID");
                        if(serviceID == null){
                            dialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "service id empty", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        new Thread(()->{
                            ChatManagerHolder.gChatManager.register(account, password, serviceID, new GeneralCallback() {
                                @Override
                                public void onSuccess() {
                                    dialog.dismiss();
                                    runOnUiThread(()->{
                                        new AlertDialog.Builder(RegisterActivity.this)
                                                .setMessage("注册成功")
                                                .setCancelable(false)
                                                .setPositiveButton("确定",(DialogInterface.OnClickListener) (dialog1, which) -> {
                                                    RegisterActivity.this.finish();
                                                }).show();
                                    });
                                }

                                @Override
                                public void onFail(int errorCode) {
                                    dialog.dismiss();
                                    runOnUiThread(()-> {
                                        new AlertDialog.Builder(RegisterActivity.this).setMessage("注册失败").show();
                                    });
                                }
                            });
                        }).start();
                    }
                    @Override
                    public void onUiFailure(int code, String msg) {
                        dialog.dismiss();
                        runOnUiThread(()->{
                            new AlertDialog.Builder(RegisterActivity.this).setMessage("获取服务ID失败").show();
                        });
                    }
                });
            }
        }).start();
    }
}
