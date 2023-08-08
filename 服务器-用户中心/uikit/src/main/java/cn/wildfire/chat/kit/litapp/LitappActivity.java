package cn.wildfire.chat.kit.litapp;

import static cn.wildfirechat.message.CardMessageContent.CardType_Litapp;
import static cn.wildfirechat.message.CardMessageContent.CardType_User;
import static cn.wildfirechat.model.ModifyMyInfoType.Modify_Portrait;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.bean.ImageItem;

import org.jetbrains.annotations.NotNull;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.WfcBaseActivity3;
import cn.wildfire.chat.kit.WfcScheme;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.common.FeedbackActivity;
import cn.wildfire.chat.kit.common.OperateResult;
import cn.wildfire.chat.kit.contact.ContactListActivity;
import cn.wildfire.chat.kit.conversation.forward.ForwardActivity;
import cn.wildfire.chat.kit.group.GroupInfoActivity;
import cn.wildfire.chat.kit.qrcode.QRCodeActivity;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.utils.CryptUtils;
import cn.wildfire.chat.kit.utils.FileManage;
import cn.wildfire.chat.kit.utils.OrientationWatchDog;
import cn.wildfire.chat.kit.utils.OssHelper;
import cn.wildfire.chat.kit.utils.ProgresDialog;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.message.CardMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.ModifyMyInfoEntry;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.GeneralCallback2;

public class LitappActivity extends WfcBaseActivity3 {
    WebView mWebView;
    boolean isLogin = false;
    LitappInfo litappInfo;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    String TAG = LitappActivity.class.getSimpleName();
    boolean isCollect = false;

    /**
     * 视频全屏参数
     */
    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private View customView;
    private FrameLayout fullscreenContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;

    boolean isSysExit = false;
    boolean isJumpOut = false;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;
    private static final int PICK_IMAGE_RESULT_CODE = 10001;

    private ImageView iv_finish, iv_share,iv_collect,iv_collect_true;
    private TextView tv_name;

