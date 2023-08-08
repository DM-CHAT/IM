package cn.wildfire.chat.app.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import java.io.IOException;

import cn.wildfire.chat.app.login.model.AccountPasswordInfo;
import cn.wildfire.chat.app.main.MainActivity;
import cn.wildfire.chat.app.main.MainActivity1;
import cn.wildfire.chat.kit.info.DataInfo;
import cn.wildfire.chat.kit.litapp.LitappListActivity1;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.remote.ChatManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AccountPasswordActivity extends AppCompatActivity {

    private Button btn_yzm,btn_confirm;
    private TimeCount time;
    private TextView tv_zhanghao,tv_userName;
    private EditText edt_yzm,edt_new1,edt_new2;
    private String PREFIX,token;
    private ImageView img_back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_password);
        PREFIX = (String) SPUtils.get(AccountPasswordActivity.this,"PREFIX","");
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        token = sp.getString("token", null);
        initView();
        getAccount();
    }
    private void initView(){
        btn_yzm = findViewById(R.id.btn_yzm);
        btn_confirm = findViewById(R.id.btn_confirm);
        tv_zhanghao = findViewById(R.id.tv_zhanghao);
        tv_userName = findViewById(R.id.tv_userName);
        edt_yzm = findViewById(R.id.edt_yzm);
        edt_new1 = findViewById(R.id.edt_new1);
        edt_new2 = findViewById(R.id.edt_new2);
        img_back = findViewById(R.id.img_back);

        tv_zhanghao.setText("DM-CHAT"+getString(R.string.account)+":");

        time = new TimeCount(60000, 1000);
        btn_yzm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                time.start();
                setCode();
            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassWord();
            }
        });
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void setCode(){
        String zhanghao = tv_userName.getText().toString();
        if(zhanghao.equals("") || zhanghao == null){
            return;
        }else{
            OkHttpClient okHttpClient = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(PREFIX+"/login/modifyPasswordCode?phone="+zhanghao)
                    .get()
                    .addHeader("X-Token",token)
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    System.out.println("@@@   获取验证码失败： "+e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String result = response.body().string();
                    System.out.println("@@@   获取验证码成功： "+result);
                    if(result == null || result.equals("")){
                        return;
                    }
                }
            });
        }
    }
    private void changePassWord(){
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String language = sp.getString("language", "0");
        String pwd1 = edt_new1.getText().toString();
        String pwd2 = edt_new2.getText().toString();
        String code = edt_yzm.getText().toString();
        String zhanghao = tv_userName.getText().toString();

        if(pwd1.equals("") || pwd1 == null){
            Toast.makeText(AccountPasswordActivity.this,getString(R.string.password_not_empty),Toast.LENGTH_SHORT).show();
            return;
        }
        if(pwd2.equals("")|| pwd2 == null){
            Toast.makeText(AccountPasswordActivity.this,getString(R.string.password_not_empty),Toast.LENGTH_SHORT).show();
            return;
        }
        if(code.equals("") || code == null){
            Toast.makeText(AccountPasswordActivity.this,getString(R.string.code_not_empty),Toast.LENGTH_SHORT).show();
            return;
        }
        if(!pwd1.equals(pwd2)){
            Toast.makeText(AccountPasswordActivity.this,getString(R.string.password_is_different),Toast.LENGTH_SHORT).show();
            return;
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject requestData = new JSONObject();
        String json = "";
        try {
            requestData.put("code",code);
            requestData.put("language",language);
            requestData.put("password",pwd1);
            requestData.put("phone",zhanghao);

            json = requestData.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);

        final Request request = new Request.Builder()
                .url(PREFIX + "/login/modifyPassword2")
                .post(requestBody)
                .addHeader("X-Token",token)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("@@@   修改密码失败="+e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                System.out.println("@@@   修改密码成功: "+result);
                if(result == null || result.equals("")){
                    return;
                }
                Gson gson = new Gson();
                AccountPasswordInfo accountPasswordInfo = gson.fromJson(result,AccountPasswordInfo.class);
                if(accountPasswordInfo.getCode() == 200){

                    String ShowMain = (String) SPUtils.get(AccountPasswordActivity.this,"ShowMain","1");
                    ChatManager.Instance().getWorkHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            ChatManager.Instance().deleteDBConfig();
                        }
                    });
                    if (ShowMain.equals("0")) {
                        MainActivity.logout(AccountPasswordActivity.this);
                    } else {
                        MainActivity1.logout(AccountPasswordActivity.this);
                    }
                    Looper.prepare();
                    Toast.makeText(AccountPasswordActivity.this,getString(R.string.modify_success),Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else{
                    Looper.prepare();
                    Toast.makeText(AccountPasswordActivity.this,accountPasswordInfo.getMsg(),Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

            }
        });

    }

    private void getAccount(){

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(PREFIX+"/user/getAccount")
                .get()
                .addHeader("X-Token",token)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("@@@   获取账号失败： "+e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                System.out.println("@@@   获取账号成功: "+result);
                if(result == null || result.equals("")){
                    return;
                }
                Gson gson = new Gson();
                DataInfo dataInfo = gson.fromJson(result,DataInfo.class);
                if(dataInfo.getCode() == 200){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_userName.setText(dataInfo.getData());
                        }
                    });

                }
            }
        });
    }

    //验证码倒计时
    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            btn_yzm.setClickable(false);
            btn_yzm.setText(millisUntilFinished / 1000 +"");
        }

        @Override
        public void onFinish() {
            btn_yzm.setText(getString(R.string.new_get_code));
            btn_yzm.setClickable(true);

        }
    }
}
