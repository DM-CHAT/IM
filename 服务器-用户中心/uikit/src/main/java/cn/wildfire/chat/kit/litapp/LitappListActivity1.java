package cn.wildfire.chat.kit.litapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.contact.ContactListActivity;
import cn.wildfire.chat.kit.info.LitappListInfo;
import cn.wildfire.chat.kit.third.utils.UIUtils;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfire.chat.kit.utils.SpacesItemDecoration;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.remote.ChatManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LitappListActivity1 extends AppCompatActivity {

    private CommonAdapter<LitappListInfo.DataBean> commonAdapter;
    private List<LitappListInfo.DataBean> list;
    private RelativeLayout rl_back;
    private RecyclerView recyclerView;
    private int space = 26;  //item间距
    private boolean pick;
    private ImageView iv_default;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_litapplist1);
        pick = getIntent().getBooleanExtra("pick", false);
        initView();
    }
    private void initView(){
        rl_back = findViewById(R.id.rl_back);
        iv_default = findViewById(R.id.iv_default);
        rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));

        String PREFIX = (String) SPUtils.get(LitappListActivity1.this,"PREFIX","");
        if (PREFIX.equalsIgnoreCase("")){
            return;
        }
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String token = sp.getString("token", null);

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(PREFIX+"/im/programList")
                .get()
                .addHeader("X-Token",token)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("@@@   小程序列表失败： "+e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                try {


                    String result = response.body().string();
                    System.out.println("@@@   小程序列表成功: "+result);

                    if(result == null || result.equals("")){
                        iv_default.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        return;
                    }

                    Gson gson = new Gson();
                    LitappListInfo litappListInfo;
                    try {
                        litappListInfo = gson.fromJson(result,LitappListInfo.class);
                    }catch (Exception e){
                        iv_default.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        return;
                    }

                    if(litappListInfo.getCode() == 200){
                        list = new ArrayList<>();
                        list.addAll(litappListInfo.getData());
                        System.out.println("@@@   list="+list.size());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(list.size()>0){
                                    iv_default.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                }else{
                                    iv_default.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                }
                            }
                        });

                        commonAdapter = new CommonAdapter<LitappListInfo.DataBean>(LitappListActivity1.this,R.layout.adapter_litapplist1,list) {
                            @Override
                            protected void convert(ViewHolder holder, LitappListInfo.DataBean dataBean, int position) {
                                String data_json = list.get(position).getData_json();

                                JSONObject jsonObject = JSONObject.parseObject(data_json);
                                //   LitappInfo litappInfo = new LitappInfo(jsonObject);
                                LitappInfo litappInfo;
                                try {
                                    litappInfo = new LitappInfo(jsonObject);
                                }catch (Exception e){
                                    System.out.println("@@@   小程序返回数据格式错误");
                                    return;
                                }
                                // LitappInfo  litappInfo = gson.fromJson(data_json,LitappInfo.class);

                                holder.setText(R.id.tv_name,litappInfo.name);

                                ImageView iv_title = holder.getView(R.id.iv_title);
                                RequestOptions requestOptions = new RequestOptions()
                                        .placeholder(R.mipmap.avatar_def)
                                        .transforms(new CenterCrop(), new RoundedCorners(UIUtils.dip2Px(LitappListActivity1.this, 10)));
                                Glide.with(LitappListActivity1.this)
                                        .load(litappInfo.portrait)
                                        .apply(requestOptions)
                                        .into(iv_title);

                                RelativeLayout rl_item = holder.getView(R.id.rl_item);
                                rl_item.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (pick) {
                                            Intent intent = new Intent();
                                            intent.putExtra("litappInfo", litappInfo);
                                            setResult(Activity.RESULT_OK, intent);
                                            finish();
                                        } else {

                                            boolean isCollect = false;
                                            LitappInfo litappInfo1 = ChatManager.Instance().getCollectLitapp(litappInfo.target);
                                            if(litappInfo1 != null){
                                                isCollect = true;
                                            }
                                            Intent intent = new Intent(LitappListActivity1.this,LitappActivity.class);
                                            intent.putExtra("litappInfo", litappInfo);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.putExtra("isCollect",isCollect);
                                            startActivity(intent);
                                        }
                                    }
                                });
                                rl_item.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View view) {

                                        List<String> titles = new ArrayList<>();
                                        titles.add(getString(R.string.forward));
                                        titles.add(getString(R.string.delete));

                                        new MaterialDialog.Builder(LitappListActivity1.this).items(titles).itemsCallback((dialog, v, position1, text) -> {
                                            switch (position1){
                                                case 0:
                                                    Intent intent = new Intent(LitappListActivity1.this, ContactListActivity.class);
                                                    intent.putExtra("litappInfo", litappInfo);
                                                    intent.putExtra("pick",true);
                                                    startActivity(intent);
                                                    break;
                                                case 1:
                                                    //删除
                                                    String PREFIX = (String) SPUtils.get(LitappListActivity1.this,"PREFIX","");
                                                    if (PREFIX.length() < 6) {
                                                        return;
                                                    }
                                                    SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
                                                    String token = sp.getString("token", null);
                                                    System.out.println("@@@    getId()= "+list.get(position).getId());

                                                    OkHttpClient okHttpClient = new OkHttpClient();
                                                    final Request request = new Request.Builder()
                                                            .url(PREFIX+"/im/delProgram?id="+list.get(position).getId())
                                                            .get()
                                                            .addHeader("X-Token",token)
                                                            .build();
                                                    Call call = okHttpClient.newCall(request);
                                                    call.enqueue(new Callback() {
                                                        @Override
                                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                            System.out.println("@@@   删除小程序列表失败： "+e);
                                                        }

                                                        @Override
                                                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                            String result = response.body().string();
                                                            System.out.println("@@@   删除小程序列表成功: "+result);
                                                            if(result == null || result.equals("")){
                                                                return;
                                                            }
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    list.remove(position);
                                                                    commonAdapter.notifyDataSetChanged();
                                                                }
                                                            });
                                                        }
                                                    });
                                                    break;
                                            }
                                        }).show();
                                        return false;
                                    }
                                });
                            }
                        };

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.setAdapter(commonAdapter);
                            }
                        });

                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                iv_default.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                        });
                    }
                } catch (Exception e) {

                }
            }
        });


    }
}
