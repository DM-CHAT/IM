/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.app.main;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.NotificationCompat;

import butterknife.ButterKnife;
import cn.wildfire.chat.app.MyApp;
import cn.wildfire.chat.app.SPUtils;
import cn.wildfire.chat.app.login.LoginJSActivity;
import cn.wildfire.chat.app.login.StartActivity;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.remote.ChatManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static cn.wildfire.chat.app.BaseApp.getContext;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SplashActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_DRAW_OVERLAY = 101;

    private SharedPreferences sharedPreferences;
    private String id;
    private String token;
    private boolean enable1 = false;
    private ComponentName defaultComponent;
    private ComponentName icon2Component;

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        hideStatusBar();
        /*defaultComponent =new ComponentName(this,"cn.wildfire.chat.app.main.SplashActivity");
        icon2Component = new ComponentName(this,"cn.wildfire.chat.app.main.NewSplashActivity");
        updateAlias(false, defaultComponent);
        updateAlias(true, icon2Component);*/

        sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);
        id = sharedPreferences.getString("id", null);
        token = sharedPreferences.getString("token", null);

        showNextScreen();
        try {
            getBombRole();
        } catch (Exception e) {

        }
    }

    private void showNextScreen() {

        if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(token)) {
            new Thread(()->{ChatManager.Instance().connect(id, token);}).start();

            showMain();
        } else {
            Resources resources = getResources();
            Configuration config = resources.getConfiguration();
            String old_lang = config.getLocales().toLanguageTags();

            /*Intent intent = new Intent(SplashActivity.this, StartActivity.class);
            intent.putExtra("old_lang",old_lang);
            startActivity(intent);
            finish();*/
            //   showLogin();

            Intent intent = new Intent(SplashActivity.this, LoginJSActivity.class);
            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getContext(),
                    android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
            intent.putExtra("url","im:http://openspn.com/apk/login.html");
            intent.putExtra("old_lang",old_lang);
            startActivity(intent, bundle);
            finish();
        }
    }
    private void getBombRole(){
        String url_BombRole = (String) SPUtils.get(SplashActivity.this,"BombRole","");
        if(url_BombRole.equals("")){
            return;
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url_BombRole)
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String result = response.body().string();
                    if(TextUtils.isEmpty(result)){
                        return;
                    }
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    if(jsonObject == null){
                        return;
                    }
                    int code = jsonObject.getIntValue("code");
                    if(code == 200){
                        String data = jsonObject.getString("data");
                        if(data != null) {
                            if (data.equals("1")) {
                                SPUtils.put(SplashActivity.this,"enable1",true);
                                ChatManager.Instance().setHideEnable1("1");
                                return;
                            }
                        }
                    }
                    SPUtils.put(SplashActivity.this,"enable1", false);
                }catch (Exception e){

                }

            }

        });
    }

    private void showMain() {
        String showMain = (String) SPUtils.get(SplashActivity.this,"ShowMain","1");
        Intent intent = null;
        if(showMain.equals("0")){
            intent = new Intent(this, MainActivity.class);
        }else{
            intent = new Intent(this, MainActivity1.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getContext(),
                android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(intent, bundle);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showLogin() {

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        String old_lang = config.getLocales().toLanguageTags();

        Intent intent;
        //intent = new Intent(this, LoginActivity.class);
        intent = new Intent(this, LoginJSActivity.class);
        intent.putExtra("old_lang",old_lang);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getContext(),
                android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(intent, bundle);
        finish();
    }



    /**
     * 更新别名显示
     * @param componentName componentName
     * @param enable 是否启用
     */
    private void updateAlias(Boolean enable, ComponentName componentName) {
        int newState;
        if (enable){
            newState = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        }else {
            newState = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        }
        getPackageManager().setComponentEnabledSetting(componentName, newState, PackageManager.DONT_KILL_APP);
    }
}
