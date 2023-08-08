package cn.wildfire.chat.kit.litapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.wildfire.chat.kit.BuildConfig;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.info.AddGroupApletsInfo;
import cn.wildfire.chat.kit.info.AddanApletsInfo;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
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

public class AddanApletsActivity extends Activity {

    private String token = null;
    private RelativeLayout rl_back,rl_confirm;
    private RecyclerView recyclerView;
    private List<AddanApletsInfo.DataBean.RecordsBean> list = new ArrayList<>();;
    public AddanApletsAdapter addanApletsAdapter;
    private String groupInfo_target,userID;
    private int grogramId;
    private ProgresDialog progresDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addanaplets);
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        token = sp.getString("token", null);
        Intent intent = getIntent();
        groupInfo_target = intent.getStringExtra("target");
        userID = intent.getStringExtra("userID");
        initView();
        initOnClick();
        initData(groupInfo_target);
    }

    private void initView(){
        rl_back = findViewById(R.id.rl_back);
        rl_confirm = findViewById(R.id.rl_confirm);
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
        rl_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progresDialog = new ProgresDialog(AddanApletsActivity.this);
                progresDialog.show();

                int position = addanApletsAdapter.getCheckedPosition();
                if(position != -1){
                    String str = list.get(position).getDapp_url_front();
                    JSONObject jsonObject = JSONObject.parseObject(str);
                    String target = jsonObject.getString("target");
                    grogramId = list.get(position).getId();

                    // 取签名 调用 GetOwnerSign
                    ChatManager.Instance().getOwnerSign(groupInfo_target, new GeneralCallback2() {
                        @Override
                        public void onSuccess(String result) {
                            try {
                                JSONObject jsonObject = JSONObject.parseObject(result);
                                String groupID = jsonObject.getString("groupID");
                                String groupSign = jsonObject.getString("groupSign");
                                String owner = jsonObject.getString("owner");
                                String timestamp = jsonObject.getString("timestamp");
                                // result 是证明user是群主的 license
                                /**
                                 * command: AddDapp
                                 * license: result
                                 * userId:  用户id
                                 * dappId: 小程序id
                                 * sign:
                                 *
                                 * 把数据传给url
                                 *
                                 * **/

                                JSONObject json = JSON.parseObject(result);
                                json.put("language", WfcBaseActivity.getBaseLanguage());
                                json.put("command", "AddDapp");
                                json.put("dappId", target);
                                json.put("userId", userID);
                                json.put("grogramId",grogramId);


                                String calc = "AddDapp" + userID + target + groupID;
                                String hash = ChatManager.Instance().hashData(calc.getBytes());
                                // hash 转 base64decode
                                //byte [] hashByte = android.util.Base64.decode(hash, Base64.DEFAULT);
                                String sign = ChatManager.Instance().signData(hash.getBytes());

                                json.put("sign", sign);

                                initAddGroupData("AddDapp",target,grogramId,groupID,groupSign,owner,sign,timestamp);

                                // 把json数据传过去

                                /*JSONObject json = JSON.parseObject(result);
                                json.put("count", groupInfo.memberCount);
                                json.put("language", WfcBaseActivity.getBaseLanguage());*/

                            }catch (Exception e){
                                e.printStackTrace();
                                System.out.println("@@@   e="+e);
                            }
                        }

                        @Override
                        public void onFail(int errorCode) {
                            System.out.println("@@@   errorCode="+errorCode);
                        }
                    });


                }

            }
        });
    }
    private void initData(String groupInfo_target){
        String url_GroupList = (String) SPUtils.get(AddanApletsActivity.this,"GroupList","");
        if (url_GroupList.length() < 6) {
            return;
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url_GroupList+"?groupId="+groupInfo_target)
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
                String result = response.body().string();
                System.out.println("@@@   小程序列表成功: "+result);
                if(result == null || result.equals("")){
                    return;
                }
                Gson gson = new Gson();
                AddanApletsInfo addanApletsInfo = gson.fromJson(result,AddanApletsInfo.class);
                if(addanApletsInfo.getCode() == 200){
                    list.addAll(addanApletsInfo.getData().getRecords());

                    addanApletsAdapter = new AddanApletsAdapter(AddanApletsActivity.this,list);
                    AddanApletsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.setAdapter(addanApletsAdapter);
                            addanApletsAdapter.notifyDataSetChanged();
                        }
                    });
                }else{
                    Looper.prepare();
                    Toast.makeText(AddanApletsActivity.this,addanApletsInfo.getMsg(),Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        });
    }
    private void initAddGroupData(String command,String dappId,int grogramId,String groupID,String groupSign,String owner,String sign,String timestamp){
        String url_AddGroup = (String) SPUtils.get(AddanApletsActivity.this,"AddGroup","");
        if (url_AddGroup.length() < 6) {
            return;
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject requestData = new JSONObject();
        String json = "";
        try {
            requestData.put("command",command);
            requestData.put("dappId",dappId);
            requestData.put("grogramId",grogramId);
            requestData.put("groupID",groupID);
            requestData.put("groupSign",groupSign);
            requestData.put("owner",owner);
            requestData.put("sign",sign);
            requestData.put("timestamp",timestamp);
            json = requestData.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        final Request request = new Request.Builder()
                .url(url_AddGroup)
                .addHeader("X-Token",token)
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("@@@     添加小程序失败="+e);
                progresDialog.dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                Gson gson = new Gson();
                AddGroupApletsInfo addGroupApletsInfo = gson.fromJson(result,AddGroupApletsInfo.class);
                int code = addGroupApletsInfo.getCode();
                if(code == 200){
                    AddanApletsActivity.this.finish();
                    progresDialog.dismiss();
                    Looper.prepare();
                    Toast.makeText(AddanApletsActivity.this,getString(R.string.add)+getString(R.string.image_done),Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else{
                    Looper.prepare();
                    Toast.makeText(AddanApletsActivity.this,addGroupApletsInfo.getMsg(),Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    progresDialog.dismiss();
                }
            }
        });
    }
}
