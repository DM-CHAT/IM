package cn.wildfire.chat.kit.sharehome;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.net.URLDecoder;
import java.util.Arrays;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.conversation.SingleConversationInfoFragment;
import cn.wildfire.chat.kit.widget.ProgressFragment;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.GeneralCallback2;

public class ShareHomeFragment extends ProgressFragment {
    String TAG = "ShareHomeFragment";
    WebView mWebView;
    ValueCallback<Uri[]> uploadMessageAboveL;
    int FILE_CHOOSER_RESULT_CODE = 10000;

    /** 视频全屏参数 */
    FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    View customView;
    FrameLayout fullscreenContainer;
    WebChromeClient.CustomViewCallback customViewCallback;

    protected int contentLayout() {
        return R.layout.share_home_fragment;
    }

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void afterViews(View view) {
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        mWebView = view.findViewById(R.id.shareWebview);
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
        mWebView.setWebChromeClient(new WebChromeClient(){
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                Log.d(TAG, "onShowFileChooser: "+fileChooserParams);
                uploadMessageAboveL = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                startActivityForResult(Intent.createChooser(intent, "Image Browser"), FILE_CHOOSER_RESULT_CODE);
                return true;
            }
            @Override
            public View getVideoLoadingProgressView() {
                FrameLayout frameLayout = new FrameLayout(getActivity());
                frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                return frameLayout;
            }
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                showCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                hideCustomView();
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle("");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, (dialog, which) -> result.confirm());
                b.setCancelable(false);
                b.create().show();
                return true;
            }
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle("");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, (dialog, which) -> result.confirm());
                b.setNegativeButton(android.R.string.cancel, (dialog, which) -> result.cancel());
                b.create().show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
                result.confirm();
                return super.onJsPrompt(view, url, message, message, result);
            }
        });
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "onPageFinished: "+url);
                showContent();
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try{
                    Log.d(TAG, "shouldOverrideUrlLoading: "+url);
                    url = URLDecoder.decode(url, "utf-8");
                    if(url.startsWith("app://")){
                        JSONObject data = new JSONObject();
                        JSONObject json = JSON.parseObject(url.substring(6));
                        String callback = json.getString("callback");
                        String serviceID;
                        switch(json.getString("command")){
                            case "Login":
                                Log.d(TAG,"Login to url: "+json.getString("url"));
                                url = json.getString("url");
                                serviceID = json.getString("serviceID");
                                ChatManager.Instance().simpleLogin(serviceID, url, new GeneralCallback2() {
                                    @Override
                                    public void onSuccess(String result) {
                                        getActivity().runOnUiThread(()->{
                                            Log.d(TAG,"login success");
                                            data.put("errCode","0:success");
                                            mWebView.loadUrl("javascript:"+callback+"("+data.toString()+")");
                                        });
                                    }

                                    @Override
                                    public void onFail(int errorCode) {
                                        getActivity().runOnUiThread(()->{
                                            Log.d(TAG,"error: "+errorCode);
                                            data.put("errCode","1:failure");
                                            mWebView.loadUrl("javascript:"+callback+"("+data.toString()+")");
                                        });
                                    }
                                });
                                break;
                            case "LoginSession":
                                Log.d(TAG, json.toString());
                                UserInfo userInfo = ChatManager.Instance().getUserInfo(null,false);
                                serviceID = json.getString("serviceID");
                                String challenge = json.getString("challenge");
                                String sign = ChatManager.Instance().signData(challenge.getBytes());
                                String eData = ChatManager.Instance().encryptData(serviceID, sign.getBytes());
                                data.put("data", eData);
                                data.put("errCode","0:success");
                                data.put("userID",userInfo.uid);
                                data.put("userName",userInfo.name);
                                data.put("nickName",userInfo.displayName);
                                data.put("portrait",userInfo.portrait);
                                mWebView.loadUrl("javascript:"+callback+"("+data.toString()+")");
                                Log.d(TAG,data.toString());
                                break;
                        }
                        return true;
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }
        });
        mWebView.loadUrl("http://127.0.0.1/");
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessageAboveL)
                return;
            onActivityResultAboveL(requestCode, resultCode, data);
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
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
        Log.d(TAG, "onActivityResultAboveL: "+ Arrays.toString(results));
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clear_cache) {
            mWebView.clearCache(true);
        }
        return super.onOptionsItemSelected(item);
    }
    /** 全屏容器界面 */
    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }
    /** 视频播放全屏 **/
    private void showCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        // if a view already exists then immediately terminate the new one
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }

        FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
        fullscreenContainer = new FullscreenHolder(getActivity());
        fullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        decor.addView(fullscreenContainer, COVER_SCREEN_PARAMS);
        customView = view;
        setStatusBarVisibility(false);
        customViewCallback = callback;
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    /** 隐藏视频全屏 */
    private void hideCustomView() {
        if (customView == null) {
            return;
        }

        setStatusBarVisibility(true);
        FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
        decor.removeView(fullscreenContainer);
        fullscreenContainer = null;
        customView = null;
        customViewCallback.onCustomViewHidden();
        mWebView.setVisibility(View.VISIBLE);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    private void setStatusBarVisibility(boolean visible) {
        int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getActivity().getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
