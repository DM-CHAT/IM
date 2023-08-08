package cn.wildfire.chat.kit.third.location.ui.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.utils.SPUtils;

public class WalletsActivity extends AppCompatActivity {

    private String wallets;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallets);
        wallets = (String) SPUtils.get(this,"Wallet","");
    }
}
