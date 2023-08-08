package cn.wildfire.chat.app.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import java.io.IOException;

import cn.wildfire.chat.app.SPUtils;
import cn.wildfire.chat.app.Utils;
import cn.wildfire.chat.app.login.model.AnnouncementInfo;
import cn.wildfirechat.chat.BuildConfig;
import cn.wildfirechat.chat.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AnnouncementXiangQingActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_title;
    private TextView tv_time;
    private TextView tv_content;
    private RelativeLayout rl_back;
    private String title,content;
    private int id;
    private long time;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_xiangqing);
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        time = intent.getLongExtra("time",0);
        content = intent.getStringExtra("content");
        id = intent.getIntExtra("id",0);

        initView();
        upReadNotice(id);
    }

    private void initView() {
        tv_title = findViewById(R.id.tv_title);
        tv_time = findViewById(R.id.tv_time);
        tv_content = findViewById(R.id.tv_content);
        rl_back = findViewById(R.id.rl_back);

        tv_title.setText(title);
        tv_content.setMovementMethod(new ScrollingMovementMethod());
        tv_time.setText(Utils.getDateToString(time,"yyyy-MM-dd HH:mm:ss"));
        tv_content.setText(content);

        rl_back.setOnClickListener(this);
    }

    private void upReadNotice(int id){
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String token = sp.getString("token", "");
        String url = (String) SPUtils.get(AnnouncementXiangQingActivity.this, "PREFIX", "");
        if(url.equals("") || url == null){
            return;
        }
        String url1 = url + BuildConfig.affirmReadNotice+"?id="+id;
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
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_back:
                finish();
                break;
        }
    }
}
