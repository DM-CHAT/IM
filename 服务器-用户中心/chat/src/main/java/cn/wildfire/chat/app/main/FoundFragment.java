package cn.wildfire.chat.app.main;

import static cn.wildfirechat.message.CardMessageContent.CardType_Channel;
import static cn.wildfirechat.message.CardMessageContent.CardType_Group;
import static cn.wildfirechat.message.CardMessageContent.CardType_Litapp;
import static cn.wildfirechat.message.CardMessageContent.CardType_User;

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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.bean.ImageItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import cn.wildfire.chat.app.OssHelper;
import cn.wildfire.chat.app.SPUtils;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.common.OperateResult;
import cn.wildfire.chat.kit.contact.ContactListActivity;
import cn.wildfire.chat.kit.conversation.forward.ForwardActivity;
import cn.wildfire.chat.kit.conversation.forward.ForwardPromptView;
import cn.wildfire.chat.kit.group.GroupInfoActivity;
import cn.wildfire.chat.kit.litapp.FileUploadSetup;
import cn.wildfire.chat.kit.litapp.LitappActivity;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.message.CardMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback2;

public class FoundFragment extends Fragment {

    private View view;
    private WebView mWebView;
    private ImageView iv_share;
    private JSONObject jsonMainDapp;
    private String target;
    private String language;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;
    private static final int PICK_IMAGE_RESULT_CODE = 10001;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragement_found,container,false);
        initView();
        initOnClick();
        return view;
    }

    @SuppressLint("JavascriptInterface")
    private void initView(){
        iv_share = view.findViewById(R.id.iv_share);
        mWebView = view.findViewById(cn.wildfire.chat.kit.R.id.webView);
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
        mWebView.getSettings().setDisplayZoomControls(false);   //隐藏缩放控件

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
                FrameLayout frameLayout = new FrameLayout(getActivity());
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
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //在这里执行你想调用的js函数
            }
        });

        String MainDapp = (String) SPUtils.get(getActivity(),"MainDapp","");
        if(TextUtils.isEmpty(MainDapp)){
            return;
        }
        String url = null;
        try {
            jsonMainDapp = JSONObject.parseObject(MainDapp);
            url = jsonMainDapp.getString("url");
            target = jsonMainDapp.getString("target");

        } catch (Exception e) {

        }
        if(url == null){
            return;
        }
        //    String url = "http://127.0.0.1:81/firend-font/#/dalogin?isMe=2";
        SharedPreferences sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        language = sp.getString("language", "0");
        //String token = "1d954bf94a1e464c91397df7324c67d7";
        String token = (String) SPUtils.get(getActivity(),"photoToken","");
        //String token = sp.getString("photo_token", "");
        url += url.contains("?") ? "&" : "?";
        if (language.equals("0")) {
            url += "language=zh-CN";
        } else if (language.equals("2")) {
            url += "language=en-US";
        } else if (language.equals("1")) {
            url += "language=vi-VN";
        } else {
            url += "language=zh-CN";
        }

        mWebView.loadUrl(url);
    }

    private void initOnClick(){
        iv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),FoundWevViewActivity.class);
                startActivity(intent);
            }
        });
    }

    @android.webkit.JavascriptInterface
    public String run(String args) {
      //  return null;
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
    JSONObject setReply(String error, JSONObject json) {
        if (json == null)
            json = new JSONObject();
        json.put("errCode", error == null ? "0:success" : error);
        return json;
    }




    private String globalCallback = null;

    JSONObject runJsCommand(JSONObject json, String callback) {
        globalCallback = callback;
        AtomicReference<JSONObject> data = new AtomicReference<>();
        try {
            switch (json.getString("command")) {

                case "Login":
                    String url = json.getString("url");
                    LitappInfo litappInfo = new LitappInfo();
                    litappInfo.target = target;
                    ChatManager.Instance().ltbLogin(litappInfo, url, new GeneralCallback2() {
                        @Override
                        public void onSuccess(String sessionKey) {
                            getActivity().runOnUiThread(() -> {
                                //isLogin = true;
                                JSONObject result = JSON.parseObject(sessionKey);
                                String photoToken = result.getString("token");
                                SPUtils.put(getActivity(),"photoToken", photoToken);
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
                case "AddGroup":
                    Intent intent2 = new Intent(mWebView.getContext(), GroupInfoActivity.class);
                    intent2.putExtra("groupId",json.getString("groupId"));
                    startActivity(intent2);
                    break;
                case "GroupInfo":
                    Intent intent3 = new Intent(mWebView.getContext(), GroupInfoActivity.class);
                    intent3.putExtra("groupId",json.getString("osnID"));
                    startActivity(intent3);
                    break;
                case "OpenDapp":
                    try {
                        JSONObject dappJson = json.getJSONObject("DappInfo");
                        LitappInfo litappInfo1  = new LitappInfo();
                        litappInfo1.target = dappJson.getString("target");
                        litappInfo1.param = dappJson.getString("param");
                        litappInfo1.info = dappJson.getString("info");
                        litappInfo1.name = dappJson.getString("name");
                        litappInfo1.portrait = dappJson.getString("portrait");
                        litappInfo1.theme = dappJson.getString("theme");
                        litappInfo1.displayName = dappJson.getString("displayName");
                        litappInfo1.url = dappJson.getString("url");
                        Intent intent1 = new Intent(getActivity(), LitappActivity.class);
                        intent1.putExtra("litappInfo",litappInfo1);
                        if (language.equals("0")) {
                            intent1.putExtra("language","language=zh-CN");
                        } else if (language.equals("2")) {
                            intent1.putExtra("language","language=en-US");
                        } else if (language.equals("1")) {
                            intent1.putExtra("language","language=vi-VN");
                        }
                        startActivity(intent1);


                    }catch (Exception e){

                    }

                    break;
                case "AddObj":




                    Intent intent4 = new Intent(getActivity(), ContactListActivity.class);
                    ArrayList<String> filterUserList = new ArrayList<>();
                    intent4.putExtra("pick",true);
                    intent4.putExtra("share",true);
                    intent4.putExtra(ContactListActivity.FILTER_USER_LIST, filterUserList);

                    startActivityForResult(intent4, 100);
                    break;

                case "Share":
                    JSONObject dappJson2 = json.getJSONObject("DappInfo");
                    if (dappJson2 != null) {
                        Message msg = new Message();
                        msg.content = new CardMessageContent(dappJson2);
                        msg.sender = ChatManager.Instance().getUserId();
                        msg.conversation = new Conversation(Conversation.ConversationType.Single, msg.sender);
                        msg.direction = MessageDirection.Send;
                        msg.status = MessageStatus.Sending;
                        msg.serverTime = System.currentTimeMillis();

                        Intent intent6 = new Intent(getActivity(), ForwardActivity.class);
                        intent6.putExtra("message", msg);
                        startActivity(intent6);

                    }

                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data.get();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessageAboveL)
                return;
            onActivityResultAboveL(requestCode, resultCode, data);
        } else if (requestCode == PICK_IMAGE_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
            if (images == null || images.isEmpty()) {
                gameCallback(setReply("-1:no select file", null));
                return;
            }
            gameUpload(images);
        } else if (resultCode == Activity.RESULT_OK) {
            UserInfo userInfo = data.getParcelableExtra("userInfo");
            //ChannelInfo channelInfo = data.getParcelableExtra("channelInfo");
            GroupInfo groupInfo =  data.getParcelableExtra("groupInfo");
            LitappInfo litappInfo = data.getParcelableExtra("litappInfo");

            JSONObject result = new JSONObject();

            if (userInfo != null) {

            } else if (groupInfo != null) {
                result.put("type", "group");
                result.put("data", groupInfo.getPublicGroupInfo());
            } else if (litappInfo != null) {
                result.put("type", "dapp");
                result.put("data", litappInfo.getDappInfo());
            }

            /**
             *  取到群和小程序的信息以后，通过callback传给h5
             *
             *  result 的json格式
             *  type:group dapp person
             *  data:
             *  {
             *
             *  }
             *
             *
             *  **/
            if (globalCallback != null)
            {
                mWebView.loadUrl("javascript:" + globalCallback + "(" + result.toString() + ")");
            }
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
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }
    void gameCallback(JSONObject result) {
        if (globalCallback != null) {
            mWebView.post(() -> mWebView.loadUrl("javascript:" + globalCallback + "(" + result.toString() + ")"));
        }
    }
    private FileUploadSetup fileUploadSetup;
    void gameUpload(ArrayList<ImageItem> images) {


        if (fileUploadSetup != null) {
            if (fileUploadSetup.type != null) {
                if (fileUploadSetup.type.equals("aliyun")) {
                    aliyunOssUpload(images);
                }
            }
        }
    }

    void aliyunOssUpload(ArrayList<ImageItem> images) {

        List<String> remoteUrls = new ArrayList<>();
        MutableLiveData<OperateResult<String>> result = new MutableLiveData<>();
        OssHelper ossHelper = new OssHelper(getActivity(),
                fileUploadSetup.AccessKeyId,
                fileUploadSetup.AccessKeySecret,
                fileUploadSetup.ENDPOINT);


        for (ImageItem item : images) {

            ossHelper.uploadFile(ossHelper.ossClient, item.path, fileUploadSetup.childDir, fileUploadSetup.BUCKETNAME, new OssHelper.CallBack() {
                @Override
                public void success() {
                }

                @Override
                public void success(String remote, String filename) {
                    result.postValue(new OperateResult<>(remote, 0));
                }

                @Override
                public void fail() {
                }
            });
        }
        result.observe(this, stringOperateResult -> {
            if (stringOperateResult.isSuccess()) {
                remoteUrls.add(stringOperateResult.getResult());
                if (remoteUrls.size() == images.size()) {
                    JSONObject json = new JSONObject();
                    json.put("urls", remoteUrls);
                    gameCallback(setReply(null, json));
                }
            } else {
                Toast.makeText(getActivity(), "upload failure", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
