package cn.wildfire.chat.kit.litapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.third.utils.UIUtils;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.remote.ChatManager;

public class AddSqliteActivity extends AppCompatActivity {

    private Button btn_add,btn_close;
    private String wallet;
    private ImageView iv_finish,iv_logo;
    private TextView tv_name,tv_target;
    private String name,logo,target;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addsplite);
        Intent intent =getIntent();
        wallet = intent.getStringExtra("wallet");
        System.out.println("@@@   add wallet : " +wallet);


        JSONObject jsonObject = new JSONObject();
        jsonObject = JSON.parseObject(wallet);
        name = jsonObject.getString("name");
        logo = jsonObject.getString("portrait");
        target = jsonObject.getString("target");

        /*String param = "{\"name\":\"Alipay\",\"type\":\"wallet\",\"portrait\":\"http://www.openspn.com/test/alipay.jpg\",\"transactionUrl\":\"http://www.openspn.com/test/transaction.html\"}";
        String name = "Alipay";
        String portrait = "http://www.openspn.com/test/alipay.jpg";
        String url = "http://www.openspn.com/test/assets.html";
        String target = "OSNS6qJPPqJv1PRJJwyBWjgqWFDVjUTqarJHuX5PfPcYZZcgpxF";
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("sign","MEQCIB0qeeHH9WAYNPhfZmJqXFACzBpKWPtXBQDg/eqUbqO0AiBxJrJN7f2FbwBhcFEVEp8TGLEnR8qluwgsK6AkaEqugA==");
        jsonObject.put("param",param);
        jsonObject.put("name",name);
        jsonObject.put("portrait",portrait);
        jsonObject.put("url",url);
        jsonObject.put("target",target);
        jsonObject.put("info",jsonObject1);*/

        //System.out.println("@@@   jsob="+jsonObject.toString());

        initView();
        initOnClick();

    }

    private void initView(){
        btn_close = findViewById(R.id.btn_close);
        btn_add = findViewById(R.id.btn_add);
        iv_finish = findViewById(R.id.iv_finish);
        iv_logo = findViewById(R.id.iv_logo);
        tv_name = findViewById(R.id.tv_name);
        tv_target = findViewById(R.id.tv_target);

        tv_name.setText(name);
        tv_target.setText(target);

        RequestOptions options = new RequestOptions()
                .placeholder(R.mipmap.avatar_def)
                .transforms(new CenterCrop(), new RoundedCorners(UIUtils.dip2Px(this, 10)));
        Glide.with(this)
                .load(logo)
                .apply(options)
                .into(iv_logo);
    }

    private void initOnClick(){
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        iv_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatManager.Instance().getWorkHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        ChatManager.Instance().insertWallets(wallet);
                        SPUtils.put(AddSqliteActivity.this, "enable3", true);
                        Toast.makeText(AddSqliteActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
    }
}
