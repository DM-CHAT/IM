package cn.wildfire.chat.app.main;

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
import android.util.Log;
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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import cn.wildfire.chat.app.SPUtils;
import cn.wildfire.chat.kit.contact.ContactListActivity;
import cn.wildfire.chat.kit.conversation.forward.ForwardActivity;
import cn.wildfire.chat.kit.litapp.DappActivity2;
import cn.wildfire.chat.kit.litapp.LitappActivity;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.message.CardMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback2;

public class FoundWevViewActivity extends AppCompatActivity {

    private WebView mWebView;

    JSONObject jsonMainDapp;

    // add by
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actiivity_found_webview);

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
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
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
                FrameLayout frameLayout = new FrameLayout(FoundWevViewActivity.this);
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



        String MainDapp = (String) SPUtils.get(FoundWevViewActivity.this,"MainDapp","");
        String url = null;
        try {
            jsonMainDapp = JSONObject.parseObject(MainDapp);
            url = jsonMainDapp.getString("url");

        } catch (Exception e) {

        }
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String language = sp.getString("language", "0");
        //String token = "1d954bf94a1e464c91397df7324c67d7";
        String token = (String) SPUtils.get(FoundWevViewActivity.this,"photoToken","");
        //String token = sp.getString("photo_token", "");
        url += url.contains("?") ? "&" : "?";
        if (language.equals("0")) {
            url += "language=zh-CN";
        } else if (language.equals("2")) {
            url += "language=en-US";
        } else if (language.equals("1")) {
            url += "language=vi-VN";
        }
        //url += "language="+language+"&token="+token;
        url += "&token="+token;
        url += "&loginType=osn&appId=com.mhhy.jtalking10";
        //Log.d(TAG, "url: "+url);
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
        try {
            System.out.println("@@@ foundWevView command : " + json.getString("command"));
            switch (json.getString("command")) {
                case "Login":
                    //Log.d(TAG, "Login to url: " + json.getString("url"));
                    String url = json.getString("url");
                    /*if (url == null)
                        url = litappInfo.url;*/
                    LitappInfo litappInfo = new LitappInfo();
                    litappInfo.target = "OSNS6qJXowk8ipXWrzUpvS7QYp2XQcUo5xGWMbKRLXgEx349NQY";
                    ChatManager.Instance().ltbLogin(litappInfo, url, new GeneralCallback2() {
                        @Override
                        public void onSuccess(String sessionKey) {
                            runOnUiThread(() -> {
                                //isLogin = true;
                                JSONObject result = JSON.parseObject(sessionKey);
                                String photoToken = result.getString("token");
                                SPUtils.put(FoundWevViewActivity.this,"photoToken", photoToken);
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
                            /*runOnUiThread(() -> {
                                isLogin = false;
                                JSONObject result = setReply("-1:failure", null);
                                if (callback == null) {
                                    data.set(result);
                                    synchronized (this) {
                                        this.notify();
                                    }
                                } else {
                                    mWebView.loadUrl("javascript:" + callback + "(" + result.toString() + ")");
                                }
                            });*/
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
                case "Share":

                    /*JSONObject dappJson2 = json.getJSONObject("DappInfo");
                    if (dappJson2 != null) {
                        LitappInfo dapp = new LitappInfo(dappJson2);

                        Intent intent6 = new Intent(FoundWevViewActivity.this, ContactListActivity.class);
                        intent6.putExtra("litappInfo", dapp);
                        intent6.putExtra("pick", true);
                        startActivity(intent6);
                    }*/
                    JSONObject dappJson2 = json.getJSONObject("DappInfo");
                    if (dappJson2 != null) {
                        Message msg = new Message();
                        msg.content = new CardMessageContent(dappJson2);
                        msg.sender = ChatManager.Instance().getUserId();
                        msg.conversation = new Conversation(Conversation.ConversationType.Single, msg.sender);
                        msg.direction = MessageDirection.Send;
                        msg.status = MessageStatus.Sending;
                        msg.serverTime = System.currentTimeMillis();

                        Intent intent6 = new Intent(FoundWevViewActivity.this, ForwardActivity.class);
                        intent6.putExtra("message", msg);
                        startActivity(intent6);

                    }

                    break;
                case "GoBack":
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
