package cn.wildfire.chat.app.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.app.SPUtils;
import cn.wildfire.chat.app.login.model.AnnouncementInfo;
import cn.wildfire.chat.app.login.model.NoticeInfo;
import cn.wildfire.chat.kit.utils.SpacesItemDecoration;
import cn.wildfirechat.chat.BuildConfig;
import cn.wildfirechat.chat.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AnnouncementActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView img_back,iv_default;
    private RecyclerView recycleView;
    private Button btn_confirm;
    private int space = 16;  //item间距
    private List<AnnouncementInfo.DataBean> list = new ArrayList<>();
    private AnnouncementAdapter announcementAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement);

        initView();
        getAnnouncementInfo();
    }

    private void initView(){
        img_back = findViewById(R.id.img_back);
        iv_default = findViewById(R.id.iv_default);
        btn_confirm = findViewById(R.id.btn_confirm);
        recycleView = findViewById(R.id.recycleView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recycleView.setLayoutManager(linearLayoutManager);
        recycleView.addItemDecoration(new SpacesItemDecoration(space));

        img_back.setOnClickListener(this);
        btn_confirm.setOnClickListener(this);
    }

    private void getAnnouncementInfo(){
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String token = sp.getString("token", "");
        String url = (String) SPUtils.get(AnnouncementActivity.this, "PREFIX", "");
        if(url.equals("") || url == null){
            return;
        }
        String language = sp.getString("language", "0");
        String url1 = url + BuildConfig.getNoticeList+"?language="+language;
        System.out.println("@@@  url1=  "+url1);
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url1)
                .get()
                .addHeader("X-Token", token)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("@@@   获取公告列表失败： " + e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                System.out.println("@@@   获取公告列表成功: " + result);
                if (result == null || result.equals("")) {
                    return;
                }
                JSONObject jsonObject = JSONObject.parseObject(result);
                String data = jsonObject.getString("data");
                if ("[]".equals(data)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv_default.setVisibility(View.VISIBLE);
                            recycleView.setVisibility(View.GONE);
                            btn_confirm.setVisibility(View.GONE);
                        }
                    });

                    return;
                }
                Gson gson = new Gson();
                AnnouncementInfo announcementInfo = gson.fromJson(result,AnnouncementInfo.class);
                if(announcementInfo.getCode() == 200){
                    list.addAll(announcementInfo.getData());
                    announcementAdapter = new AnnouncementAdapter(AnnouncementActivity.this,list);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recycleView.setAdapter(announcementAdapter);
                            announcementAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    private void upReadNotice(){
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String token = sp.getString("token", "");
        String url = (String) SPUtils.get(AnnouncementActivity.this, "PREFIX", "");
        if(url.equals("") || url == null){
            return;
        }
        String url1 = url + BuildConfig.affirmReadNotice+"?id=0";
        System.out.println("@@@  url1=  "+url1);
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url1)
                .get()
                .addHeader("X-Token", token)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("@@@   上传已读失败： " + e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                System.out.println("@@@   上传已读成功: " + result);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_confirm:
                upReadNotice();
                break;
        }
    }
}
