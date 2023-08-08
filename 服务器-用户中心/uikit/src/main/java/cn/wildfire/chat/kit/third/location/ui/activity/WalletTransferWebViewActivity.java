package cn.wildfire.chat.kit.third.location.ui.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.concurrent.atomic.AtomicReference;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfirechat.message.RedPacketMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback2;

public class WalletTransferWebViewActivity extends AppCompatActivity {

    private WebView mWebView;

    JSONObject jsonMainDapp;
    Conversation conversation;

    // add by
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;
    private String walletId;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actiivity_walletransfer_webview);

        mWebView = findViewById(cn.wildfire.chat.kit.R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setBuiltInZoomControls(false);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setAllowContentAccess(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setGeolocationEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        mWebView.addJavascriptInterface(this, "osnsdk");
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.setWebChromeClient(new WebChromeClient(){
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                //Log.d(TAG, "onShowFileChooser: "+fileChooserParams);
                uploadMessageAboveL = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                startActivityForResult(Intent.createChooser(intent, "Image Browser"), FILE_CHOOSER_RESULT_CODE);
                return true;
            }
            @Override
            public View getVideoLoadingProgressView() {
                FrameLayout frameLayout = new FrameLayout(WalletTransferWebViewActivity.this);
                frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                return frameLayout;
            }
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                //   showCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                //   hideCustomView();
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                return super.onJsAlert(view,url,message,result);
            }
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                return super.onJsConfirm(view,url,message,result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
                return super.onJsPrompt(view, url, message, message, result);
            }
        });
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                System.out.println("@@@   url="+url);
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //在这里执行你想调用的js函数
            }
        });
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        String payTo = intent.getStringExtra("payTo");
        walletId = intent.getStringExtra("walletId");
        if (!url.contains("?")) {
            url += "?loginType=osn";
        } else {
            url += "&loginType=osn";
        }
        url += "&payTo=";
        url += payTo;
        conversation = intent.getParcelableExtra("conversation");
        System.out.println("@@@  url="+url);
        mWebView.loadUrl(url);
    }


    // add by
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessageAboveL)
                return;
            onActivityResultAboveL(requestCode, resultCode, data);
        }
    }
    // add by
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return;
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        //Log.d(TAG, "onActivityResultAboveL: " + Arrays.toString(results));
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }


    JSONObject setReply(String error, JSONObject json) {
        if (json == null)
            json = new JSONObject();
        json.put("errCode", error == null ? "0:success" : error);
        return json;
    }


    JSONObject runJsCommand(JSONObject json, String callback) {
        AtomicReference<JSONObject> data = new AtomicReference<>();
        System.out.println("@@@   command="+json.getString("command"));
        try {
            switch (json.getString("command")) {
                case "Login":
                    LitappInfo litappInfo = new LitappInfo();
                    litappInfo.target = walletId;

                    //Log.d(TAG, "Login to url: " + json.getString("url"));
                    String url = json.getString("url");
                    if (url == null)
                        url = litappInfo.url;
                    ChatManager.Instance().ltbLogin(litappInfo, url, new GeneralCallback2() {
                        @Override
                        public void onSuccess(String sessionKey) {
                            runOnUiThread(() -> {
                                //isLogin = true;
                                JSONObject result = JSON.parseObject(sessionKey);
                                if (callback == null) {
                                    data.set(result);
                                    synchronized (this) {
                                        this.notify();
                                    }
                                } else {
                                    mWebView.loadUrl("javascript:" + callback + "(" + result.toString() + ")");
                                }
                            });
                        }

                        @Override
                        public void onFail(int errorCode) {
                            runOnUiThread(() -> {
                                JSONObject result = setReply("-1:failure", null);
                                if (callback == null) {
                                    data.set(result);
                                    synchronized (this) {
                                        this.notify();
                                    }
                                } else {
                                    mWebView.loadUrl("javascript:" + callback + "(" + result.toString() + ")");
                                }
                            });
                        }
                    });
                    if (callback == null) {
                        synchronized (this) {
                            this.wait(10000);
                        }
                    }
                    break;
                case "GetUserInfo":
                    /*if (!isLogin) {
                        data.set(setReply("-1:need login", null));
                        if (callback == null)
                            return data.get();
                        mWebView.loadUrl("javascript:" + callback + "(" + data.get().toString() + ")");
                        break;
                    }*/
                    UserInfo userInfo = ChatManager.Instance().getUserInfo(null, false);
                    JSONObject infoResult = new JSONObject();
                    infoResult.put("userID", userInfo.uid);
                    infoResult.put("userName", userInfo.name);
                    infoResult.put("nickName", userInfo.displayName);
                    infoResult.put("portrait", userInfo.portrait);
                    data.set(setReply(null, infoResult));
                    if (callback == null)
                        return data.get();
                    mWebView.loadUrl("javascript:" + callback + "(" + data.get().toString() + ")");
                    break;
                case "SignData":
                    /*if (!isLogin) {
                        data.set(setReply("-1:need login", null));
                        if (callback == null)
                            return data.get();
                        mWebView.loadUrl("javascript:" + callback + "(" + data.get().toString() + ")");
                        break;
                    }*/
                    String sign = ChatManager.Instance().signData(json.getString("data").getBytes());
                    JSONObject signResult = new JSONObject();
                    signResult.put("sign", sign);
                    signResult.put("data", json.getString("data"));
                    data.set(setReply(null, signResult));
                    if (callback == null)
                        return data.get();
                    mWebView.loadUrl("javascript:" + callback + "(" + data.get().toString() + ")");
                    break;
                case "AddFriend":
                    //userInfo = ChatManager.Instance().getUserInfo(json.getString("userID"), false);
                    userInfo = new UserInfo(json.getString("userID"));
                    Intent intent = new Intent(mWebView.getContext(), UserInfoActivity.class);
                    intent.putExtra("userInfo", userInfo);
                    startActivity(intent);
                    break;
                case "GoBack":
                    finish();
                    break;
                case "TransactionResult":
                    String userID = ChatManager.Instance().getUserId();
                    long timestamp = System.currentTimeMillis();
                    String strtemp = json.getString("Data");
                    JSONObject data1 = JSONObject.parseObject(strtemp);
                    String txid = data1.getString("txid");
                    String balance = data1.getString("balance");
                    String urlQuery = data1.getString("queryUrl");
                    String text = data1.getString("greetings");
                    String wallet = data1.getString("wallet");
                    String cointype = data1.getString("coinType");
                    String serial = data1.getString("serial");
                    String count = data1.getString("count");
                    String type = data1.getString("type");
                    JSONObject data2 = new JSONObject();

                    data2.put("txid", txid);
                    data2.put("text", text);
                    data2.put("packetID", txid);
                    data2.put("unpackID", txid);
                    data2.put("urlQuery", urlQuery);
                    data2.put("urlFetch", "");
                    data2.put("wallet",wallet);
                    data2.put("coinType",cointype);
                    data2.put("type",type);
                    data2.put("count",count);
                    String urlFetch = data1.getString("urlFetch");
                    if (urlFetch == null) {
                        urlFetch = "";
                    }
                    data2.put("urlFetch", urlFetch);
                    data2.put("greetings",text);
                    data2.put("luckNum","");
                    data2.put("balance",balance);
                    data2.put("from",userID);
                    data2.put("to",conversation.target);
                    if (serial == null) {
                        data2.put("serial",0);
                    } else {
                        data2.put("serial",serial);
                    }

                    if (count == null) {
                        data2.put("count","");
                    } else {
                        data2.put("count",count);
                    }

                    data2.put("timestamp",timestamp);


                    System.out.println("@@@   info:" + data2.toString());
                    Intent intent1 = new Intent();
                    intent1.putExtra("info",data2.toString());
                    intent1.putExtra("id",txid);
                    intent1.putExtra("text",text);
                    setResult(Activity.RESULT_OK, intent1);
                    finish();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data.get();
    }

    @android.webkit.JavascriptInterface
    public String run(String args) {
        String result = null;
        try {
            JSONObject json = JSON.parseObject(args);
            if (json == null)
                return setReply("-1:json parse error", null).toString();
            String callback = json.getString("callback");
            json = runJsCommand(json, callback);
            if (json != null)
                result = json.toString();
        } catch (Exception e) {
            e.printStackTrace();
            result = setReply("-1:" + e.getLocalizedMessage(), null).toString();
        }
        return result;
    }
}