    private static Boolean isExit = false;
    private static Boolean hasTask = false;
    Timer tExit = new Timer();
    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            isExit = false;
            hasTask = true;
        }
    };

    private String language;

    private FileUploadSetup fileUploadSetup;

    private Dialog dialog, dialog1, dialog2, dialog3;
    private ProgresDialog progresDialog;


    @Override
    protected int contentLayout() {
        return R.layout.activity_litapp;
    }

    protected int menu() {
        return R.menu.litapp;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void afterViews() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);


        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String hostip = sp.getString("hostip", null);

        progresDialog = new ProgresDialog(this);
        progresDialog.show();

        ChatManager.init(getApplication(), hostip);
        language = getIntent().getStringExtra("language");
        litappInfo = getIntent().getParcelableExtra("litappInfo");
        isCollect = getIntent().getBooleanExtra("isCollect",false);
        if (litappInfo == null)
            return;

        iv_collect = findViewById(R.id.iv_collect);
        iv_collect_true = findViewById(R.id.iv_collect_true);
        if(isCollect){
            iv_collect.setVisibility(View.GONE);
            iv_collect_true.setVisibility(View.VISIBLE);
        }
        /*SPUtils.put(LitappActivity.this,"enable2",true);
        boolean enable2 =  (boolean) SPUtils.get(LitappActivity.this,"enable2",false);
        System.out.println("@@@ set enable2 : " + enable2);
        ChatManager.Instance().setHideEnable2("1");*/


        System.out.println("@@@  param url=" + litappInfo.url);
        setTitle(litappInfo.name);

        JSONObject json = new JSONObject();
        json.put("Litapp", litappInfo);
        Log.d(TAG, json.toString());

        yincangToolbar();
        iv_finish = findViewById(R.id.iv_finish);
        iv_share = findViewById(R.id.iv_share);

        tv_name = findViewById(R.id.tv_name);
        tv_name.setText(litappInfo.name);
        iv_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                /*ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                manager.restartPackage(getPackageName());*/
            }
        });



        iv_collect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatManager.Instance().addCollectLitapp(litappInfo, new GeneralCallback() {
                    @Override
                    public void onSuccess() {
                        iv_collect.setVisibility(View.GONE);
                        iv_collect_true.setVisibility(View.VISIBLE);
                        Toast.makeText(LitappActivity.this,"收藏成功",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFail(int errorCode) {

                    }
                });
            }
        });

        iv_collect_true.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatManager.Instance().deleteCollectLitapp(litappInfo.target);
                iv_collect.setVisibility(View.VISIBLE);
                iv_collect_true.setVisibility(View.GONE);
                Toast.makeText(LitappActivity.this,"取消收藏成功",Toast.LENGTH_SHORT).show();
            }
        });

        iv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //    openContextMenu(iv_share);

                /*Intent intent = new Intent(LitappActivity.this, ContactListActivity.class);
                intent.putExtra("litappInfo", litappInfo);
                intent.putExtra("pick", true);
                startActivity(intent);*/

                PopupMenu popupMenu = new PopupMenu(LitappActivity.this, iv_share);
                popupMenu.getMenuInflater().inflate(R.menu.litapp, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int itemId = menuItem.getItemId();
                        if (itemId == R.id.clear_cache) {
                            mWebView.clearCache(true);
                        } else if (itemId == R.id.litapp_qrcode) {
                            if (litappInfo != null) {
                                String qrCodeValue = WfcScheme.QR_CODE_PREFIX_LITAPP + litappInfo.target;
                                startActivity(QRCodeActivity.buildQRCodeIntent(LitappActivity.this, WfcUIKit.getString(R.string.qrcode), litappInfo.portrait, qrCodeValue, litappInfo.target));
                            }
                        } else if (itemId == R.id.litapp_share) {
                            if (litappInfo != null) {

                                /*System.out.println("@@@@    litappInfo  :"+litappInfo.url);
                                if(litappInfo.url.indexOf("?shareId") == -1){
                                    litappInfo.url = litappInfo.url + "?shareId="+ChatManager.Instance().getUserId();
                                }*/
                                System.out.println("@@@@@       litappInfo.url: "+litappInfo.url);
                                Message msg = new Message();
                                msg.content = new CardMessageContent(litappInfo);
                                msg.sender = ChatManager.Instance().getUserId();
                                msg.conversation = new Conversation(Conversation.ConversationType.Single, msg.sender);
                                msg.direction = MessageDirection.Send;
                                msg.status = MessageStatus.Sending;
                                msg.serverTime = System.currentTimeMillis();

                                Intent intent = new Intent(LitappActivity.this, ForwardActivity.class);
                                intent.putExtra("message", msg);
                                startActivity(intent);

                            /*    Intent intent = new Intent(LitappActivity.this, ContactListActivity.class);
                                intent.putExtra("litappInfo", litappInfo);
                                intent.putExtra("pick", true);
                                startActivity(intent);
                            */

                            }
                        }
                        return false;
                    }
                });
                popupMenu.show();

            }
        });

        registerForContextMenu(iv_share);

        mWebView = findViewById(R.id.litappWebview);
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
        mWebView.addJavascriptInterface(this, "osnsdk");
        mWebView.setWebChromeClient(new WebChromeClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                Log.d(TAG, "onShowFileChooser: " + fileChooserParams);
                uploadMessageAboveL = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                startActivityForResult(Intent.createChooser(intent, "Image Browser"), FILE_CHOOSER_RESULT_CODE);
                return true;
            }

            @Override
            public View getVideoLoadingProgressView() {
                FrameLayout frameLayout = new FrameLayout(LitappActivity.this);
                frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                return frameLayout;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
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
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
                return super.onJsPrompt(view, url, message, message, result);
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "onPageFinished: " + url);
                if (isLogin && !isJumpOut) {
                    isJumpOut = true;
                    mWebView.clearHistory();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    Log.d(TAG, "shouldOverrideUrlLoading: " + url);
                    url = URLDecoder.decode(url, "utf-8");
                    if (url.startsWith("app://")) {
                        JSONObject json = JSON.parseObject(url.substring(6));
                        String callback = json.getString("callback");
                        runJsCommand(json, callback);
                        return true;
                    }
                    if(url.startsWith("alipays://")){
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        mWebView.goBack();
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        if (!CheckRight(litappInfo.target)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.authorize);
            builder.setMessage(litappInfo.displayName + getString(R.string.auth_tip));
            builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
                AddRight(litappInfo.target);
                startLitapp();
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> finish());
            builder.show();
        } else {
            startLitapp();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void startLitapp() {
        new Thread(() -> {
            while (!ChatManager.Instance().checkRemoteService()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(() -> {
                if (checkLitapp()) {
                    System.out.println("@@@ 开始拼接----");
                    String url = litappInfo.url;
                    if (!url.contains("?")) {
                        url += "?loginType=osn&appId=com.jtalking.dm";
                    } else {
                        url += "&loginType=osn&appId=com.jtalking.dm";
                    }

                    try {
                        Resources resources = getResources();
                        Configuration config = resources.getConfiguration();
                        String lang = config.getLocales().toLanguageTags();
                        url += "&language=" + lang;
                    } catch (Exception e) {

                    }


                    // 从urlParam 这个JSON中循环获取参数传递

                    if (this.litappInfo.urlParam != null) {

                        JSONObject urlParamJson = JSONObject.parseObject(this.litappInfo.urlParam);

                        Set<String> keySet = urlParamJson.keySet();
                        for (String key : keySet) {
                            String value = urlParamJson.getString(key);
                            url += "&" + key + "=" + value;
                        }
                    }

                    System.out.println(TAG + " open dapp url : " + url);
                    //mWebView.loadUrl(url+"&"+language);
                    System.out.println("@@@ 拼接之后的url是："+url);
                    mWebView.loadUrl(url);
                } else {
                    Toast.makeText(this, "小程序签名校验失败", Toast.LENGTH_LONG).show();
                    new Thread(() -> {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        finish();
                    }).start();
                }
            });
        }).start();
    }

    boolean checkLitapp() {
        if (litappInfo.info == null || litappInfo.info.isEmpty())
            return true;
        try {
            JSONObject json = JSON.parseObject(litappInfo.info);
            String sign = json.getString("sign");
            if (sign != null) {
                System.out.println("@@@   param= " + litappInfo.param);
                String calc = litappInfo.target + litappInfo.url + litappInfo.param;
                String hash = ChatManager.Instance().hashData(calc.getBytes());
                Log.d(TAG, "verify data: " + calc + ", sign: " + sign + ", hash: " + hash);
                return ChatManager.Instance().verifyData(litappInfo.target, hash.getBytes(), sign);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    boolean checkOrderPaymentParam(JSONObject data, String dappId) {

        /** data 数据
         * walletName:
         * walletId:
         * coinType:
         * payId:
         * amount:
         * orderId:
         * timestamp:
         * orderId:
         * memo:{
         *     description:
         * }
         * sign:使用小程序id进行验签
         * **/

        try {


            String walletName = data.getString("walletName");
            String walletId = data.getString("walletId");
            String coinType = data.getString("coinType");
            String payId = data.getString("payId");
            String amount = data.getString("amount");
            String timestamp = data.getString("timestamp");
            String memo = data.getString("memo");
            String orderId = data.getString("orderId");


            String calc = walletName + walletId + coinType + payId + amount + orderId + timestamp + memo;
            String hash = ChatManager.Instance().hashData(calc.getBytes());
            String sign = data.getString("sign");

            return ChatManager.Instance().verifyData(dappId, hash.getBytes(), sign);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    void gameCallback(JSONObject result) {
        if (globalCallback != null) {
            mWebView.post(() -> mWebView.loadUrl("javascript:" + globalCallback + "(" + result.toString() + ")"));
        }
    }

    void gameUpload(ArrayList<ImageItem> images) {


        if (fileUploadSetup != null) {
            System.out.println("@@@ type : " + fileUploadSetup.type);
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
        OssHelper ossHelper = new OssHelper(this,
                fileUploadSetup.AccessKeyId,
                fileUploadSetup.AccessKeySecret,
                fileUploadSetup.ENDPOINT);

        for (ImageItem item : images) {

            ossHelper.uploadFile(ossHelper.ossClient,
                    item.path,
                    fileUploadSetup.childDir,
                    fileUploadSetup.BUCKETNAME,
                    new OssHelper.CallBack() {
                        @Override
                        public void success() {
                        }

                        @Override
                        public void success(String remote, String filename) {
                            System.out.println("@@@ remote : " + remote);
                            System.out.println("@@@ filename : " + filename);
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
                    System.out.println("@@@ upload to callback" + remoteUrls);
                    gameCallback(setReply(null, json));
                }
            } else {
                Toast.makeText(this, "upload failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessageAboveL)
                return;
            onActivityResultAboveL(requestCode, resultCode, data);
        } else if (requestCode == PICK_IMAGE_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
            System.out.println("@@@ upload list : " + images);
            if (images == null || images.isEmpty()) {
                gameCallback(setReply("-1:no select file", null));
                return;
            }
            gameUpload(images);
        } else if (resultCode == Activity.RESULT_OK) {
            UserInfo userInfo = data.getParcelableExtra("userInfo");
            //ChannelInfo channelInfo = data.getParcelableExtra("channelInfo");
            GroupInfo groupInfo = data.getParcelableExtra("groupInfo");
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

            if (globalCallback != null) {
                LitappActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:" + globalCallback + "(" + result.toString() + ")");
                    }
                });

            }
        } else if (resultCode == 301) {
            // 支付完成，返回给小程序
            String payResult = data.getStringExtra("payResult");
            System.out.println("@@@@ payResult : " + payResult);
            if (globalCallback != null) {
                System.out.println("@@@@ callback : " + globalCallback);
                LitappActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:" + globalCallback + "(" + payResult + ")");
                    }
                });

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
        Log.d(TAG, "onActivityResultAboveL: " + Arrays.toString(results));
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }

    @Override
    public void onBackPressed() {
        /*if (mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }
        if (!isSysExit) {
            isSysExit = true;

            Toast.makeText(this, R.string.exit_tip, Toast.LENGTH_SHORT).show();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isSysExit = false;
                }
            }, 2000);
        } else {
            finish();
        }*/
    }

    private void AddRight(String osnID) {
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String cache = sp.getString("litappAccess", null);
        JSONArray json = null;
        if (cache != null && !cache.isEmpty()) {
            try {
                json = JSON.parseArray(cache);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (json == null)
            json = new JSONArray();
        json.add(osnID);
        sp.edit().putString("litappAccess", json.toString()).apply();
    }

    private boolean CheckRight(String osnID) {
        boolean hasRight = false;
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String cache = sp.getString("litappAccess", null);
        if (cache != null && !cache.isEmpty()) {
            try {
                JSONArray json = JSON.parseArray(cache);
                for (Object o : json) {
                    if (osnID.equalsIgnoreCase((String) o)) {
                        hasRight = true;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hasRight;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clear_cache) {
            mWebView.clearCache(true);
        } else if (item.getItemId() == R.id.litapp_qrcode) {
            if (litappInfo != null) {
                String qrCodeValue = WfcScheme.QR_CODE_PREFIX_LITAPP + litappInfo.target;
                startActivity(QRCodeActivity.buildQRCodeIntent(this, WfcUIKit.getString(R.string.qrcode), litappInfo.portrait, qrCodeValue, litappInfo.target));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.litapp, menu);
    }
    /*@Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.clear_cache) {
            mWebView.clearCache(true);
        } else if (itemId == R.id.litapp_qrcode) {
            if (litappInfo != null) {
                String qrCodeValue = WfcScheme.QR_CODE_PREFIX_LITAPP + litappInfo.target;
                startActivity(QRCodeActivity.buildQRCodeIntent(this, WfcUIKit.getString(R.string.qrcode), litappInfo.portrait, qrCodeValue, litappInfo.target));
            }
        }
        return true;
    }*/

    /**
     * 全屏容器界面
     */
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

    /**
     * 视频播放全屏
     **/
    private void showCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        // if a view already exists then immediately terminate the new one
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }

        LitappActivity.this.getWindow().getDecorView();

        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        fullscreenContainer = new FullscreenHolder(LitappActivity.this);
        fullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        decor.addView(fullscreenContainer, COVER_SCREEN_PARAMS);
        customView = view;
        setStatusBarVisibility(false);
        customViewCallback = callback;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    /**
     * 隐藏视频全屏
     */
    private void hideCustomView() {
        if (customView == null) {
            return;
        }

        setStatusBarVisibility(true);
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        decor.removeView(fullscreenContainer);
        fullscreenContainer = null;
        customView = null;
        customViewCallback.onCustomViewHidden();
        mWebView.setVisibility(View.VISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void setStatusBarVisibility(boolean visible) {
        int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (customView != null) {
                hideCustomView();
            } else if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                //    finish();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration config) {
        super.onConfigurationChanged(config);
        switch (config.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                System.out.println("@@@   监听");
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                break;
        }
    }

    JSONObject setReply(String error, JSONObject json) {
        if (json == null)
            json = new JSONObject();
        json.put("errCode", error == null ? "0:success" : error);
        Log.d(TAG, "reply: " + json.toString());
        return json;
    }

    private String globalCallback = null;

    JSONObject runJsCommand(JSONObject json, String callback) {
        globalCallback = callback;
        AtomicReference<JSONObject> data = new AtomicReference<>();
        try {
            System.out.println( "@@@     litapp command : " + json.getString("command"));
            System.out.println("@@@ 传递过来的参数是："+ json);
            switch (json.getString("command")) {
                case "Login":
                    //Log.d(TAG, "Login to url: " + json.getString("url"));
                    String url = json.getString("url");
                    if (url == null)
                        url = litappInfo.url;
                    System.out.println("@@@ 123");
                    ChatManager.Instance().ltbLogin(litappInfo, url, new GeneralCallback2() {
                        @Override
                        public void onSuccess(String sessionKey) {
                            runOnUiThread(() -> {
                                isLogin = true;
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
                    System.out.println("@@@     GetUserInfo");
                    if (!isLogin) {
                        data.set(setReply("-1:need login", null));
                        if (callback == null)
                            return data.get();
                        mWebView.loadUrl("javascript:" + callback + "(" + data.get().toString() + ")");
                        break;
                    }
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
                    if (!isLogin) {
                        data.set(setReply("-1:need login", null));
                        if (callback == null)
                            return data.get();
                        mWebView.loadUrl("javascript:" + callback + "(" + data.get().toString() + ")");
                        break;
                    }
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
                    //NullUserInfo(json.getString("userID"));
                    Intent intent = new Intent(mWebView.getContext(), UserInfoActivity.class);
                    intent.putExtra("userInfo", userInfo);
                    startActivity(intent);
                    break;
                case "GroupInfo":
                    Intent intent1 = new Intent(mWebView.getContext(), GroupInfoActivity.class);
                    intent1.putExtra("groupId", json.getString("osnID"));
                    startActivity(intent1);
                    break;
                case "AddWallet":
                    String wallet = json.getString("DappInfo");
                    Intent intent2 = new Intent(LitappActivity.this, AddSqliteActivity.class);
                    intent2.putExtra("wallet", wallet);
                    startActivity(intent2);
                    break;

                case "OrderPaymentWeb":

                    /** data 数据
                     * walletId:
                     * payTo:
                     * coinType:
                     * amount:
                     * timestamp:
                     * memo:{
                     *     payId:
                     *     orderId:
                     *     description:
                     * }
                     * sign:使用小程序id进行验签
                     * **/
                    orderPaymentWeb(json);


                    break;

                case "OrderPayment":
                    /** data 数据
                     * walletName:
                     * walletId:
                     * coinType:
                     * payId:
                     * amount:
                     * timestamp:
                     * orderId:
                     * memo:{
                     *     description:
                     * }
                     * sign:使用小程序id进行验签
                     * **/

                    JSONObject payData = json.getJSONObject("data");
                    String walletId = payData.getString("walletId");

                    // 1 验证数据正确性
                    if (checkOrderPaymentParam(payData, litappInfo.target)) {
                        //System.out.println("@@@@ 1");
                        // 2 获取wallet信息
                        String walletInfo = ChatManager.Instance().getWallet(walletId);
                        //System.out.println("@@@@ walletInfo:" + walletInfo);
                        if (walletInfo != null) {
                            // 3 打开一个新的dapp，传递参数
                            try {

                                JSONObject walletJson = JSONObject.parseObject(walletInfo);
                                LitappInfo walletDappInfo = new LitappInfo();
                                walletDappInfo.target = walletJson.getString("target");
                                walletDappInfo.info = walletJson.getString("info");
                                walletDappInfo.url = walletJson.getString("url");
                                walletDappInfo.param = walletJson.getString("param");
                                //System.out.println("@@@@ wallet url  : " + walletDappInfo.url);
                                String urlParam = null;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    urlParam = Base64.getUrlEncoder().encodeToString(payData.toString().getBytes());
                                }
                                JSONObject urlParamJson = new JSONObject();
                                urlParamJson.put("payType", "OrderPayment");
                                urlParamJson.put("param", urlParam);
                                walletDappInfo.urlParam = urlParamJson.toString();

                                //System.out.println("@@@@ wallet : " + walletInfo);
                                //System.out.println("@@@@ urlParam : " + walletDappInfo.urlParam);
                                //System.out.println("@@@@ urlParam size : " + walletDappInfo.urlParam.length());

                                Intent intent5 = new Intent(LitappActivity.this, DappActivity2.class);
                                intent5.putExtra("litappInfo", walletDappInfo);
                                startActivityForResult(intent5, 301);

                            } catch (Exception e) {

                            }

                        } else {
                            Toast.makeText(this, "You have not added this wallet.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "The payment data is incorrect.", Toast.LENGTH_LONG).show();
                    }


                    break;

                case "OrderPaymentResult":


                    String result = json.getString("result");
                    System.out.println("@@@@ DappActivity OrderPaymentResult : " + result);
                    Intent intent3 = getIntent();
                    intent3.putExtra("payResult", result);
                    setResult(301, intent3);
                    finish();

                    break;

                case "OpenDapp":
                    try {
                        JSONObject dappJson = json.getJSONObject("DappInfo");
                        LitappInfo litappInfo = new LitappInfo();
                        litappInfo.target = dappJson.getString("target");
                        litappInfo.info = dappJson.getString("info");
                        litappInfo.url = dappJson.getString("url");

                        Intent intent4 = new Intent(LitappActivity.this, DappActivity2.class);
                        intent4.putExtra("litappInfo", litappInfo);
                        startActivity(intent4);

                    } catch (Exception e) {

                    }

                    break;

                case "Share":


                    /*JSONObject dappJson2 = json.getJSONObject("DappInfo");
                    if (dappJson2 != null) {
                        LitappInfo dapp = new LitappInfo(dappJson2);

                        Intent intent6 = new Intent(LitappActivity.this, ContactListActivity.class);
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

                        Intent intent6 = new Intent(LitappActivity.this, ForwardActivity.class);
                        intent6.putExtra("message", msg);
                        startActivity(intent6);

                    }

                    break;

                case "AddObj":
                    Intent intent4 = new Intent(LitappActivity.this, ContactListActivity.class);
                    ArrayList<String> filterUserList = new ArrayList<>();
                    intent4.putExtra("pick", true);
                    intent4.putExtra("share", true);
                    intent4.putExtra(ContactListActivity.FILTER_USER_LIST, filterUserList);
                    startActivityForResult(intent4, 100);
                    break;

                case "SetNft":
                    AlertDialog.Builder dialog2 = new AlertDialog.Builder(LitappActivity.this);
                    dialog2.setMessage("是否同意将该NFT设置为头像");
                    dialog2.setCancelable(false);
                    dialog2.setPositiveButton(getString(cn.wildfire.chat.kit.R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                String portrait = json.getString("portrait");
                                String url2 = json.getString("url");

                                ChatManager.Instance().getWorkHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        List<ModifyMyInfoEntry> list = new ArrayList<>();
                                        ModifyMyInfoEntry info = new ModifyMyInfoEntry(Modify_Portrait, portrait);
                                        list.add(info);
                                        ChatManager.Instance().modifyMyInfo(list, new GeneralCallback() {
                                            @Override
                                            public void onSuccess() {
                                                ChatManager.Instance().upDescribes("nft", url2, null, new GeneralCallback2() {
                                                    @Override
                                                    public void onSuccess(String result) {
                                                        Toast.makeText(LitappActivity.this, getString(R.string.set_ok), Toast.LENGTH_SHORT).show();
                                                    }

                                                    @Override
                                                    public void onFail(int errorCode) {
                                                        Toast.makeText(LitappActivity.this, getString(R.string.operation_failure), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFail(int errorCode) {
                                                Toast.makeText(LitappActivity.this, getString(R.string.operation_failure), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            } catch (Exception e) {

                            }
                        }
                    });
                    dialog2.setNegativeButton(getString(cn.wildfire.chat.kit.R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog2.show();
                        }
                    });

                    break;

                case "gameUpload":
                    System.out.println("@@@ gameUpload : " + json);

                    fileUploadSetup = new FileUploadSetup(json);

                    ImagePicker.picker().enableMultiMode(1).pick(this, PICK_IMAGE_RESULT_CODE);
                    break;
                case "PaySign":
                    JSONObject result2 = paySign(json, litappInfo.target);
                    System.out.println("@@@    小程序的json  ：" + json.toString());
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data.get();
    }

    private void orderPaymentWeb(JSONObject json) {

        /** data 数据
         * walletId:
         * payTo:
         * coinType:
         * amount:
         * memo:{
         *     orderId:
         *     description:
         * }
         * sign:使用小程序id进行验签
         * **/

        String walletId = json.getString("walletId");

        // 获取钱包信息
        String walletInfo = ChatManager.Instance().getWallet(walletId);
        System.out.println("@@@            walletInfo:  "+walletInfo);
        if(walletInfo == null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LitappActivity.this,"请先添加钱包",Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        JSONObject walletJson = JSONObject.parseObject(walletInfo);
        LitappInfo dappInfo = new LitappInfo(walletJson);
        // 组装urlParam
        String payTo = json.getString("payTo");
        String coinType = json.getString("coinType");
        long amount = json.getLongValue("amount");
        String memo = json.getString("memo");

        JSONObject urlParamJson = new JSONObject();
        urlParamJson.put("payTo", payTo);
        urlParamJson.put("coinType", coinType);
        urlParamJson.put("amount", amount);
        urlParamJson.put("memo", memo);
        urlParamJson.put("type", "exchange");
        dappInfo.urlParam = urlParamJson.toString();

        // 打开dapp
        System.out.println("@@@         dappInfo :"+dappInfo);
        Intent intent = new Intent(LitappActivity.this, DappActivity2.class);
        intent.putExtra("litappInfo", dappInfo);
        startActivityForResult(intent,301);
    //    finish();
    }

    private String needPayPassword = "";
    private String needShadowSign = "";
    private String decPassword = "";
    private String payPassword = "";

    private JSONObject paySign(JSONObject json2, String dappId) {

        needPayPassword = "";
        needShadowSign = "";
        decPassword = "";
        payPassword = "";

        JSONObject payData = json2.getJSONObject("data");
        needPayPassword = json2.getString("needPayPassword");
        needShadowSign = json2.getString("needShadowSign");
        if (needPayPassword == null) {
            needPayPassword = "";
        }
        if (needShadowSign == null) {
            needShadowSign = "";
        }

        // activitiy

        View views = View.inflate(LitappActivity.this, R.layout.dialog_show_info, null);
        Button btn_cancel = views.findViewById(R.id.btn_cancel);
        Button btn_confirm = views.findViewById(R.id.btn_confirm);
        TextView tv_type = views.findViewById(R.id.tv_type);
        TextView tv_from = views.findViewById(R.id.tv_from);
        TextView tv_to = views.findViewById(R.id.tv_to);
        TextView tv_walletId = views.findViewById(R.id.tv_walletId);
        TextView tv_balance = views.findViewById(R.id.tv_balance);
        TextView tv_serial = views.findViewById(R.id.tv_serial);
        TextView tv_greetings = views.findViewById(R.id.tv_greetings);
        TextView tv_coin = views.findViewById(R.id.tv_coin);
        AlertDialog.Builder builder = new AlertDialog.Builder(LitappActivity.this);
        builder.setView(views);
        builder.setCancelable(true);

        // 解析data
        String mySelf = ChatManager.Instance().getUserId();
        JSONObject data = new JSONObject();
        data.put("type", payData.getString("type"));
        data.put("from", payData.getString("from"));
        data.put("to", payData.getString("to"));
        data.put("walletId", payData.getString("walletId"));
        data.put("balance", payData.getString("amount"));
        data.put("timestamp", System.currentTimeMillis());
        data.put("serial", payData.getLongValue("serial") + 1);
        data.put("greetings", payData.getString("remark"));
        data.put("coin", payData.getString("coin"));

        tv_type.setText(payData.getString("type"));
        tv_from.setText(payData.getString("from"));
        tv_to.setText(payData.getString("to"));
        tv_walletId.setText(payData.getString("walletId"));
        tv_balance.setText(payData.getString("amount"));
        tv_serial.setText(payData.getString("serial"));
        tv_greetings.setText(payData.getString("remark"));
        tv_coin.setText(payData.getString("coin"));

        dialog = builder.create();
        //    dialog.show();
        System.out.println("@@@  serial1   :" + payData.getLongValue("serial") + "");

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                System.out.println("@@@ dapp result : null");
                if (globalCallback != null) {
                    LitappActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mWebView.loadUrl("javascript:" + globalCallback + "(" + "" + ")");
                        }
                    });

                }
            }
        });
        String finalNeedShadowSign = needShadowSign;
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();

                //输入解密密码
                if (needShadowSign.equalsIgnoreCase("yes")) {
                    View views2 = View.inflate(LitappActivity.this, R.layout.dialog_keyword2, null);
                    Button btn_cancel2 = views2.findViewById(R.id.btn_cancel);
                    Button btn_confirm2 = views2.findViewById(R.id.btn_confirm);
                    EditText edt_keywords2 = views2.findViewById(R.id.edt_keywords);

                    AlertDialog.Builder builder2 = new AlertDialog.Builder(LitappActivity.this);
                    builder2.setView(views2);
                    builder2.setCancelable(true);

                    dialog2 = builder2.create();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog2.show();
                        }
                    });


                    btn_cancel2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog2.dismiss();
                            System.out.println("@@@ dapp result : null");
                            if (globalCallback != null) {
                                LitappActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mWebView.loadUrl("javascript:" + globalCallback + "(" + "" + ")");
                                    }
                                });

                            }
                        }
                    });
                    btn_confirm2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog2.dismiss();
                            decPassword = edt_keywords2.getText().toString();

                            // 输入交易密码
                            if (needPayPassword.equalsIgnoreCase("yes")) {

                                View views1 = View.inflate(LitappActivity.this, R.layout.dialog_keyword3, null);
                                Button btn_cancel1 = views1.findViewById(R.id.btn_cancel);
                                Button btn_confirm1 = views1.findViewById(R.id.btn_confirm);
                                EditText edt_keywords1 = views1.findViewById(R.id.edt_keywords);


                                AlertDialog.Builder builder1 = new AlertDialog.Builder(LitappActivity.this);
                                builder1.setView(views1);
                                builder1.setCancelable(true);
                                dialog1 = builder1.create();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog1.show();
                                    }
                                });


                                btn_cancel1.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog1.dismiss();
                                    }
                                });
                                btn_confirm1.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog1.dismiss();
                                        payPassword = edt_keywords1.getText().toString();
                                        // 两个密码都有
                                        // 处理逻辑
                                        paySign2(payData);
                                    }
                                });
                            } else {
                                // 只有解密密码
                                // 处理逻辑
                                paySign2(payData);
                            }
                        }
                    });
                } else {
                    // 需要交易密码
                    if (needPayPassword.equalsIgnoreCase("yes")) {

                        View views1 = View.inflate(LitappActivity.this, R.layout.dialog_keyword3, null);
                        Button btn_cancel1 = views1.findViewById(R.id.btn_cancel);
                        Button btn_confirm1 = views1.findViewById(R.id.btn_confirm);
                        EditText edt_keywords1 = views1.findViewById(R.id.edt_keywords);


                        AlertDialog.Builder builder1 = new AlertDialog.Builder(LitappActivity.this);
                        builder1.setView(views1);
                        builder1.setCancelable(true);
                        dialog1 = builder1.create();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog1.show();
                            }
                        });


                        btn_cancel1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog1.dismiss();
                                System.out.println("@@@ dapp result : null");
                                if (globalCallback != null) {
                                    LitappActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //                           mWebView.loadUrl("javascript:" + globalCallback + "(" + "" + ")");
                                        }
                                    });

                                }
                            }
                        });
                        btn_confirm1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog1.dismiss();
                                payPassword = edt_keywords1.getText().toString();
                                // 只有交易密码
                                // 处理逻辑
                                paySign2(payData);
                            }
                        });
                    } else {
                        // 什么都不需要
                        // 处理逻辑
                        paySign2(payData);
                    }

                }
            }
        });

        //输入解密密码
        if (needShadowSign.equalsIgnoreCase("yes")) {
            View views2 = View.inflate(LitappActivity.this, R.layout.dialog_keyword2, null);
            Button btn_cancel2 = views2.findViewById(R.id.btn_cancel);
            Button btn_confirm2 = views2.findViewById(R.id.btn_confirm);
            EditText edt_keywords2 = views2.findViewById(R.id.edt_keywords);

            AlertDialog.Builder builder2 = new AlertDialog.Builder(LitappActivity.this);
            builder2.setView(views2);
            builder2.setCancelable(true);

            dialog2 = builder2.create();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog2.show();
                }
            });


            btn_cancel2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog2.dismiss();
                    System.out.println("@@@ dapp result : null");
                    if (globalCallback != null) {
                        LitappActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mWebView.loadUrl("javascript:" + globalCallback + "(" + "" + ")");
                            }
                        });

                    }
                }
            });
            btn_confirm2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog2.dismiss();
                    decPassword = edt_keywords2.getText().toString();

                    // 输入交易密码
                    if (needPayPassword.equalsIgnoreCase("yes")) {

                        View views1 = View.inflate(LitappActivity.this, R.layout.dialog_keyword3, null);
                        Button btn_cancel1 = views1.findViewById(R.id.btn_cancel);
                        Button btn_confirm1 = views1.findViewById(R.id.btn_confirm);
                        EditText edt_keywords1 = views1.findViewById(R.id.edt_keywords);


                        AlertDialog.Builder builder1 = new AlertDialog.Builder(LitappActivity.this);
                        builder1.setView(views1);
                        builder1.setCancelable(true);
                        dialog1 = builder1.create();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog1.show();
                            }
                        });


                        btn_cancel1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog1.dismiss();
                            }
                        });
                        btn_confirm1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog1.dismiss();
                                payPassword = edt_keywords1.getText().toString();
                                // 两个密码都有
                                // 处理逻辑
                                paySign2(payData);
                            }
                        });
                    } else {
                        // 只有解密密码
                        // 处理逻辑
                        paySign2(payData);
                    }
                }
            });
        } else {
            // 需要交易密码
            if (needPayPassword.equalsIgnoreCase("yes")) {

                View views1 = View.inflate(LitappActivity.this, R.layout.dialog_keyword3, null);
                Button btn_cancel1 = views1.findViewById(R.id.btn_cancel);
                Button btn_confirm1 = views1.findViewById(R.id.btn_confirm);
                EditText edt_keywords1 = views1.findViewById(R.id.edt_keywords);


                AlertDialog.Builder builder1 = new AlertDialog.Builder(LitappActivity.this);
                builder1.setView(views1);
                builder1.setCancelable(true);
                dialog1 = builder1.create();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog1.show();
                    }
                });


                btn_cancel1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog1.dismiss();
                        System.out.println("@@@ dapp result : null");
                        if (globalCallback != null) {
                            LitappActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //        mWebView.loadUrl("javascript:" + globalCallback + "(" + "" + ")");
                                }
                            });

                        }
                    }
                });
                btn_confirm1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog1.dismiss();
                        payPassword = edt_keywords1.getText().toString();
                        // 只有交易密码
                        // 处理逻辑
                        paySign2(payData);
                    }
                });
            } else {
                // 什么都不需要
                // 处理逻辑
                paySign2(payData);
            }

        }

        return null;
    }

    private void paySign2(JSONObject data) {

        System.out.println("@@@   paySign2   serial :" + data.getLongValue("serial"));
        long serial = data.getLongValue("serial") + 1;
        System.out.println("@@@   paySign2 long  serial :" + serial);
        data.put("serial", serial);

        String mySelf = ChatManager.Instance().getUserId();
        String calc = data.getString("type")
                + data.getString("from")
                + data.getString("to")
                + data.getString("walletId")
                + data.getString("balance")
                + data.getString("timestamp")
                + data.getString("serial")
                + data.getString("greetings")
                + data.getString("coin");

        System.out.println("@@@  serial2   :" + calc);

        String hash = ChatManager.Instance().hashData(calc.getBytes());
        String sign = ChatManager.Instance().signData(hash.getBytes());
        data.put("sign", sign);

        if (!decPassword.equalsIgnoreCase("")) {
            String pubKey = ChatManager.Instance().getShadowKeyPubId(decPassword);
            System.out.println("@@@ dapp pubKey : " + pubKey);
            if (pubKey == null) {
                System.out.println("@@@ dapp pubKey null");
                return;
            }
            if (pubKey.length() == 0) {
                System.out.println("@@@ dapp pubKey null");
                return;
            }
            // 做错误提示
            String shadowSign = ChatManager.Instance().signByShadowKey(hash, decPassword);
            System.out.println("@@@ dapp shadowSign : " + shadowSign);
            if (shadowSign == null) {
                System.out.println("@@@ dapp shadowSign null");
                return;
            }
            if (shadowSign.length() == 0) {
                System.out.println("@@@ dapp shadowSign null");
                return;
            }

            data.put("shadowKey", pubKey);
            data.put("shadowSign", shadowSign);
        }
        //System.out.println("@@@ dapp payPassword : " + payPassword);
        if (!payPassword.equalsIgnoreCase("")) {
            JSONObject json = new JSONObject();
            json.put("command", "transfer");
            json.put("from", mySelf);

            String encrypted = CryptUtils.aesEncrypt(data.toString().getBytes(), payPassword);
            json.put("body", encrypted);
            String calc2 = json.getString("command") + json.getString("from") + json.getString("body");
            String hash2 = ChatManager.Instance().hashData(calc2.getBytes());
            String sign2 = ChatManager.Instance().signData(hash2.getBytes());
            json.put("hash", hash2);
            json.put("sign", sign2);
            json.put("language", WfcBaseActivity.getBaseLanguage());

            System.out.println("@@@ dapp json:" + json);
            if (globalCallback != null) {
                System.out.println("@@@     小程序链接 ： " + "javascript:" + globalCallback + "(" + json + ")");
                LitappActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:" + globalCallback + "(" + json + ")");
                    }
                });

            }
            return;
        }

        System.out.println("@@@ dapp data:" + data);
        if (globalCallback != null) {
            LitappActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:" + globalCallback + "(" + data + ")");
                }
            });

        }

    }

    @android.webkit.JavascriptInterface
    public String run(String args) {
        String result = null;
        Log.d(TAG, "run args: " + args);
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

    @Override
    protected void onDestroy() {
        mWebView.stopLoading();
        mWebView.destroy();
        super.onDestroy();
    }
}
