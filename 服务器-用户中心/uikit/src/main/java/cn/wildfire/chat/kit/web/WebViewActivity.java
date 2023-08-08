package cn.wildfire.chat.kit.web;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSONObject;

import butterknife.BindView;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.redpacket.RedPacketInfoActivity;
import cn.wildfirechat.model.RedPacketInfo;
import cn.wildfirechat.remote.GeneralCallback2;

public class WebViewActivity extends WfcBaseActivity implements WebViewInterface.RunProxy {
    WebViewInterface webViewInterface;
    @BindView(R2.id.webview)
    WebView webView;
    String TAG = WebViewActivity.class.getSimpleName();
    private String isSetting;

    @Override
    protected int contentLayout() {
        return R.layout.activity_webview;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void afterViews(){
        try {
            getSupportActionBar().hide();
            //getWindow().setStatusBarColor(Color.rgb(255,255,255));

            webViewInterface = new WebViewInterface(this, webView, this, this);
            String url = getIntent().getStringExtra("url");
            isSetting = getIntent().getStringExtra("isSetting");
            SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
            String language = sp.getString("language", "0");
            String token = sp.getString("token", "");
            url += url.contains("?") ? "&" : "?";
       //     url += "language="+language+"&token="+token;
            url += "token="+token+"&language="+language;
            if(isSetting != null){
                url = url + "&isSetting=1";
            }
            System.out.println("@@@    资产url  :"+url);
            webView.loadUrl(url);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        webViewInterface.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public JSONObject run(JSONObject json, GeneralCallback2 callback) {
        try {
            Log.d(TAG, "run: " + json.toString());
            String command = json.getString("command");
            switch (command) {
                case "GoBack":
                    finish();
                    break;
                case "ShowRedPacket":
                    showRedPacket(json);
                    break;
            }
            return webViewInterface.setResult(null);
        }catch (Exception e){
            e.printStackTrace();
            return webViewInterface.setError("-1:"+e.getLocalizedMessage());
        }
    }
    public void showRedPacket(JSONObject json){
        RedPacketInfo redPacketInfo = new RedPacketInfo();
        redPacketInfo.type = "loot";
        redPacketInfo.count = json.getString("count");
        redPacketInfo.price = json.getString("price");
        redPacketInfo.text = json.getString("text");
        redPacketInfo.packetID = json.getString("packetID");
        redPacketInfo.urlQuery = json.getString("urlQuery");
        redPacketInfo.luckNum = json.getString("luckNum");
        redPacketInfo.state = 1;
        Intent intent = new Intent(this, RedPacketInfoActivity.class);
        intent.putExtra("packetID", redPacketInfo.packetID);
        intent.putExtra("groupID", json.getString("groupID"));
        intent.putExtra("redPacketInfo", redPacketInfo);
        startActivity(intent);
    }
}
