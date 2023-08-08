package cn.wildfire.chat.kit.group.manage;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PipedReader;
import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.kit.BuildConfig;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.conversation.MessageReminderAdapter;
import cn.wildfire.chat.kit.info.AddGroupApletsInfo;
import cn.wildfire.chat.kit.litapp.AddanApletsActivity;
import cn.wildfire.chat.kit.utils.ProgresDialog;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback2;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SetKeyWordActivity extends AppCompatActivity implements SetKeyWordAdapter.SetKeyWordViewItemOnClick {

    private RecyclerView recyclerView;
    private RelativeLayout rl_back,rl_add;
    private LinearLayout ll_example;
    private String token = null;
    private String groupId = null;
    private String url = null;
    private List<KeyWordListInfo.DataBean.KeywordListBean> list = new ArrayList<>();
    private SetKeyWordAdapter setKeyWordAdapter;
    private MessageSetKeyWordAdapter messageSetKeyWordAdapter;
    private Dialog dialog;
    private ProgresDialog progresDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_key_word);
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        token = sp.getString("token", null);
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        url = (String) SPUtils.get(SetKeyWordActivity.this,"PREFIX","");

        if(progresDialog == null){
            progresDialog = new ProgresDialog(this);
        }
        initView();
        initOnClick();
        getKeyWordList();
    }

    private void initView(){
        rl_add = findViewById(R.id.rl_add);
        rl_back = findViewById(R.id.rl_back);
        ll_example = findViewById(R.id.ll_example);
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void initOnClick(){
        rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        rl_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View views = View.inflate(SetKeyWordActivity.this, R.layout.dialog_keyword1, null);
                Button btn_cancel = views.findViewById(R.id.btn_cancel);
                Button btn_confirm= views.findViewById(R.id.btn_confirm);
                EditText edt_keywords = views.findViewById(R.id.edt_keywords);
                AlertDialog.Builder builder = new AlertDialog.Builder(SetKeyWordActivity.this);
                builder.setView(views);
                builder.setCancelable(true);
                dialog = builder.create();
                dialog.show();

                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                btn_confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String content = edt_keywords.getText().toString();
                        if(!content.isEmpty()){
                            progresDialog.show();
                            addKeyWords(content);
                        }else{
                            Toast.makeText(SetKeyWordActivity.this, getString(R.string.input_empty), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    private void getKeyWordList(){
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url + BuildConfig.KEYWORD_LIST+"?groupId="+groupId)
                .get()
                .addHeader("X-Token",token)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                progresDialog.dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                System.out.println("@@@   result ="+result);
                if(result == null || result.equals("")){
                    progresDialog.dismiss();
                    return;
                }
                Gson gson = new Gson();
                KeyWordListInfo keyWordListInfo = gson.fromJson(result,KeyWordListInfo.class);

                SetKeyWordActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(keyWordListInfo.getCode() == 200){
                            int count = keyWordListInfo.getData().getKeywordList().size();
                            System.out.println("@@@   count="+count);
                            progresDialog.dismiss();
                            if(count == 0){
                                ll_example.setVisibility(View.VISIBLE);
                            }else{
                                ll_example.setVisibility(View.GONE);
                                list.clear();
                                list.addAll(keyWordListInfo.getData().getKeywordList());
                                 setKeyWordAdapter = new SetKeyWordAdapter(SetKeyWordActivity.this,list,SetKeyWordActivity.this);
                                 recyclerView.setAdapter(setKeyWordAdapter);
                                 setKeyWordAdapter.notifyDataSetChanged();

                            }
                        }else{
                            progresDialog.dismiss();
                            /*Looper.loop();
                            Toast.makeText(SetKeyWordActivity.this,keyWordListInfo.getMsg(),Toast.LENGTH_SHORT).show();
                            Looper.prepare();*/
                        }
                    }
                });

            }
        });
    }

    private void addKeyWords(String content){

        ChatManager.Instance().getOwnerSign(groupId, new GeneralCallback2() {
            @Override
            public void onSuccess(String result) {
                try {
                    // result 是证明user是群主的 license

                    OkHttpClient okHttpClient = new OkHttpClient();
                    JSONObject requestData = new JSONObject();
                    String json = "";
                    try {
                        requestData.put("content",content);
                        requestData.put("groupId",groupId);
                        requestData.put("id","");
                        requestData.put("osnId","");
                        requestData.put("timestamp","");
                        requestData.put("license",result);
                        json = requestData.toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                            , json);

                    final Request request = new Request.Builder()
                            .url(url + BuildConfig.ADD_KEYWORD)
                            .addHeader("X-Token",token)
                            .post(requestBody)
                            .build();
                    Call call = okHttpClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            progresDialog.dismiss();
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            String result = response.body().string();
                            if(result == null || result.equals("")){
                                progresDialog.dismiss();
                                return;
                            }
                            System.out.println("@@@   新增关键词="+result);
                            Gson gson = new Gson();
                            AddGroupApletsInfo addGroupApletsInfo = gson.fromJson(result,AddGroupApletsInfo.class);
                            if(addGroupApletsInfo.getCode() == 200){
                                dialog.dismiss();
                                getKeyWordList();
                            }else{
                                dialog.dismiss();
                                progresDialog.dismiss();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(SetKeyWordActivity.this,addGroupApletsInfo.getMsg(),Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }

                        }
                    });


                }catch (Exception e){
                    e.printStackTrace();
                    System.out.println("@@@   e="+e);
                }
            }

            @Override
            public void onFail(int errorCode) {
                System.out.println("@@@   errorCode="+errorCode);
                dialog.dismiss();
                progresDialog.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SetKeyWordActivity.this,getString(R.string.service_error),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void deleteKeyWord(String id,int position){

        ChatManager.Instance().getOwnerSign(groupId, new GeneralCallback2() {
            @Override
            public void onSuccess(String result) {
                OkHttpClient okHttpClient = new OkHttpClient();
                final Request request = new Request.Builder()
                        .url(url + BuildConfig.DEL_KEYWORD+"?groupId="+groupId + "&id="+id)
                        .get()
                        .addHeader("X-Token",token)
                        .build();
                Call call = okHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        progresDialog.dismiss();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String result = response.body().string();
                        if(result == null || result.equals("")){
                            progresDialog.dismiss();
                            return;
                        }
                        System.out.println("@@@   删除关键词="+result);
                        Gson gson = new Gson();
                        AddGroupApletsInfo addGroupApletsInfo = gson.fromJson(result,AddGroupApletsInfo.class);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(addGroupApletsInfo.getCode() == 200){
                                    progresDialog.dismiss();
                                    list.remove(position);
                                    setKeyWordAdapter = new SetKeyWordAdapter(SetKeyWordActivity.this,list,SetKeyWordActivity.this);
                                    recyclerView.setAdapter(setKeyWordAdapter);
                                    if(list.size() == 0){
                                        ll_example.setVisibility(View.VISIBLE);
                                    }else{
                                        ll_example.setVisibility(View.GONE);
                                    }
                                    setKeyWordAdapter.notifyDataSetChanged();
                                    //   getKeyWordList();
                                }else{
                                    progresDialog.dismiss();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(SetKeyWordActivity.this,addGroupApletsInfo.getMsg(),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });

                    }
                });
            }

            @Override
            public void onFail(int errorCode) {
                progresDialog.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SetKeyWordActivity.this,getString(R.string.service_error),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void itemOnClick(int title, int position) {
        if(title == 100){
            progresDialog = new ProgresDialog(SetKeyWordActivity.this);
            progresDialog.show();
            System.out.println("@@@    删除="+list.get(position).getId());
            deleteKeyWord(list.get(position).getId()+"",position);
        }
    }
}
