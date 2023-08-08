package cn.wildfire.chat.app.setting;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.app.SPUtils;
import cn.wildfire.chat.app.login.AccountActivity;
import cn.wildfire.chat.app.main.MainActivity;
import cn.wildfire.chat.app.main.MainActivity1;
import cn.wildfire.chat.app.main.SplashActivity;
import cn.wildfire.chat.app.utils.KeyStore;
import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.widget.OptionItemView;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.remote.ChatManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SecurityActivity extends WfcBaseActivity {

    @BindView(R.id.exitOptionItemView)
    OptionItemView exitOptionItemView;
    @BindView(R.id.accountAndSecurity)
    OptionItemView accountAndSecurity;
    @BindView(R.id.account)
    OptionItemView account;

    private String DEL_ACCOUNT_URL;
    private String token;
    private String str_account;
    private Dialog dialog;

    @Override
    protected int contentLayout() {
        return R.layout.activity_security;
    }

    @Override
    protected void afterViews() {
        DEL_ACCOUNT_URL = (String) SPUtils.get(this,"DEL_ACCOUNT_URL","");
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        token = sp.getString("token", null);
        str_account = (String) SPUtils.get(SecurityActivity.this,"account","");

        if(str_account.equals("") || str_account == null){
            exitOptionItemView.setVisibility(View.VISIBLE);
            account.setVisibility(View.GONE);
        }else{
            exitOptionItemView.setVisibility(View.GONE);
            account.setVisibility(View.VISIBLE);
        }

    }

    @OnClick(R.id.accountAndSecurity)
    void accountAndSecurity(){
        if(str_account.equals("") || str_account == null){
            Intent intent = new Intent(SecurityActivity.this,AccountPasswordActivity.class);
            startActivity(intent);
        }else{
            Intent intent = new Intent(SecurityActivity.this,AccountPasswordActivity1.class);
            startActivity(intent);
        }
    }

    @OnClick(R.id.account)
    void account(){
        View views = View.inflate(SecurityActivity.this, R.layout.dialog_keyword, null);
        Button btn_cancel = views.findViewById(R.id.btn_cancel);
        Button btn_confirm= views.findViewById(R.id.btn_confirm);
        EditText edt_keywords = views.findViewById(R.id.edt_keywords);
        AlertDialog.Builder builder = new AlertDialog.Builder(SecurityActivity.this);
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
                String pwd = edt_keywords.getText().toString();
                String account = (String) SPUtils.get(SecurityActivity.this,"account","");
                if(pwd.equals("") || pwd == null){
                    Toast.makeText(SecurityActivity.this,"密码不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                String mnemonic = KeyStore.getMnemonic(SecurityActivity.this,account,pwd);
                if(mnemonic == null){
                    Toast.makeText(SecurityActivity.this,"密码错误",Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(SecurityActivity.this, AccountActivity.class);
                intent.putExtra("mnemonic",mnemonic);
                startActivity(intent);
                dialog.dismiss();
            }
        });
    }

    @OnClick(R.id.exitOptionItemView)
    public void onLogout(){
        new android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.warning))
                .setMessage(getString(R.string.logout))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        security(DEL_ACCOUNT_URL,token);

                        ChatManagerHolder.gChatManager.disconnect(true, false);
                        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
                        sp.edit().clear().apply();

                        sp = getSharedPreferences("moment", Context.MODE_PRIVATE);
                        sp.edit().clear().apply();

                        OKHttpHelper.clearCookies();

                        Intent intent = new Intent(SecurityActivity.this, SplashActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create().show();
    }
    private void security(String url,String token){

        if (url==null){
            return;
        }
        if (url.length() < 5) {
            return;
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("X-Token",token)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("@@@    注销失败="+e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                System.out.println("@@@    注销成功="+result);

            }
        });
    }
}
