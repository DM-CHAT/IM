package cn.wildfire.chat.kit.web;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Arrays;

import cn.wildfire.chat.kit.litapp.LitappActivity;
import cn.wildfire.chat.kit.utils.ProgresDialog;
import cn.wildfirechat.client.IGeneralCallback;
import cn.wildfirechat.client.IGeneralCallback2;
import cn.wildfirechat.remote.GeneralCallback2;

public class WebViewInterface {
    Context mContext;
    Activity mActivity;
    WebView mWebView;
    RunProxy mProxy;
    String TAG = WebViewInterface.class.getSimpleName();
    private String url_webView;

    View customView;
    FrameLayout fullscreenContainer;
    WebChromeClient.CustomViewCallback customViewCallback;
    static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    ValueCallback<Uri[]> uploadMessageAboveL;

    public final static int FILE_CHOOSER_RESULT_CODE = 10000;
    private ProgresDialog progresDialog;

    public interface RunProxy {
        JSONObject run(JSONObject json, GeneralCallback2 callback);
    }
    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    WebViewInterface(Context context, WebView webView, RunProxy runProxy, Activity activity){
        mActivity = activity;
        mWebView = webView;
        mContext = context;
        mProxy = runProxy;
        progresDialog = new ProgresDialog(mContext);
        progresDialog.show();
        mWebView.clearCache(true);
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
        mWebView.addJavascriptInterface(this, "osnsdk");
        mWebView.setWebViewClient(new WebViewClient(){
            @SuppressLint("CommitPrefEdits")
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG,"onPageFinished url: "+url);
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
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                Log.d(TAG, "onReceivedSslError: "+error.toString());
                if (view != null) {
                    handler.proceed();
                } else {
                    handler.cancel();
                }
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient(){
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                Log.d(TAG, "onShowFileChooser: "+fileChooserParams);
                uploadMessageAboveL = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                activity.startActivityForResult(Intent.createChooser(intent, "Image Browser"), FILE_CHOOSER_RESULT_CODE);
                return true;
            }
            @Override
            public View getVideoLoadingProgressView() {
                FrameLayout frameLayout = new FrameLayout(mContext);
                frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                return frameLayout;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if(newProgress == 100){
                    progresDialog.dismiss();
                }
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
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessageAboveL)
                return;
            onActivityResultAboveL(requestCode, resultCode, data);
        }
    }
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
        Log.d(TAG, "onActivityResultAboveL: "+ Arrays.toString(results));
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
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

        mActivity.getWindow().getDecorView();

        FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
        fullscreenContainer = new WebViewInterface.FullscreenHolder(mActivity);
        fullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        decor.addView(fullscreenContainer, COVER_SCREEN_PARAMS);
        customView = view;
        setStatusBarVisibility(false);
        customViewCallback = callback;
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    /** 隐藏视频全屏 */
    private void hideCustomView() {
        if (customView == null) {
            return;
        }

        setStatusBarVisibility(true);
        FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
        decor.removeView(fullscreenContainer);
        fullscreenContainer = null;
        customView = null;
        customViewCallback.onCustomViewHidden();
        mWebView.setVisibility(View.VISIBLE);
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    private void setStatusBarVisibility(boolean visible) {
        int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
        mActivity.getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    JSONObject setResult(JSONObject json){
        if(json == null){
            json = new JSONObject();
        }
        json.put("errCode", "0:success");
        return json;
    }
    JSONObject setError(String code){
        JSONObject json = new JSONObject();
        json.put("errCode", code);
        return json;
    }
    @android.webkit.JavascriptInterface
    public String run(String args, String callback) {
        Log.d(TAG,"args: "+args);
        try {
            JSONObject json = JSON.parseObject(args);
            if (json == null){
                Log.d(TAG, "args parse failure");
                if(callback != null)
                    url_webView = "javascript:"+callback+"("+setError("-1:args parse failure").toString()+")";
                    mWebView.loadUrl("javascript:"+callback+"("+setError("-1:args parse failure").toString()+")");
                return setError("-1:args parse failure").toString();
            }
            JSONObject result = mProxy.run(json, new GeneralCallback2() {
                @Override
                public void onSuccess(String result) {
                    Log.d(TAG, "result: "+result);
                    if(callback != null)
                        mWebView.loadUrl("javascript:"+callback+"("+result+")");
                }

                @Override
                public void onFail(int errorCode) {
                    Log.d(TAG, "error: "+errorCode);
                    if(callback != null)
                        mWebView.loadUrl("javascript:"+callback+"("+setError(errorCode+":result error").toString()+")");
                }
            });
            return result == null ? null : result.toString();
        } catch (Exception e){
            e.printStackTrace();
            if(callback != null)
                mWebView.loadUrl("javascript:"+callback+"("+setError("-2:"+e.getLocalizedMessage()).toString()+")");
            return setError("-2:"+e.getLocalizedMessage()).toString();
        }
    }
}
