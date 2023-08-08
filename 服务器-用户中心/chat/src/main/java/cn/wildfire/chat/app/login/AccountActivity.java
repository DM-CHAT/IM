package cn.wildfire.chat.app.login;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import cn.wildfirechat.chat.R;

public class AccountActivity extends AppCompatActivity {

    private String mnemonic;
    private TextView tv_mnemonic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mnemonic = getIntent().getStringExtra("mnemonic");
        tv_mnemonic = findViewById(R.id.tv_mnemonic);
        tv_mnemonic.setText(mnemonic);

    }
}
