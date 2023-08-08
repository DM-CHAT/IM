/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.app.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.app.AppService;
import cn.wildfire.chat.app.SPUtils;
import cn.wildfire.chat.app.Utils;
import cn.wildfire.chat.app.main.MainActivity;
import cn.wildfire.chat.app.main.MainActivity1;
import cn.wildfire.chat.app.main.SplashActivity;
import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfire.chat.kit.Config;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfire.chat.kit.settings.PrivacySettingActivity;
import cn.wildfire.chat.kit.widget.OptionItemView;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback2;

public class SettingActivity extends WfcBaseActivity {
    @BindView(R.id.diagnoseOptionItemView)
    OptionItemView diagnoseOptionItemView;
    @BindView(R.id.tv_version)
    TextView tv_version;
    @BindView(R.id.rl_about)
    RelativeLayout rl_about;
    @BindView(R.id.tv_about)
    TextView tv_about;

    private UserInfo userInfo;

    @Override
    protected int contentLayout() {
        return R.layout.setting_activity;
    }

    @Override
    protected void afterViews() {
        super.afterViews();
        userInfo = getIntent().getParcelableExtra("userInfo");
        tv_about.setText(getString(R.string.abort)+"DM-CHAT");

        tv_version.setText(String.valueOf(Utils.getVersionName(SettingActivity.this)));
    }

    @OnClick(R.id.exitOptionItemView)
    void exit() {
        new android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.warning))
                .setMessage(getString(R.string.exit_confirm))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String ShowMain = (String) SPUtils.get(SettingActivity.this,"ShowMain","1");
                        ChatManager.Instance().getWorkHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                ChatManager.Instance().deleteDBConfig();
                            }
                        });

                        if (ShowMain.equals("0")) {
                            MainActivity.logout(SettingActivity.this);
                        } else {
                            MainActivity1.logout(SettingActivity.this);
                        }

                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create().show();
        //MainActivity.logout(this);
    }

    @OnClick(R.id.privacySettingOptionItemView)
    void privacySetting() {
        Intent intent = new Intent(this, PrivacySettingActivity.class);
        intent.putExtra("userInfo",userInfo);
        startActivity(intent);
    }

    @OnClick(R.id.diagnoseOptionItemView)
    void diagnose() {
    }

    @OnClick(R.id.uploadLogOptionItemView)
    void uploadLog() {
        ChatManager.Instance().uploadLogger(new GeneralCallback2() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(SettingActivity.this, result, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFail(int errorCode) {
                Toast.makeText(SettingActivity.this, "failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.themeOptionItemView)
    void theme() {
        SharedPreferences sp =SettingActivity.this.getSharedPreferences("config", Context.MODE_PRIVATE);
        boolean darkTheme = sp.getBoolean("darkTheme", true);
        new MaterialDialog.Builder(SettingActivity.this).items(R.array.themes).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View v, int position, CharSequence text) {
                if (position == 0 && darkTheme) {
                    sp.edit().putBoolean("darkTheme", false).apply();
                    restart();
                    return;
                }
                if (position == 1 && !darkTheme) {
                    sp.edit().putBoolean("darkTheme", true).apply();
                    restart();
                }
            }
        }).show();
    }

    private void restart() {
        Intent i =SettingActivity.this.getApplicationContext().getPackageManager().getLaunchIntentForPackage(SettingActivity.this.getApplicationContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    @OnClick(R.id.rl_about)
    void about() {
        Intent intent = new Intent(this, AboutActivity1.class);
        startActivity(intent);
    }

    @OnClick(R.id.spaceLogOptionItemView)
    void setSpace(){
        Intent intent = new Intent(this, SpaceActivity.class);
        startActivity(intent);
    }
    @OnClick(R.id.accountAndSecurity)
    void onSecurity(){
        Intent intent = new Intent(this, SecurityActivity.class);
        startActivity(intent);
    }
    @OnClick(R.id.languageOptionItemView)
    void onLanguage(){
        //String[] items = new String[]{getString(R.string.chinese),getString(R.string.vietnam),getString(R.string.english)};
        String[] items = new String[]{"中文","ViệtName","English","日本語","한국어","español","Das ist Deutsch","Türkçe","中文繁體","IndonesiaName"};
        AlertDialog.Builder listDialog = new AlertDialog.Builder(SettingActivity.this);
        listDialog.setTitle(getString(R.string.select_language));
        listDialog.setItems(items, (dialog, which) -> {
            String language = String.valueOf(which);
            SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
            sp.edit().putString("language", language).apply();
            WfcBaseActivity.setBaseLanguage(language);
            ChatManager.Instance().setLanguage(WfcBaseActivity.toLanguage(language));
            AlertDialog.Builder normalDialog =  new AlertDialog.Builder(SettingActivity.this);
            normalDialog.setTitle(getString(R.string.restart_app))
                .setPositiveButton(getString(R.string.confirm), (dialog1, which1) -> {
                    final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }).show();
        });
        listDialog.show();
    }
}
