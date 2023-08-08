package cn.wildfire.chat.app.login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.system.Os;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import static android.util.Base64.NO_WRAP;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.wildfire.chat.app.AppService;
import cn.wildfire.chat.app.ProgresDialog;
import cn.wildfire.chat.app.SPUtils;
import cn.wildfire.chat.app.main.MainActivity;
import cn.wildfire.chat.app.main.MainActivity1;
import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.group.manage.SetKeyWordActivity;
import cn.wildfirechat.chat.BuildConfig;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.client.SqliteUtils;
import cn.wildfirechat.model.WalletsInfo;
import cn.wildfirechat.remote.ChatManager;

import static cn.wildfirechat.client.ConnectionStatus.ConnectionStatusConnected;

public class LoginJSActivity extends WfcBaseActivity implements LoginJSWebModule.LoginCallback{
    WebView mWebView;
    LoginJSWebModule mModelVue;
    final Object mLock = new Object();
    String TAG = "LoginJSActivity";
    private String old_lang;
    private String str_MainDapp = null;
    private String url;
    private Dialog dialog;
    private String password2 = "";
    private boolean start = false;
    private ProgresDialog progresDialog;

    @Override
    protected int contentLayout() {
        return R.layout.login_activity_web;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebview(){

        Intent intent = getIntent();
        url = intent.getStringExtra("url");

        progresDialog = new ProgresDialog(this);
        progresDialog.show();
        mModelVue = new LoginJSWebModule(this);
        mModelVue.setCallback(this);
        mWebView = findViewById(R.id.loginWebview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setAllowContentAccess(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setGeolocationEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);

        //使用缓存，否则localstorage等无法使用
        /*mWebView.getSettings().setAppCacheMaxSize(1024*1024*8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        mWebView.getSettings().setAppCachePath(appCachePath);
        mWebView.getSettings().setAppCacheEnabled(true);*/

        mWebView.addJavascriptInterface(mModelVue, "osnsdk");
        mWebView.setWebViewClient(new WebViewClient(){
            @SuppressLint("CommitPrefEdits")
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG,"url: "+url);
            }
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.d(TAG,"shouldOverrideUrlLoading url: "+request.getUrl());
                return false;
            }
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Log.d(TAG,"shouldInterceptRequest url: "+request.getUrl());
                return super.shouldInterceptRequest(view, request);
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient(){

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                return super.onJsAlert(view,url,message,result);            }
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                return super.onJsConfirm(view,url,message,result);            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
                return super.onJsPrompt(view, url, message, message, result);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if(newProgress == 100){
                    progresDialog.dismiss();
                }
            }
        });
        mWebView.loadUrl(url);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWebview();
   //     checkNeedPermission();
        Intent intent = getIntent();
        old_lang = intent.getStringExtra("old_lang");
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkNeedPermission() {
        boolean granted = true;
        String[] permissions = {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                granted = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
                if (!granted) {
                    break;
                }
            }
        }
        if(!granted){
            requestPermissions(permissions, 100);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.error_need_permission), Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == 100) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, getString(R.string.error_privilege_grant), Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }
    @Override
    public void onLogin(JSONObject data) {
        String username = data.getString("username");
        String password = data.getString("password");
        //System.out.println("@@@@@@ username : " + username);
        //System.out.println("@@@@@@ password : " + password);
        String ip = data.getString("ip");
        String token = data.getString("token");
        String language = data.getString("language");
        String json = data.getString("json");

        JSONObject jsonObject = JSONObject.parseObject(json);
        String APP_DEVICE = jsonObject.getString("APP_DEVICE");
        String AccessKeySecret = jsonObject.getString("AccessKeySecret");
        String GROUP_PROFIT_URL = jsonObject.getString("GROUP_PROFIT_URL");
        String BOMB_ROLE_URL = jsonObject.getString("BOMB_ROLE_URL");
        String RemoteTempFilePath = jsonObject.getString("RemoteTempFilePath");
        String TRANSFER_URL = jsonObject.getString("TRANSFER_URL");
        String GroupList = jsonObject.getString("GroupList");
        String HOST_IP = jsonObject.getString("HOST_IP");
        String GroupPortraitDirectory = jsonObject.getString("GroupPortraitDirectory");
        String WALLET_URL = jsonObject.getString("WALLET_URL");
        String APP_URL = jsonObject.getString("APP_URL");
        String SET_GROUP_URL = jsonObject.getString("SET_GROUP_URL");
        String AddGroup = jsonObject.getString("AddGroup");
        String ACCOUNT_PREFIX_URL = jsonObject.getString("ACCOUNT_PREFIX_URL");
        String QueryAplets = jsonObject.getString("QueryAplets");
        String UserPortraitDirectory = jsonObject.getString("UserPortraitDirectory");
        String GETTRANSFEREST_URL = jsonObject.getString("GETTRANSFEREST_URL");
        String GROUP_ZERO = jsonObject.getString("GROUP_ZERO");
        String KEFU_LIST = jsonObject.getString("KEFU_LIST");
        String SET_NAME = jsonObject.getString("SET_NAME");
        String TempDirectory = jsonObject.getString("TempDirectory");
        String BombRole = jsonObject.getString("BombRole");
        String AccessKeyId = jsonObject.getString("AccessKeyId");
        String ENDPOINT = jsonObject.getString("ENDPOINT");
        String BUCKETNAME = jsonObject.getString("BUCKETNAME");
        String GROUP_UPDATE_COUNT = jsonObject.getString("GROUP_UPDATE_COUNT");
        String MainDapp = jsonObject.getString("MainDapp");
        String PREFIX = jsonObject.getString("PREFIX");
        String Alias = jsonObject.getString("Alias");
        String Wallet = jsonObject.getString("Wallet");
        String redPack = jsonObject.getString("redPack");
        String voiceBaseUrl = jsonObject.getString("voiceBaseUrl");
        String voiceHostUrl = jsonObject.getString("voiceHostUrl");
        String DEL_ACCOUNT_URL = jsonObject.getString("DEL_ACCOUNT_URL");
        String create_url = jsonObject.getString("create_group_url");
        String LOGIN_URL = jsonObject.getString("LOGIN_URL");

        if(LOGIN_URL != null){
            SPUtils.put(LoginJSActivity.this,"LOGIN_URL",LOGIN_URL);
        }
        if (APP_DEVICE != null)
            SPUtils.put(LoginJSActivity.this,"APP_DEVICE",APP_DEVICE);
        if (AccessKeySecret != null)
            SPUtils.put(LoginJSActivity.this,"AccessKeySecret",AccessKeySecret);
        if (GROUP_PROFIT_URL != null)
            SPUtils.put(LoginJSActivity.this,"GROUP_PROFIT_URL",GROUP_PROFIT_URL);
        if (BOMB_ROLE_URL != null)
            SPUtils.put(LoginJSActivity.this,"BOMB_ROLE_URL",BOMB_ROLE_URL);
        if (RemoteTempFilePath != null)
            SPUtils.put(LoginJSActivity.this,"RemoteTempFilePath",RemoteTempFilePath);
        if (TRANSFER_URL != null)
            SPUtils.put(LoginJSActivity.this,"TRANSFER_URL",TRANSFER_URL);
        if (GroupList != null)
            SPUtils.put(LoginJSActivity.this,"GroupList",GroupList);
        if (HOST_IP != null)
            SPUtils.put(LoginJSActivity.this,"HOST_IP",HOST_IP);
        if (GroupPortraitDirectory != null)
            SPUtils.put(LoginJSActivity.this,"GroupPortraitDirectory",GroupPortraitDirectory);
        if (WALLET_URL != null)
            SPUtils.put(LoginJSActivity.this,"WALLET_URL",WALLET_URL);
        if (APP_URL != null)
            SPUtils.put(LoginJSActivity.this,"APP_URL",APP_URL);
        if (SET_GROUP_URL != null)
            SPUtils.put(LoginJSActivity.this,"SET_GROUP_URL",SET_GROUP_URL);
        if (AddGroup != null)
            SPUtils.put(LoginJSActivity.this,"AddGroup",AddGroup);
        if (ACCOUNT_PREFIX_URL != null)
            SPUtils.put(LoginJSActivity.this,"ACCOUNT_PREFIX_URL",ACCOUNT_PREFIX_URL);
        if (QueryAplets != null)
            SPUtils.put(LoginJSActivity.this,"QueryAplets",QueryAplets);
        if (UserPortraitDirectory != null)
            SPUtils.put(LoginJSActivity.this,"UserPortraitDirectory",UserPortraitDirectory);
        if (GETTRANSFEREST_URL != null)
            SPUtils.put(LoginJSActivity.this,"GETTRANSFEREST_URL",GETTRANSFEREST_URL);
        if (GROUP_ZERO != null)
            SPUtils.put(LoginJSActivity.this,"GROUP_ZERO",GROUP_ZERO);
        if (KEFU_LIST != null)
            SPUtils.put(LoginJSActivity.this,"KEFU_LIST",KEFU_LIST);
        if (SET_NAME != null)
            SPUtils.put(LoginJSActivity.this,"SET_NAME",SET_NAME);
        if (TempDirectory != null)
            SPUtils.put(LoginJSActivity.this,"TempDirectory",TempDirectory);
        if (BombRole != null)
            SPUtils.put(LoginJSActivity.this,"BombRole",BombRole);
        if (AccessKeyId != null)
            SPUtils.put(LoginJSActivity.this,"AccessKeyId",AccessKeyId);
        if (ENDPOINT != null)
            SPUtils.put(LoginJSActivity.this,"ENDPOINT",ENDPOINT);
        if (BUCKETNAME != null)
            SPUtils.put(LoginJSActivity.this,"BUCKETNAME",BUCKETNAME);
        if (GROUP_UPDATE_COUNT != null)
            SPUtils.put(LoginJSActivity.this,"GROUP_UPDATE_COUNT",GROUP_UPDATE_COUNT);
        if (PREFIX != null)
            SPUtils.put(LoginJSActivity.this,"PREFIX",PREFIX);
        if(redPack != null)
            SPUtils.put(LoginJSActivity.this,"redPack",redPack);
        SPUtils.remove(LoginJSActivity.this, "voiceBaseUrl");
        SPUtils.remove(LoginJSActivity.this, "voiceHostUrl");
        ChatManager.Instance().removeVoiceSetup();
        if(voiceBaseUrl != null)
            SPUtils.put(LoginJSActivity.this,"voiceBaseUrl",voiceBaseUrl);
        if(voiceHostUrl != null)
            SPUtils.put(LoginJSActivity.this,"voiceHostUrl",voiceHostUrl);
        if(DEL_ACCOUNT_URL != null)
            SPUtils.put(LoginJSActivity.this,"DEL_ACCOUNT_URL",DEL_ACCOUNT_URL);
        if(create_url != null)
            SPUtils.put(LoginJSActivity.this,"create_url",create_url);

        System.out.println("@@@     WALLET_URL    :"+WALLET_URL);

        ChatManager.Instance().setVoiceBaseUrl(voiceBaseUrl);
        ChatManager.Instance().setVoiceHostUrl(voiceHostUrl);

        System.out.println("@@@     QueryAplets :"+QueryAplets);

        SPUtils.remove(LoginJSActivity.this, "MainDapp");
        try {
            ChatManager.Instance().setMainDapp("");
        } catch (Exception e) {
        }

        if (MainDapp != null) {
            try {
                str_MainDapp = new String(Base64.decode(MainDapp,0), "utf-8");
                System.out.println("@@@  MainDapp:  "+MainDapp);
                SPUtils.put(LoginJSActivity.this,"MainDapp",str_MainDapp);
                SPUtils.put(LoginJSActivity.this,"ShowMain", "0");
                ChatManager.Instance().setMainDapp(str_MainDapp);
            } catch (Exception e) {
                SPUtils.put(LoginJSActivity.this,"ShowMain", "1");
            }
        } else {
            SPUtils.put(LoginJSActivity.this,"ShowMain", "1");
        }
        String Wallets = null;
        if(Wallet != null){
            try {
                Wallets = new String(Base64.decode(Wallet,0), "utf-8");
                SPUtils.put(LoginJSActivity.this,"Wallet",Wallets);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            SPUtils.remove(LoginJSActivity.this,"Wallet");
        }



        View views = View.inflate(LoginJSActivity.this, cn.wildfire.chat.kit.R.layout.dialog_keyword, null);
        Button btn_cancel = views.findViewById(cn.wildfire.chat.kit.R.id.btn_cancel);
        Button btn_confirm= views.findViewById(cn.wildfire.chat.kit.R.id.btn_confirm);
        EditText edt_keywords = views.findViewById(cn.wildfire.chat.kit.R.id.edt_keywords);
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginJSActivity.this);
        builder.setView(views);
        builder.setCancelable(true);



        String[] pwdArr = password.split("-");
        if (pwdArr == null) {
            intentActivity(ip,username,password,token,language,Alias);
            return;
        }

        if (pwdArr.length != 3) {
            intentActivity(ip,username,password,token,language,Alias);
            return;
        }

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
                String content = edt_keywords.getText().toString();
                if(!content.isEmpty()){
                    password2 = content;
                    dialog.dismiss();
                    intentActivity(ip,username,password,token,language,Alias);
                }else{
                    dialog.dismiss();
                    Toast.makeText(LoginJSActivity.this, getString(cn.wildfire.chat.kit.R.string.input_empty), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onMnemonicMode(JSONObject data) {
        // http://121.62.23.241:9903/login/loginByMnemonic
        String mnemonicLoginUrl = data.getString("url");
        SPUtils.put(LoginJSActivity.this,"mnemonicLoginUrl",mnemonicLoginUrl);

        // go to activitity
        Intent intent = new Intent(LoginJSActivity.this, LoginBip39Activity.class);
        intent.putExtra("mnemonicLoginUrl",mnemonicLoginUrl);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }

    private void intentActivity(String ip,String username,String password,String token,String language,String Alias){
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String oip = sp.getString("hostip", "");
        sp.edit().putString("hostip",ip).apply();
        AtomicBoolean loginResult = new AtomicBoolean(false);
        new Thread(()->{
            try {
                System.out.println("IP:" + ip);
                if(!oip.equalsIgnoreCase(ip))
                    ChatManager.Instance().setHost(ip);
                long timestamp = System.currentTimeMillis()+15000;
                do{
                    Thread.sleep(100);
                }while(ChatManager.Instance().getConnectionStatus() != ConnectionStatusConnected
                        && timestamp > System.currentTimeMillis());
                //ChatManagerHolder.gChatManager.disconnect(true, false);
                if (password2.equals("")) {
                    loginResult.set(ChatManagerHolder.gChatManager.connect(username, password));
                } else
                    loginResult.set(ChatManagerHolder.gChatManager.connect(username, password, password2));
                synchronized (mLock) {
                    mLock.notify();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();

        try {
            synchronized (mLock) {
                mLock.wait(16000);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        //dialog.dismiss();
        if (loginResult.get()) {
            String userID = ChatManagerHolder.gChatManager.getUserId();
            sp = getSharedPreferences("config", Context.MODE_PRIVATE);
            sp.edit().putString("id", userID).putString("token", token).putString("language", language).apply();

            //////////////////////////////////////
            //Locale locale = new Locale(language);
            //Locale.setDefault(locale);
            Resources resources = getResources();
            Configuration config = resources.getConfiguration();
            String old_lang = config.getLocales().toLanguageTags();
            String new_lang = WfcBaseActivity.toLanguage(language);
            boolean restart = false;

            System.out.println("@@@3   old_lang "+old_lang);

            if (!old_lang.startsWith(new_lang)) {
                // 给一个重启标签，传递给main
                restart = true;
            }
            if(old_lang.startsWith("en")){
                WfcBaseActivity.setBaseLanguage(new_lang);
                ChatManager.Instance().setLanguage(WfcBaseActivity.toLanguage("en"));
            }else if(old_lang.startsWith("zh")){
                ChatManager.Instance().setLanguage(WfcBaseActivity.toLanguage("zh"));
            }else if(old_lang.startsWith("vn")){
                ChatManager.Instance().setLanguage(WfcBaseActivity.toLanguage("vn"));
            }

            boolean isNeedReset = false;
            if(Alias == null){
                isNeedReset = true;
            }
            SPUtils.put(LoginJSActivity.this,"isNeedReset",isNeedReset);

            System.out.println("@@@     str_MainDapp :"+str_MainDapp);

            if(str_MainDapp==null){
                Intent intent = new Intent(LoginJSActivity.this, MainActivity1.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("restart",restart);
                intent.putExtra("new_lang",new_lang);
                intent.putExtra("old_lang",old_lang);
                startActivity(intent);
                finish();
                return;
            }
            if(str_MainDapp.length() < 5){
                Intent intent = new Intent(LoginJSActivity.this, MainActivity1.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("restart",restart);
                intent.putExtra("new_lang",new_lang);
                intent.putExtra("old_lang",old_lang);
                startActivity(intent);
                finish();
            }else{
                Intent intent = new Intent(LoginJSActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("restart",restart);
                intent.putExtra("new_lang",new_lang);
                intent.putExtra("old_lang",old_lang);
                startActivity(intent);
                finish();
            }

        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AppService.validateConfig(LoginJSActivity.this);
                    Toast.makeText(LoginJSActivity.this, getString(R.string.error_login_failed), Toast.LENGTH_SHORT).show();
                }
            });

        //    runOnUiThread(() -> Toast.makeText(LoginJSActivity.this, getString(R.string.error_login_failed), Toast.LENGTH_SHORT).show());
        }
    }
}
