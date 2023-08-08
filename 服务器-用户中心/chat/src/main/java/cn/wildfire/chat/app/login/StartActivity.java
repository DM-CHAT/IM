package cn.wildfire.chat.app.login;

import static cn.wildfire.chat.app.BaseApp.getContext;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.app.login.model.StartInfo;
import cn.wildfirechat.chat.BuildConfig;
import cn.wildfirechat.chat.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<StartInfo.DataBean>list = new ArrayList<>();
    private StartAdapter startAdapter;
    private EditText edt_url;
    private RelativeLayout rl_search;
    private String old_lang;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Intent intent = getIntent();
        old_lang = intent.getStringExtra("old_lang");

        initView();
        initOnClick();
        initData();
    }

    private void initView(){
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        int space = 10;
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));

        edt_url = findViewById(R.id.edt_url);
        rl_search = findViewById(R.id.rl_search);
    }

    private void initOnClick(){
        rl_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = edt_url.getText().toString();
                if(TextUtils.isEmpty(url)){
                    Toast.makeText(StartActivity.this,"没有找到该服务器",Toast.LENGTH_SHORT).show();
                    return;
                } else{
                    if (!url.startsWith("im:")) {
                        Toast.makeText(StartActivity.this,"没有找到该服务器",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    url=url.substring(3);
                    if(!url.startsWith("http")){
                        Toast.makeText(StartActivity.this,"没有找到该服务器",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(StartActivity.this, LoginJSActivity.class);
                    Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getContext(),
                            android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                    intent.putExtra("url",url);
                    intent.putExtra("old_lang",old_lang);
                    startActivity(intent, bundle);
                }
            }
        });
    }

    private void initData(){
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(BuildConfig.Start_URL)
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                System.out.println("@@@   服务器列表="+result);
                if(result == null || result.equals("")){
                    return;
                }

                Gson gson = new Gson();
                StartInfo startInfo = gson.fromJson(result,StartInfo.class);
                if(startInfo.getCode() != 200){
                    return;
                }
                list.addAll(startInfo.getData());
                startAdapter = new StartAdapter(StartActivity.this,list);
                StartActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setAdapter(startAdapter);
                        startAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
    //recycleview 间距设置
    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.top = space;
        }
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
