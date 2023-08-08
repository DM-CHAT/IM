package cn.wildfire.chat.app.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;

import cn.wildfire.chat.app.utils.KeyStore;
import cn.wildfirechat.chat.R;

public class LoginBip39Activity extends AppCompatActivity {

    private Button btn_chaungjian,btn_daoru,btn_login;
    private String mnemonicLoginUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginbip39);

        mnemonicLoginUrl = getIntent().getStringExtra("mnemonicLoginUrl");

        initView();
        initOnClick();
    }



    private void initView(){
        btn_chaungjian = findViewById(R.id.btn_chuangjian);
        btn_daoru = findViewById(R.id.btn_daoru);
        btn_login = findViewById(R.id.btn_login);
    }

    private void initOnClick(){
        btn_chaungjian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginBip39Activity.this, ImportBip39Activity.class);
                intent.putExtra("mnemonicLoginUrl",mnemonicLoginUrl);
                intent.putExtra("type",1);
                startActivity(intent);
            }
        });
        btn_daoru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginBip39Activity.this, ImportBip39Activity.class);
                intent.putExtra("mnemonicLoginUrl",mnemonicLoginUrl);
                intent.putExtra("type",2);
                startActivity(intent);
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginBip39Activity.this, AccountListActivity.class);
                startActivity(intent);
            }
        });
    }
}
