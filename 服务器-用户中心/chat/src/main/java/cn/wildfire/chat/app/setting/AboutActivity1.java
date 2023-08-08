package cn.wildfire.chat.app.setting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.app.SPUtils;
import cn.wildfire.chat.app.Utils;
import cn.wildfire.chat.app.login.model.AppVersionInfo;
import cn.wildfire.chat.app.main.MainActivity1;
import cn.wildfire.chat.kit.WfcBaseActivity2;
import cn.wildfirechat.chat.BuildConfig;
import cn.wildfirechat.chat.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AboutActivity1 extends WfcBaseActivity2 {

    @BindView(R.id.rl_back)
    RelativeLayout rl_back;
    @BindView(R.id.rl_checkupdate)
    RelativeLayout rl_checkupdate;
    @BindView(R.id.tv_privacyPolicy)
    TextView tv_privacyPolicy;
    @BindView(R.id.tv_uesrAgreement)
    TextView tv_uesrAgreement;

    private Dialog dialog;
    @Override
    protected int contentLayout() {
        return R.layout.activity_about1;
    }

    @Override
    protected void afterViews() {
        super.afterViews();
        yincangToolbar();
    }

    @OnClick(R.id.rl_back)
    void setRl_back(){
        finish();
    }

    @OnClick(R.id.rl_checkupdate)
    void rl_checkupdate(){
        getUpdateApk();
    }
    @OnClick(R.id.tv_privacyPolicy)
    void setTv_privacyPolicy(){
        //0中文,1越南语,2英语
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String language = sp.getString("language", "0");

        //    http://streemfinancialg.xyz/JTalking/J-user-zh.html
        //    http://streemfinancialg.xyz/JTalking/J-user-vn.html
        //     http://streemfinancialg.xyz/JTalking/J-user-en.html

        Intent intent = new Intent(AboutActivity1.this, UserAgreementActivity.class);
        if(language.equals("0")){
            intent.putExtra("url","https://Luckmoney8888.com/DM-CHAT/J-user-zh.html");
        }else if(language.equals("1")){
            intent.putExtra("url","https://Luckmoney8888.com/DM-CHAT/J-user-vn.html");
        }else if(language.equals("2")){
            intent.putExtra("url","https://Luckmoney8888.com/DM-CHAT/J-user-en.html");
        }
        startActivity(intent);
    }
    @OnClick(R.id.tv_uesrAgreement)
    void setTv_uesrAgreement(){
        //0中文,1越南语,2英语
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String language = sp.getString("language", "0");

        //   http://streemfinancialg.xyz/JTalking/J-privacy-zh.html
        //    http://streemfinancialg.xyz/JTalking/J-privacy-vn.html
        //    http://streemfinancialg.xyz/JTalking/J-privacy-en.html

        Intent intent = new Intent(AboutActivity1.this, PrivaryPolicyActivity.class);
        if(language.equals("0")){
            intent.putExtra("url","https://Luckmoney8888.com/DM-CHAT/J-privacy-zh.html");
        }else if(language.equals("1")){
            intent.putExtra("url","https://Luckmoney8888.com/DM-CHAT/J-privacy-vn.html");
        }else if(language.equals("2")){
            intent.putExtra("url","https://Luckmoney8888.com/DM-CHAT/J-privacy-en.html");
        }
        startActivity(intent);
    }


    private void getUpdateApk() {
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String token = sp.getString("token", "");

        String url =BuildConfig.AppVersion;
        System.out.println("[AboutActivity] getUpdateApk check version url:"+url);
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()
                //.addHeader("X-Token", token)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("@@@   获取更新apk失败： " + e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                System.out.println("[AboutActivity] getUpdateApk result : " + result);
                try {
                    if (result == null || result.equals("")) {
                        return;
                    }
                    Gson gson = new Gson();
                    AppVersionInfo appVersionInfo = gson.fromJson(result, AppVersionInfo.class);
                    if (appVersionInfo.getCode() == 200) {
                        int versionCode = Utils.getVersionCode(AboutActivity1.this);
                        int versionCode1 = appVersionInfo.getData().getAndroidCode();
                        if(versionCode >= versionCode1){
                            Looper.prepare();
                            Toast.makeText(AboutActivity1.this,getString(R.string.new_updated),Toast.LENGTH_SHORT).show();
                            Looper.loop();
                            return;
                        }
                        String appVersionName = Utils.getVersionName(AboutActivity1.this);
                        String androidVersion = appVersionInfo.getData().getAndroidVersion();
                        if (appVersionName.equals(androidVersion)) {
                            return;
                        }

                        View views = View.inflate(AboutActivity1.this, R.layout.dialog_show_appupdate, null);
                        Button btn_updateapp = views.findViewById(R.id.btn_updateapp);
                        TextView tv_cancle = views.findViewById(R.id.tv_cancle);
                        TextView tv_tishi = views.findViewById(R.id.tv_tishi);
                        tv_tishi.setText(getString(R.string.software_update_prompt) + " V"+appVersionInfo.getData().getAndroidVersion());
                        AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity1.this);
                        builder.setView(views);
                        builder.setCancelable(false);

                        if (appVersionInfo.getData().getUpdateVersion() == 1) {
                            tv_cancle.setVisibility(View.GONE);
                        }

                        Looper.prepare();
                        dialog = builder.create();
                        dialog.show();
                        btn_updateapp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                                final Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(appVersionInfo.getData().getApkUrl()));
                                // 注意此处的判断intent.resolveActivity()可以返回显示该Intent的Activity对应的组件名
                                // 官方解释 : Name of the component implementing an activity that can display the intent
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    final ComponentName componentName = intent.resolveActivity(getPackageManager());
                                    startActivity(Intent.createChooser(intent, "请选择浏览器"));
                                    finish();
                                } else {
                                    // GlobalMethod.showToast(context, "链接错误或无浏览器");
                                }
                            }
                        });
                        tv_cancle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        Looper.loop();
                    }else{
                        Looper.prepare();
                        Toast.makeText(AboutActivity1.this,getString(R.string.new_updated),Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }catch (Exception e){
                    return;
                }
            }
        });
    }

}
