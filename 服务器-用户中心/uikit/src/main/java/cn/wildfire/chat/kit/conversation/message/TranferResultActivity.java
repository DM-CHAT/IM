package cn.wildfire.chat.kit.conversation.message;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Locale;

import cn.wildfire.chat.kit.ECUtils;
import cn.wildfire.chat.kit.OsnUtils;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.litapp.LitappActivity;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfire.chat.kit.redpacket.RedPacketInfoActivity;
import cn.wildfire.chat.kit.utils.LoadingDialog;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.RedPacketInfo;
import cn.wildfirechat.remote.ChatManager;

public class TranferResultActivity extends AppCompatActivity {

    private RelativeLayout rl_back;
    private String price;
    private long time;
    private TextView tv_price,tv_time;
    private String info;
    private String wallet;
    private String names;
    private String portraits;
    private ImageView iv_portrait;
    private TextView tv_name;
    private Button btn_shoukuan;
    private TextView tv_shoukuan;
    RedPacketInfo redPacketInfo = null;
    long mMessageID = -1;
    String dappId = null;
    private String direction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tranfer_result);
        Intent intent = getIntent();
        price = intent.getStringExtra("price");
        time = intent.getLongExtra("time",0);
        info = getIntent().getStringExtra("info");
        redPacketInfo = getIntent().getParcelableExtra("redPacketInfo");
        mMessageID = getIntent().getLongExtra("messageID", -1);
        direction = getIntent().getStringExtra("direction");


        if(info != null){
            JSONObject jsonObject = JSONObject.parseObject(info);
            wallet = jsonObject.getString("wallet");
            if(wallet != null){
                JSONObject jsonObject1 = JSONObject.parseObject(wallet);
                names = jsonObject1.getString("name");
                portraits = jsonObject1.getString("portrait");
            }
        }

        initView();
    }
    private void initView(){
        tv_price = findViewById(R.id.tv_price);
        tv_time = findViewById(R.id.tv_time);
        rl_back = findViewById(R.id.rl_back);
        iv_portrait = findViewById(R.id.iv_portrait);
        tv_name = findViewById(R.id.tv_name);
        btn_shoukuan = findViewById(R.id.btn_shoukuan);
        tv_shoukuan = findViewById(R.id.tv_shoukuan);

        if(redPacketInfo != null){
            if(redPacketInfo.state == 1){
                btn_shoukuan.setVisibility(View.GONE);
                tv_shoukuan.setText(getString(R.string.receive_shoukuan));
            }else{
                btn_shoukuan.setVisibility(View.VISIBLE);
                tv_shoukuan.setText(getString(R.string.please_shoukuan));
            }
        }

        rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tv_price.setText(price);
        System.out.println("@@@    money="+price +"   time="+time);
        //转换为String类型
        String endDate = timeStampToDate(time);
        tv_time.setText(endDate);

        tv_name.setText(names);
        Glide.with(this).load(portraits).apply(new RequestOptions().placeholder(R.mipmap.default_header).centerCrop()).into(iv_portrait);


        iv_portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LitappInfo litappInfo = new LitappInfo();

                try {

                    JSONObject jsonObject = JSONObject.parseObject(wallet);
                    System.out.println("@@@   wallet="+wallet);
                    litappInfo.url = jsonObject.getString("url");
                    litappInfo.info = jsonObject.getString("info");
                    litappInfo.portrait = jsonObject.getString("portrait");
                    litappInfo.target = jsonObject.getString("target");



                    System.out.println("@@@    target ="+litappInfo.target);

                    if (litappInfo.target.equals("OSNS6qJE5Y5JqH9hosq3bhzydniWzRZM8F18MQ24gLsqQzque49")) {
                        return;
                    }


                    litappInfo.name = jsonObject.getString("name");
                    litappInfo.displayName = "";
                    litappInfo.theme = "";
                    litappInfo.param = jsonObject.getString("param");
                    System.out.println("@@@    param2="+jsonObject.getString("param"));

                    Intent intent = new Intent(TranferResultActivity.this, LitappActivity.class);
                    intent.putExtra("litappInfo",litappInfo);
                    startActivity(intent);

                } catch (Exception e) {

                }
            }
        });

        btn_shoukuan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(redPacketInfo != null){
                    wallet = redPacketInfo.wallet;
                    if(wallet != null){
                        JSONObject jsonObject = JSONObject.parseObject(wallet);
                        dappId = jsonObject.getString("target");
                    }
                }
                long timestamp = System.currentTimeMillis();
            //    String hash = dappId+redPacketInfo.unpackID+ChatManager.Instance().getUserId()+timestamp;
           //     String userSign = ChatManager.Instance().signData(hash.getBytes());

                String calc = dappId+redPacketInfo.unpackID+ChatManager.Instance().getUserId()+timestamp;
                String hash = ECUtils.osnHash(calc.getBytes());
                String userSign = ChatManager.Instance().signData(hash.getBytes());


                JSONObject data = new JSONObject();
                data.put("command","open");
                data.put("dappId",dappId);
                data.put("txid",redPacketInfo.unpackID);
                data.put("from", ChatManager.Instance().getUserId());
                data.put("timestamp",timestamp);
                data.put("userSign",userSign);
                System.out.println("@@@              data="+data.toString());
                OKHttpHelper.postJson(redPacketInfo.urlFetch, data.toString(), new SimpleCallback<String>() {

                    @Override
                    public void onSuccess1(String t) {

                    }

                    @Override
                    public void onUiSuccess(String s) {
                        System.out.println("@@@          收款成功:"+s);
                        if(mMessageID != -1)
                            ChatManager.Instance().openRedPacket(redPacketInfo.packetID, redPacketInfo.unpackID, mMessageID);

                        redPacketInfo.state = 1;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ChatManager.Instance().updateRedPaket(redPacketInfo);
                                tv_shoukuan.setText("已收款");
                                btn_shoukuan.setVisibility(View.GONE);
                                ChatManager.Instance().updateRedPaket(redPacketInfo);
                            }
                        });
                    }

                    @Override
                    public void onUiFailure(int code, String msg) {
                        Toast.makeText(TranferResultActivity.this, getString(R.string.error_hit)+code, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        if(direction.equals("Send")){
            tv_shoukuan.setText("对方已收款");
            btn_shoukuan.setVisibility(View.GONE);
        }
    }

    private static String timeStampToDate(long tsp) {
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault());
        return sdf.format(tsp);
    }
}
