package cn.wildfire.chat.app.login;

import static cn.wildfirechat.client.ConnectionStatus.ConnectionStatusConnected;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.wildfire.chat.app.AppService;
import cn.wildfire.chat.app.SPUtils;
import cn.wildfire.chat.app.main.MainActivity;
import cn.wildfire.chat.app.main.MainActivity1;
import cn.wildfire.chat.app.utils.KeyStore;
import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfire.chat.kit.WfcBaseActivity;
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

public class AccountListAdapter extends RecyclerView.Adapter<AccountListAdapter.ViewHoldere> {

    private Context context;
    private List<String> list;
    private Dialog dialog;
    private String password2 = "";
    private String account;
    private String mnemonicEnd;
    private String[] fromMnemonic;
    private String osnId,osnPassword;
    private String str_MainDapp = null;
    final Object mLock = new Object();
    private AccountListActivity accountListActivity;

    public AccountListAdapter(Context context, List<String> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHoldere onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_accountlist,parent,false);
        ViewHoldere viewHoldere = new ViewHoldere(view);
        return viewHoldere;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHoldere holder, int position) {
        holder.tv_account.setText(list.get(position));
        holder.rl_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View views = View.inflate(context, cn.wildfire.chat.kit.R.layout.dialog_keyword, null);
                Button btn_cancel = views.findViewById(cn.wildfire.chat.kit.R.id.btn_cancel);
                Button btn_confirm= views.findViewById(cn.wildfire.chat.kit.R.id.btn_confirm);
                EditText edt_keywords = views.findViewById(cn.wildfire.chat.kit.R.id.edt_keywords);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(views);
                builder.setCancelable(true);

                dialog = builder.create();
                dialog.show();
                if(AccountListActivity.class.isInstance(context)){
                    accountListActivity = (AccountListActivity) context;
                }


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

                            String mnemonicTemp = KeyStore.getMnemonic(context, list.get(position), password2);
                            if (mnemonicTemp == null ){
                                Toast.makeText(context, "密码错误", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            JSONObject jsonObject = KeyStore.getKeyInfo(context, list.get(position));


                            if(jsonObject == null){
                                Toast.makeText(context, "密码错误", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            osnId = jsonObject.getString("osnId");
                            osnPassword = jsonObject.getString("osnPassword");

                            String mnemonicLoginUrl = (String) SPUtils.get(context,"mnemonicLoginUrl","");
                            if (mnemonicLoginUrl.length()<5){
                                return;
                            }

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
                                    System.out.println("@@@          登录失败="+e.getMessage());
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                                    String result = response.body().string();
                                    System.out.println("@@@          登录成功="+result);
                                    SPUtils.put(context,"account",list.get(position));
                                    JSONObject jsonObject = JSONObject.parseObject(result);
                                    login(jsonObject);

                                }
                            });
                        } else {
                            dialog.dismiss();
                            Toast.makeText(context, "输入内容不能为空", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHoldere extends RecyclerView.ViewHolder{

        TextView tv_account;
        RelativeLayout rl_item;
        
        public ViewHoldere(@NonNull View itemView) {
            super(itemView);
            tv_account = itemView.findViewById(R.id.tv_account);
            rl_item = itemView.findViewById(R.id.rl_item);
        }
    }

    private void login(JSONObject data1){

        JSONObject data = data1.getJSONObject("data");
        String ip = data.getString("osn_node");
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

        if (APP_DEVICE != null)
            SPUtils.put(context,"APP_DEVICE",APP_DEVICE);
        if (AccessKeySecret != null)
            SPUtils.put(context,"AccessKeySecret",AccessKeySecret);
        if (GROUP_PROFIT_URL != null)
            SPUtils.put(context,"GROUP_PROFIT_URL",GROUP_PROFIT_URL);
        if (BOMB_ROLE_URL != null)
            SPUtils.put(context,"BOMB_ROLE_URL",BOMB_ROLE_URL);
        if (RemoteTempFilePath != null)
            SPUtils.put(context,"RemoteTempFilePath",RemoteTempFilePath);
        if (TRANSFER_URL != null)
            SPUtils.put(context,"TRANSFER_URL",TRANSFER_URL);
        if (GroupList != null)
            SPUtils.put(context,"GroupList",GroupList);
        if (HOST_IP != null)
            SPUtils.put(context,"HOST_IP",HOST_IP);
        if (GroupPortraitDirectory != null)
            SPUtils.put(context,"GroupPortraitDirectory",GroupPortraitDirectory);
        if (WALLET_URL != null)
            SPUtils.put(context,"WALLET_URL",WALLET_URL);
        if (APP_URL != null)
            SPUtils.put(context,"APP_URL",APP_URL);
        if (SET_GROUP_URL != null)
            SPUtils.put(context,"SET_GROUP_URL",SET_GROUP_URL);
        if (AddGroup != null)
            SPUtils.put(context,"AddGroup",AddGroup);
        if (ACCOUNT_PREFIX_URL != null)
            SPUtils.put(context,"ACCOUNT_PREFIX_URL",ACCOUNT_PREFIX_URL);
        if (QueryAplets != null)
            SPUtils.put(context,"QueryAplets",QueryAplets);
        if (UserPortraitDirectory != null)
            SPUtils.put(context,"UserPortraitDirectory",UserPortraitDirectory);
        if (GETTRANSFEREST_URL != null)
            SPUtils.put(context,"GETTRANSFEREST_URL",GETTRANSFEREST_URL);
        if (GROUP_ZERO != null)
            SPUtils.put(context,"GROUP_ZERO",GROUP_ZERO);
        if (KEFU_LIST != null)
            SPUtils.put(context,"KEFU_LIST",KEFU_LIST);
        if (SET_NAME != null)
            SPUtils.put(context,"SET_NAME",SET_NAME);
        if (TempDirectory != null)
            SPUtils.put(context,"TempDirectory",TempDirectory);
        if (BombRole != null)
            SPUtils.put(context,"BombRole",BombRole);
        if (AccessKeyId != null)
            SPUtils.put(context,"AccessKeyId",AccessKeyId);
        if (ENDPOINT != null)
            SPUtils.put(context,"ENDPOINT",ENDPOINT);
        if (BUCKETNAME != null)
            SPUtils.put(context,"BUCKETNAME",BUCKETNAME);
        if (GROUP_UPDATE_COUNT != null)
            SPUtils.put(context,"GROUP_UPDATE_COUNT",GROUP_UPDATE_COUNT);
        if (PREFIX != null)
            SPUtils.put(context,"PREFIX",PREFIX);
        if(redPack != null)
            SPUtils.put(context,"redPack",redPack);
        SPUtils.remove(context, "voiceBaseUrl");
        SPUtils.remove(context, "voiceHostUrl");
        ChatManager.Instance().removeVoiceSetup();
        if(voiceBaseUrl != null)
            SPUtils.put(context,"voiceBaseUrl",voiceBaseUrl);
        if(voiceHostUrl != null)
            SPUtils.put(context,"voiceHostUrl",voiceHostUrl);
        if(DEL_ACCOUNT_URL != null)
            SPUtils.put(context,"DEL_ACCOUNT_URL",DEL_ACCOUNT_URL);
        if(create_url != null)
            SPUtils.put(context,"create_url",create_url);

        System.out.println("@@@     WALLET_URL    :"+WALLET_URL);

        ChatManager.Instance().setVoiceBaseUrl(voiceBaseUrl);
        ChatManager.Instance().setVoiceHostUrl(voiceHostUrl);

        System.out.println("@@@     QueryAplets :"+QueryAplets);

        SPUtils.remove(context, "MainDapp");
        try {
            ChatManager.Instance().setMainDapp("");
        } catch (Exception e) {
        }

        if (MainDapp != null) {
            try {
                str_MainDapp = new String(Base64.decode(MainDapp,0), "utf-8");
                System.out.println("@@@  MainDapp:  "+MainDapp);
                SPUtils.put(context,"MainDapp",str_MainDapp);
                SPUtils.put(context,"ShowMain", "0");
                ChatManager.Instance().setMainDapp(str_MainDapp);
            } catch (Exception e) {
                SPUtils.put(context,"ShowMain", "1");
            }
        } else {
            SPUtils.put(context,"ShowMain", "1");
        }
        String Wallets = null;
        if(Wallet != null){
            try {
                Wallets = new String(Base64.decode(Wallet,0), "utf-8");
                SPUtils.put(context,"Wallet",Wallets);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            SPUtils.remove(context,"Wallet");
        }

        intentActivity(ip,osnId,osnPassword,token,language,Alias);

    }

    private void intentActivity(String ip,String username,String password,String token,String language,String Alias){
        SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
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
            sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
            sp.edit().putString("id", userID).putString("token", token).putString("language", language).apply();

            //////////////////////////////////////
            //Locale locale = new Locale(language);
            //Locale.setDefault(locale);
            Resources resources = context.getResources();
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
            SPUtils.put(context,"isNeedReset",isNeedReset);

            System.out.println("@@@     str_MainDapp :"+str_MainDapp);



            if(str_MainDapp==null){
                Intent intent = new Intent(context, MainActivity1.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("restart",restart);
                intent.putExtra("new_lang",new_lang);
                intent.putExtra("old_lang",old_lang);
                accountListActivity.startActivity(intent);
                accountListActivity.finish();
                return;
            }
            if(str_MainDapp.length() < 5){
                Intent intent = new Intent(context, MainActivity1.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("restart",restart);
                intent.putExtra("new_lang",new_lang);
                intent.putExtra("old_lang",old_lang);
                accountListActivity.startActivity(intent);
                accountListActivity.finish();
            }else{
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("restart",restart);
                intent.putExtra("new_lang",new_lang);
                intent.putExtra("old_lang",old_lang);
                accountListActivity.startActivity(intent);
                accountListActivity.finish();
            }

        } else {
            accountListActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AppService.validateConfig(context);
                    Toast.makeText(context, "密码错误", Toast.LENGTH_SHORT).show();
                }
            });

            //    runOnUiThread(() -> Toast.makeText(PwdBit39Activity.this, getString(R.string.error_login_failed), Toast.LENGTH_SHORT).show());
        }
    }
}
