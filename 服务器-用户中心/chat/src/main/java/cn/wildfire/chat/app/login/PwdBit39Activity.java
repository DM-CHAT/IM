package cn.wildfire.chat.app.login;

import static cn.wildfirechat.client.ConnectionStatus.ConnectionStatusConnected;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Looper;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.wildfire.chat.app.AppService;
import cn.wildfire.chat.app.FileUtil;
import cn.wildfire.chat.app.main.MainActivity;
import cn.wildfire.chat.app.main.MainActivity1;
import cn.wildfire.chat.app.setting.BaiBeiWalletUtils;
import cn.wildfire.chat.app.utils.KeyStore;
import cn.wildfire.chat.app.utils.OsnUtils;
import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.remote.ChatManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PwdBit39Activity extends AppCompatActivity {

    private EditText edt_pwd1,edt_pwd2,edt_accout;
    private Button btn_confirm;
    private String osnId,PREFIX,osnPassword;
    private String str_MainDapp = null;
    private String password2 = "";
    private String account;
    private String mnemonic;
    private String mnemonicEnc;
    private String mnemonicLoginUrl;
    final Object mLock = new Object();
    private String[] fromMnemonic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pwdbip39);
        mnemonic = getIntent().getStringExtra("mnemonic");
        mnemonicLoginUrl = getIntent().getStringExtra("mnemonicLoginUrl");
        PREFIX = (String) SPUtils.get(PwdBit39Activity.this,"PREFIX","");

        initView();
    }

    private void initView(){
        edt_pwd1 = findViewById(R.id.edt_pwd1);
        edt_pwd2 = findViewById(R.id.edt_pwd2);
        edt_accout = findViewById(R.id.edt_accout);
        btn_confirm = findViewById(R.id.btn_confirm);

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 检查一下account文件是否存在，存在则提示用户，用户已经存在，请重新输入

                account = edt_accout.getText().toString();

                if (KeyStore.isKeyExist(PwdBit39Activity.this, account)) {
                    Toast.makeText(PwdBit39Activity.this,"账号名已存在,请重新输入",Toast.LENGTH_SHORT).show();
                    return ;
                }

                String pwd1 = edt_pwd1.getText().toString();
                String pwd2 = edt_pwd2.getText().toString();

                if(account.equals("") || account == null){
                    Toast.makeText(PwdBit39Activity.this,"accout不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(pwd1.equals("") || pwd1 == null){
                    Toast.makeText(PwdBit39Activity.this,"密码不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(pwd2.equals("") || pwd2 == null){
                    Toast.makeText(PwdBit39Activity.this,"密码不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!pwd1.equals(pwd2)){
                    Toast.makeText(PwdBit39Activity.this,"两次密码不一样",Toast.LENGTH_SHORT).show();
                    return;
                }

                password2 = pwd2;

                if (mnemonic == null) {
                    //mnemonic = BaiBeiWalletUtils.generateBip39();
                }


                mnemonicEnc = OsnUtils.aesEncrypt(mnemonic, password2);
                fromMnemonic = BaiBeiWalletUtils.sha256toOsnPrivateKey(mnemonic,pwd2);
                osnId = fromMnemonic[0];
                osnPassword = "VER2" + "-" +fromMnemonic[1] + "-" + fromMnemonic[2];

                OkHttpClient okHttpClient = new OkHttpClient();
                JSONObject requestData = new JSONObject();
                String json = "";
                try {
                    requestData.put("user",osnId);
                    json = requestData.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                        , json);

                final Request request = new Request.Builder()
                        .url(mnemonicLoginUrl)
                        .post(requestBody)
                        .build();
                Call call = okHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                        // 存数据
                        /*String account = "test";
                        account;  acount作为文件名
                        osnId
                        osnPassword*/
                        JSONObject json = new JSONObject();
                        json.put("osnId", osnId);
                        json.put("osnPassword", osnPassword);
                        json.put("mnemonic", mnemonicEnc);

                        KeyStore.createKey(PwdBit39Activity.this, account, json);


                        String result = response.body().string();
                        int code = getResponseCode(result);
                        if (code != 200) {
                            String msg = getResponseMsg(result);
                            String error = "code:"+code + " message:" + msg;
                            Looper.prepare();
                            Toast.makeText(PwdBit39Activity.this, error, Toast.LENGTH_SHORT).show();
                            Looper.loop();
                            return;
                        }
                        JSONObject data = getResponseData(result);

                        SPUtils.put(PwdBit39Activity.this,"account",account);

                        login(data);

                    }
                });
            }
        });
    }

    private int getResponseCode(String response) {
        try {
            JSONObject json = JSONObject.parseObject(response);
            return json.getIntValue("code");
        } catch (Exception e) {

        }
        return -1;
    }

    private String getResponseMsg(String response) {
        try {
            JSONObject json = JSONObject.parseObject(response);
            return json.getString("msg");
        } catch (Exception e) {

        }
        return "";
    }

    private JSONObject getResponseData(String response) {
        try {
            JSONObject json = JSONObject.parseObject(response);
            return json.getJSONObject("data");
        } catch (Exception e) {

        }
        return null;
    }

    private void login(JSONObject data){

        //JSONObject data = data1.getJSONObject("data");
        String ip = data.getString("osn_node");
        String token = data.getString("token");
        String language = data.getString("language");
        JSONObject jsonObject = data.getJSONObject("json");
        if (jsonObject == null) {
            jsonObject = new JSONObject();
        }

        //JSONObject jsonObject = JSONObject.parseObject(json);
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

        if (APP_DEVICE != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"APP_DEVICE",APP_DEVICE);
        if (AccessKeySecret != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"AccessKeySecret",AccessKeySecret);
        if (GROUP_PROFIT_URL != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"GROUP_PROFIT_URL",GROUP_PROFIT_URL);
        if (BOMB_ROLE_URL != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"BOMB_ROLE_URL",BOMB_ROLE_URL);
        if (RemoteTempFilePath != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"RemoteTempFilePath",RemoteTempFilePath);
        if (TRANSFER_URL != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"TRANSFER_URL",TRANSFER_URL);
        if (GroupList != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"GroupList",GroupList);
        if (HOST_IP != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"HOST_IP",HOST_IP);
        if (GroupPortraitDirectory != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"GroupPortraitDirectory",GroupPortraitDirectory);
        if (WALLET_URL != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"WALLET_URL",WALLET_URL);
        if (APP_URL != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"APP_URL",APP_URL);
        if (SET_GROUP_URL != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"SET_GROUP_URL",SET_GROUP_URL);
        if (AddGroup != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"AddGroup",AddGroup);
        if (ACCOUNT_PREFIX_URL != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"ACCOUNT_PREFIX_URL",ACCOUNT_PREFIX_URL);
        if (QueryAplets != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"QueryAplets",QueryAplets);
        if (UserPortraitDirectory != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"UserPortraitDirectory",UserPortraitDirectory);
        if (GETTRANSFEREST_URL != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"GETTRANSFEREST_URL",GETTRANSFEREST_URL);
        if (GROUP_ZERO != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"GROUP_ZERO",GROUP_ZERO);
        if (KEFU_LIST != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"KEFU_LIST",KEFU_LIST);
        if (SET_NAME != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"SET_NAME",SET_NAME);
        if (TempDirectory != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"TempDirectory",TempDirectory);
        if (BombRole != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"BombRole",BombRole);
        if (AccessKeyId != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"AccessKeyId",AccessKeyId);
        if (ENDPOINT != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"ENDPOINT",ENDPOINT);
        if (BUCKETNAME != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"BUCKETNAME",BUCKETNAME);
        if (GROUP_UPDATE_COUNT != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"GROUP_UPDATE_COUNT",GROUP_UPDATE_COUNT);
        if (PREFIX != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"PREFIX",PREFIX);
        if(redPack != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"redPack",redPack);
        cn.wildfire.chat.app.SPUtils.remove(PwdBit39Activity.this, "voiceBaseUrl");
        cn.wildfire.chat.app.SPUtils.remove(PwdBit39Activity.this, "voiceHostUrl");
        ChatManager.Instance().removeVoiceSetup();
        if(voiceBaseUrl != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"voiceBaseUrl",voiceBaseUrl);
        if(voiceHostUrl != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"voiceHostUrl",voiceHostUrl);
        if(DEL_ACCOUNT_URL != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"DEL_ACCOUNT_URL",DEL_ACCOUNT_URL);
        if(create_url != null)
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"create_url",create_url);


        ChatManager.Instance().setVoiceBaseUrl(voiceBaseUrl);
        ChatManager.Instance().setVoiceHostUrl(voiceHostUrl);

        cn.wildfire.chat.app.SPUtils.remove(PwdBit39Activity.this, "MainDapp");
        try {
            ChatManager.Instance().setMainDapp("");
        } catch (Exception e) {
        }

        if (MainDapp != null) {
            try {
                str_MainDapp = new String(Base64.decode(MainDapp,0), "utf-8");
                cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"MainDapp",str_MainDapp);
                cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"ShowMain", "0");
                ChatManager.Instance().setMainDapp(str_MainDapp);
            } catch (Exception e) {
                cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"ShowMain", "1");
            }
        } else {
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"ShowMain", "1");
        }
        String Wallets = null;
        if(Wallet != null){
            try {
                Wallets = new String(Base64.decode(Wallet,0), "utf-8");
                cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"Wallet",Wallets);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            cn.wildfire.chat.app.SPUtils.remove(PwdBit39Activity.this,"Wallet");
        }



        View views = View.inflate(PwdBit39Activity.this, cn.wildfire.chat.kit.R.layout.dialog_keyword, null);
        Button btn_cancel = views.findViewById(cn.wildfire.chat.kit.R.id.btn_cancel);
        Button btn_confirm= views.findViewById(cn.wildfire.chat.kit.R.id.btn_confirm);
        EditText edt_keywords = views.findViewById(cn.wildfire.chat.kit.R.id.edt_keywords);
        AlertDialog.Builder builder = new AlertDialog.Builder(PwdBit39Activity.this);
        builder.setView(views);
        builder.setCancelable(true);



        intentActivity(ip,osnId,osnPassword,token,language,Alias);


    }

    private void intentActivity(String ip,String username,String password,String token,String language,String Alias){
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String oip = sp.getString("hostip", "");
        sp.edit().putString("hostip",ip).apply();
        AtomicBoolean loginResult = new AtomicBoolean(false);
        new Thread(()->{
            try {
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
            cn.wildfire.chat.app.SPUtils.put(PwdBit39Activity.this,"isNeedReset",isNeedReset);


            if(str_MainDapp==null){
                Intent intent = new Intent(PwdBit39Activity.this, MainActivity1.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("restart",restart);
                intent.putExtra("new_lang",new_lang);
                intent.putExtra("old_lang",old_lang);
                startActivity(intent);
                finish();
                return;
            }
            if(str_MainDapp.length() < 5){
                Intent intent = new Intent(PwdBit39Activity.this, MainActivity1.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("restart",restart);
                intent.putExtra("new_lang",new_lang);
                intent.putExtra("old_lang",old_lang);
                startActivity(intent);
                finish();
            }else{
                Intent intent = new Intent(PwdBit39Activity.this, MainActivity.class);
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
                    AppService.validateConfig(PwdBit39Activity.this);
                    Toast.makeText(PwdBit39Activity.this, getString(R.string.error_login_failed), Toast.LENGTH_SHORT).show();
                }
            });

            //    runOnUiThread(() -> Toast.makeText(PwdBit39Activity.this, getString(R.string.error_login_failed), Toast.LENGTH_SHORT).show());
        }
    }
}
