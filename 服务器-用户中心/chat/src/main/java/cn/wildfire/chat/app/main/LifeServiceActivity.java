package cn.wildfire.chat.app.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;

import cn.wildfire.chat.kit.litapp.DappActivity2;
import cn.wildfire.chat.kit.litapp.LitappActivity;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.model.LitappInfo;

public class LifeServiceActivity extends AppCompatActivity {

    private ImageView img_back;
    private LinearLayout ll_take_taxi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_life_service);

        initView();
        initOnClick();
    }

    private void initView(){
        img_back = findViewById(R.id.img_back);
        ll_take_taxi = findViewById(R.id.ll_take_taxi);

    }
    private void initOnClick(){
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ll_take_taxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String dapp = "{\"name\":\"微喇打车\",\"url\":\"https://web.weila.hk/\",\"target\":\"OSNS6qJXpF5VXqSzFVdtG3NnGrgwQp1nbQZcFuqwF8gLHP9no2c\",\"info\":{\"sign\":\"MEQCIC1HoWyDaWZpPxOMJRT0XCB1FRQGPtavUg4d8jPrT9J7AiBDc5aTFkUgBuuefWRXbGsKrSsR5goJ0m2b9ihtnvQ29Q==\"}}";
                    JSONObject dappJson = JSONObject.parseObject(dapp);
                    LitappInfo litappInfo = new LitappInfo();
                    litappInfo.target = dappJson.getString("target");
                    litappInfo.info = dappJson.getString("info");
                    litappInfo.url = dappJson.getString("url");

                    Intent intent4 = new Intent(LifeServiceActivity.this, LitappActivity.class);
                    intent4.putExtra("litappInfo", litappInfo);
                    startActivity(intent4);

                } catch (Exception e) {

                }
            }
        });
    }
}
