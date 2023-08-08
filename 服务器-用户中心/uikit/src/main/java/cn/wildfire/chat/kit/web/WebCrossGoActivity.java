package cn.wildfire.chat.kit.web;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSONObject;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.redpacket.RedPacketInfoActivity;
import cn.wildfirechat.model.RedPacketInfo;
import cn.wildfirechat.remote.GeneralCallback2;

public class WebCrossGoActivity extends WfcBaseActivity implements WebViewInterface.RunProxy {
    WebViewInterface webViewInterface;
    @BindView(R2.id.webview)
    WebView webView;
    boolean isSysExit = false;
    String TAG = WebCrossGoActivity.class.getSimpleName();

    @Override
    protected int contentLayout() {
        return R.layout.activity_webview;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void afterViews(){
        try {
            getSupportActionBar().hide();
            webViewInterface = new WebViewInterface(this, webView, this, this);
            String url = getIntent().getStringExtra("url");
            Log.d(TAG, "url: "+url);
            webView.loadUrl(url);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void onBackPressed() {
        if(webView.canGoBack()){
            webView.goBack();
            return;
        }
        if(!isSysExit){
            isSysExit = true;

            Toast.makeText(this, R.string.exit_tip, Toast.LENGTH_SHORT).show();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isSysExit = false;
                }
            }, 2000);
        }else {
            finish();
        }
    }
    @Override
    public JSONObject run(JSONObject json, GeneralCallback2 callback) {
        try {
            Log.d(TAG, "run: " + json.toString());
            String command = json.getString("command");
            if ("GoBack".equals(command)) {
                finish();
            }
            return webViewInterface.setResult(null);
        }catch (Exception e){
            e.printStackTrace();
            return webViewInterface.setError("-1:"+e.getLocalizedMessage());
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        webViewInterface.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onConfigurationChanged(@NotNull Configuration config) {
        super.onConfigurationChanged(config);
        switch (config.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                break;
        }
    }
}
