package cn.wildfire.chat.kit.contact.newfriend;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.group.GroupInfoActivity;
import cn.wildfire.chat.kit.info.AddanApletsInfo;
import cn.wildfire.chat.kit.litapp.AddanApletsActivity;
import cn.wildfire.chat.kit.litapp.AddanApletsAdapter;
import cn.wildfire.chat.kit.litapp.LitappInfoActivity;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GetGroupInfoCallback;
import cn.wildfirechat.remote.GetLitappInfoCallback;
import cn.wildfirechat.remote.GetUserInfoCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddOsnIDActivity extends WfcBaseActivity {
    @BindView(R2.id.content)
    EditText content;

    @Override
    protected int contentLayout() {
        return R.layout.activity_add_osnid;
    }
    @OnClick(R2.id.addTarget)
    protected void onClick(View view) {
        String osnID = content.getText().toString().trim();
        if(osnID.isEmpty()){
            Toast.makeText(AddOsnIDActivity.this,getString(R.string.input_empty),Toast.LENGTH_SHORT).show();
            return;
        }
        if(osnID.startsWith("OSN")){
            if(osnID.startsWith("OSNU")){
                UserInfo userInfo = new UserInfo(osnID);
                Intent intent = new Intent(AddOsnIDActivity.this, UserInfoActivity.class);
                intent.putExtra("userInfo", userInfo);
                startActivity(intent);
                finish();

                /*ChatManager.Instance().getUserInfo(osnID, false, new GetUserInfoCallback() {
                    @Override
                    public void onSuccess(UserInfo userInfo) {
                        Intent intent = new Intent(AddOsnIDActivity.this, UserInfoActivity.class);
                        intent.putExtra("userInfo", userInfo);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFail(int errorCode) {
                        Toast.makeText(AddOsnIDActivity.this, getString(R.string.add_failure), Toast.LENGTH_SHORT).show();
                    }
                });*/
            }
            else if(osnID.startsWith("OSNG")){
                Intent intent = new Intent(AddOsnIDActivity.this, GroupInfoActivity.class);
                intent.putExtra("groupId", osnID);
                intent.putExtra("refresh", true);
                startActivity(intent);
                finish();
                /*ChatManager.Instance().getGroupInfo(osnID, false, new GetGroupInfoCallback() {
                    @Override
                    public void onSuccess(GroupInfo groupInfo) {
                        Intent intent = new Intent(AddOsnIDActivity.this, GroupInfoActivity.class);
                        intent.putExtra("groupId", groupInfo.target);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFail(int errorCode) {
                        Toast.makeText(AddOsnIDActivity.this, getString(R.string.add_failure), Toast.LENGTH_SHORT).show();
                    }
                });*/
            }
            else if(osnID.startsWith("OSNS")){
                ChatManager.Instance().getLitappInfoEx(osnID, false, new GetLitappInfoCallback() {
                    @Override
                    public void onSuccess(LitappInfo litappInfo) {
                        Intent intent = new Intent(AddOsnIDActivity.this, LitappInfoActivity.class);
                        intent.putExtra("litappId", litappInfo.target);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFail(int errorCode) {
                        Toast.makeText(AddOsnIDActivity.this, getString(R.string.add_failure), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                Toast.makeText(this, getString(R.string.error_osnid), Toast.LENGTH_SHORT).show();
            }
        }
        else{
            //从服务器获取osnID
            SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
            String token = sp.getString("token", null);
            String PREFIX = (String) SPUtils.get(AddOsnIDActivity.this,"PREFIX",null);
            if(PREFIX == null){
                Toast.makeText(AddOsnIDActivity.this,getString(R.string.server_parameter_error),Toast.LENGTH_SHORT).show();
                return;
            }
            OkHttpClient okHttpClient = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(PREFIX+"/im/getOsnId"+"?phone="+osnID)
                    .get()
                    .addHeader("X-Token",token)
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    System.out.println("@@@   获取osnid失败： "+e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String result = response.body().string();
                    System.out.println("@@@   获取osnid成功: "+result);
                    if(result.isEmpty()){
                        AddOsnIDActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddOsnIDActivity.this,getString(R.string.server_parameter_error),Toast.LENGTH_SHORT).show();
                                return;
                            }
                        });
                    }
                    Gson gson = new Gson();
                    AddOsnIDInfo addOsnIDInfo = gson.fromJson(result,AddOsnIDInfo.class);
                    if(addOsnIDInfo.getCode() == 200){
                        String osnID = addOsnIDInfo.getData().getOsn_id();
                        AddOsnIDActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(osnID.startsWith("OSNU")){

                                    UserInfo userInfo = new UserInfo(osnID);
                                    Intent intent = new Intent(AddOsnIDActivity.this, UserInfoActivity.class);
                                    intent.putExtra("userInfo", userInfo);
                                    startActivity(intent);
                                    finish();

                                    /*ChatManager.Instance().getUserInfo(osnID, false, new GetUserInfoCallback() {
                                        @Override
                                        public void onSuccess(UserInfo userInfo) {
                                            Intent intent = new Intent(AddOsnIDActivity.this, UserInfoActivity.class);
                                            intent.putExtra("userInfo", userInfo);
                                            startActivity(intent);
                                            finish();
                                        }

                                        @Override
                                        public void onFail(int errorCode) {
                                            Toast.makeText(AddOsnIDActivity.this, getString(R.string.add_failure), Toast.LENGTH_SHORT).show();
                                        }
                                    });*/
                                }
                                else if(osnID.startsWith("OSNG")){
                                    Intent intent = new Intent(AddOsnIDActivity.this, GroupInfoActivity.class);
                                    intent.putExtra("groupId", osnID);
                                    startActivity(intent);
                                    finish();
                                    /*ChatManager.Instance().getGroupInfo(osnID, false, new GetGroupInfoCallback() {
                                        @Override
                                        public void onSuccess(GroupInfo groupInfo) {
                                            Intent intent = new Intent(AddOsnIDActivity.this, GroupInfoActivity.class);
                                            intent.putExtra("groupId", groupInfo.target);
                                            startActivity(intent);
                                            finish();
                                        }

                                        @Override
                                        public void onFail(int errorCode) {
                                            Toast.makeText(AddOsnIDActivity.this, getString(R.string.add_failure), Toast.LENGTH_SHORT).show();
                                        }
                                    });*/
                                }
                                else if(osnID.startsWith("OSNS")){
                                    ChatManager.Instance().getLitappInfoEx(osnID, false, new GetLitappInfoCallback() {
                                        @Override
                                        public void onSuccess(LitappInfo litappInfo) {
                                            Intent intent = new Intent(AddOsnIDActivity.this, LitappInfoActivity.class);
                                            intent.putExtra("litappId", litappInfo.target);
                                            startActivity(intent);
                                            finish();
                                        }

                                        @Override
                                        public void onFail(int errorCode) {
                                            Toast.makeText(AddOsnIDActivity.this, getString(R.string.add_failure), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                else{
                                    Toast.makeText(AddOsnIDActivity.this, getString(R.string.error_osnid), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }else{
                        AddOsnIDActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddOsnIDActivity.this,addOsnIDInfo.getMsg(),Toast.LENGTH_SHORT).show();
                                return;
                            }
                        });
                    }
                }
            });
        }

    }
}
