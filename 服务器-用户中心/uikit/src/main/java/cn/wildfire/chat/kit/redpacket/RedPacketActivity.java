package cn.wildfire.chat.kit.redpacket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.king.zxing.Intents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.wildfire.chat.kit.BuildConfig;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfire.chat.kit.third.utils.UIUtils;
import cn.wildfire.chat.kit.utils.CryptUtils;
import cn.wildfire.chat.kit.utils.LoadingDialog;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfire.chat.kit.web.WebViewActivity;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;

import androidx.annotation.NonNull;

public class RedPacketActivity extends WfcBaseActivity {
    @BindView(R2.id.price)
    TextView price;
    @BindView(R2.id.info_price)
    TextView priceInfo;
    @BindView(R2.id.info_group)
    TextView userCountTextView;
    @BindView(R2.id.layout_target)
    LinearLayout layoutTarget;
    @BindView(R2.id.layout_count)
    LinearLayout layoutCount;
    @BindView(R2.id.layout_luck)
    LinearLayout layoutLuck;
    @BindView(R2.id.game_rule)
    LinearLayout layoutGameRule;
    @BindView(R2.id.luck_input)
    EditText luckInput;
    @BindView(R2.id.price_input)
    EditText priceInput;
    @BindView(R2.id.packet_count)
    EditText packetCount;
    @BindView(R2.id.text)
    EditText packetText;
    @BindView(R2.id.confirm)
    Button confirm;
    @BindView(R2.id.game_role)
    TextView tvGameRole;
    @BindView(R2.id.iv_title)
    ImageView iv_title;
    @BindView(R2.id.tv_name)
    TextView tv_name;
    @BindView(R2.id.edt_money)
    EditText edt_money;
    @BindView(R2.id.btn_zhuanzhang)
    Button btn_zhuanzhang;
    @BindView(R2.id.ll_zhuanzhang)
    LinearLayout ll_zhuanzhang;
    @BindView(R2.id.ll_zhuanzhang1)
    LinearLayout ll_zhuanzhang1;
    @BindView(R2.id.ll_zhuanzhang2)
    LinearLayout ll_zhuanzhang2;
    @BindView(R2.id.rl_title)
    RelativeLayout rl_title;

    String packetType;
    String gameRules;
    Conversation conversation;
    String TAG = RedPacketActivity.class.getSimpleName();
    List<Integer> countRange = new ArrayList<>();
    int priceMin = 0;
    int priceMax = 0;
    int inputStatus = 0;
    String inputCode = "";
    List<EditText> inputEdit = new ArrayList<>();
    AlertDialog inputDialog = null;
    public static float oldPrice = 0.0f;
    public static int oldCount = 0;
    public static int oldLucky = -1;
    private String userName;


    @Override
    protected int contentLayout() {
        return R.layout.activity_red_packet;
    }
    @Override
    public void afterViews() {
        packetType = getIntent().getStringExtra("type");
        conversation = getIntent().getParcelableExtra("conversation");
        switch(packetType){
            case "normal":
                layoutGameRule.setVisibility(View.GONE);
                layoutTarget.setVisibility(View.GONE);
                layoutCount.setVisibility(View.GONE);
                userCountTextView.setVisibility(View.GONE);
                priceInfo.setText(R.string.single_price);
                ll_zhuanzhang1.setVisibility(View.GONE);
                ll_zhuanzhang2.setVisibility(View.GONE);
                price.setVisibility(View.GONE);
                confirm.setVisibility(View.GONE);
                ll_zhuanzhang.setVisibility(View.VISIBLE);
                rl_title.setVisibility(View.VISIBLE);
                btn_zhuanzhang.setVisibility(View.VISIBLE);

                UserInfo userInfo = ChatManager.Instance().getUserInfo(conversation.target,false);
                tv_name.setText(userInfo.displayName);
                userName = userInfo.displayName;
                Glide.with(this)
                        .load(userInfo.portrait)
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(iv_title);

                break;
            case "bomb":
                layoutGameRule.setVisibility(View.VISIBLE);
                tvGameRole.setVisibility(View.GONE);
                layoutLuck.setVisibility(View.VISIBLE);
                if(oldPrice != 0){
                    priceInput.setText(String.valueOf(oldPrice));
                    packetCount.setText(String.valueOf(oldCount));
                    luckInput.setText(String.valueOf(oldLucky));
                }
                getBombInfo();
            case "loot":
                GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(conversation.target, false);
                if(groupInfo != null){
                    userCountTextView.setText(getString(R.string.group_has)+" " +groupInfo.memberCount+" "+getString(R.string.man));
                }else{
                    userCountTextView.setVisibility(View.GONE);
                }
                layoutTarget.setVisibility(View.GONE);
                priceInfo.setText(R.string.all_price);
                break;
            case "user":
                layoutTarget.setVisibility(View.VISIBLE);
                layoutCount.setVisibility(View.GONE);
                layoutGameRule.setVisibility(View.GONE);
                userCountTextView.setVisibility(View.GONE);
                priceInfo.setText(R.string.price);
                break;
        }
        InputFilter[] filter = {new CashierInputFilter()};
        priceInput.setFilters(filter);
    }
    boolean checkInput(){
        String f;
        if(packetType.equals("normal")){
            if(edt_money.getText().toString().isEmpty()){
                Toast.makeText(RedPacketActivity.this, R.string.price_error, Toast.LENGTH_SHORT).show();
                return false;
            }
            f = edt_money.getText().toString();
        }else{
            if(priceInput.getText().toString().isEmpty()){
                Toast.makeText(RedPacketActivity.this, R.string.price_error, Toast.LENGTH_SHORT).show();
                return false;
            }
            f = priceInput.getText().toString();
        }

        float ff = Float.parseFloat(f);
        if (ff > 99999999.0f) {
            Toast.makeText(RedPacketActivity.this, R.string.price_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        switch (packetType){
            case "bomb":
                if(luckInput.getText().toString().isEmpty()){
                    Toast.makeText(RedPacketActivity.this, R.string.luck_no_error, Toast.LENGTH_SHORT).show();
                    return false;
                }
            case "loot":
                GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(conversation.target, false);
                if(groupInfo != null){
                    int count = Integer.parseInt(packetCount.getText().toString());
                    if(count > groupInfo.memberCount){
                        Toast.makeText(RedPacketActivity.this, R.string.packet_count_error, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                if(packetCount.getText().toString().isEmpty()){
                    Toast.makeText(RedPacketActivity.this, R.string.packet_count_error, Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
        }
        if (packetType.equals("bomb")) {
            String value = priceInput.getText().toString();
            if(!value.isEmpty()){
                //oldPrice = Integer.parseInt(value);
                oldPrice = Float.parseFloat(value);

            }

            value = packetCount.getText().toString();
            if(!value.isEmpty())
                oldCount = Integer.parseInt(value);
            value = luckInput.getText().toString();
            if(!value.isEmpty())
                oldLucky = Integer.parseInt(value);
        }

        return true;
    }
    @OnClick(R2.id.confirm)
    void confirm(){
        if(!checkInput())
            return;
        View view = getLayoutInflater().inflate(R.layout.dialog_redpacket_verify,null);
        ImageView ivClose = view.findViewById(R.id.close);
        TextView tvType = view.findViewById(R.id.type);
        TextView tvPrice = view.findViewById(R.id.price);
        EditText tvText0 = view.findViewById(R.id.text0);
        EditText tvText1 = view.findViewById(R.id.text1);
        EditText tvText2 = view.findViewById(R.id.text2);
        EditText tvText3 = view.findViewById(R.id.text3);
        EditText tvText4 = view.findViewById(R.id.text4);
        EditText tvText5 = view.findViewById(R.id.text5);
        switch(packetType){
            case "user":
            case "normal":
                tvType.setText(R.string.redPacket);
                break;
            case "loot":
                tvType.setText(R.string.redPacket_group);
                break;
            case "bomb":
                tvType.setText(R.string.redPacket_bomb);
                break;
        }
        tvPrice.setText("USDT "+priceInput.getText().toString());
        if(inputDialog != null)
            inputDialog.dismiss();
        inputEdit.clear();
        inputCode = "";
        inputStatus = 0;
        inputDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .create();
        inputDialog.setOnShowListener(dialog1 -> {
            tvText0.requestFocus();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(tvText0, 0);
                }
            }, 500);
        });
        inputDialog.show();
        Window window = inputDialog.getWindow();
        window.setContentView(view);
        window.setBackgroundDrawableResource(R.drawable.shape_corner);
        window.clearFlags(FLAG_ALT_FOCUSABLE_IM);

        ivClose.setOnClickListener(v -> {
            inputDialog.dismiss();
            inputDialog = null;
        });
        addTextListener(tvText0);
        addTextListener(tvText1);
        addTextListener(tvText2);
        addTextListener(tvText3);
        addTextListener(tvText4);
        addTextListener(tvText5);
    }

    @OnClick(R2.id.btn_zhuanzhang)
    void setBtn_zhuanzhang(){
        if(!checkInput())
            return;
        View view = getLayoutInflater().inflate(R.layout.dialog_redpacket_verify,null);
        ImageView ivClose = view.findViewById(R.id.close);
        TextView tvType = view.findViewById(R.id.type);
        TextView tvPrice = view.findViewById(R.id.price);
        EditText tvText0 = view.findViewById(R.id.text0);
        EditText tvText1 = view.findViewById(R.id.text1);
        EditText tvText2 = view.findViewById(R.id.text2);
        EditText tvText3 = view.findViewById(R.id.text3);
        EditText tvText4 = view.findViewById(R.id.text4);
        EditText tvText5 = view.findViewById(R.id.text5);
        String content = getString(R.string.direction) + userName +"  "+getString(R.string.transfer);
        switch(packetType){
            case "user":
            case "normal":
                tvType.setText(content);
                break;
            case "loot":
                tvType.setText(R.string.redPacket_group);
                break;
            case "bomb":
                tvType.setText(R.string.redPacket_bomb);
                break;
        }
        tvPrice.setText("usdt "+edt_money.getText().toString());
        if(inputDialog != null)
            inputDialog.dismiss();
        inputEdit.clear();
        inputCode = "";
        inputStatus = 0;
        inputDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .create();
        inputDialog.setOnShowListener(dialog1 -> {
            tvText0.requestFocus();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(tvText0, 0);
                }
            }, 500);
        });
        inputDialog.show();
        Window window = inputDialog.getWindow();
        window.setContentView(view);
        window.setBackgroundDrawableResource(R.drawable.shape_corner);
        window.clearFlags(FLAG_ALT_FOCUSABLE_IM);

        ivClose.setOnClickListener(v -> {
            inputDialog.dismiss();
            inputDialog = null;
        });
        addTextListener(tvText0);
        addTextListener(tvText1);
        addTextListener(tvText2);
        addTextListener(tvText3);
        addTextListener(tvText4);
        addTextListener(tvText5);
    }

    void onEditChanged(CharSequence s){
        inputCode += s;
        inputStatus += 1;
        if (inputStatus >= 6) {
            inputDialog.dismiss();
            inputDialog = null;

            Log.d(TAG, "verify code: "+inputCode);
            makePacket(inputCode);
        }else{
            EditText editText = inputEdit.get(inputStatus);
            editText.requestFocus();
        }
    }
    void onEditDeleted(){
        if(inputStatus > 0) {
            inputStatus -= 1;
            inputCode = inputCode.substring(0, inputStatus);
            EditText editText = inputEdit.get(inputStatus);
            editText.setText("");
            editText.requestFocus();
        }
    }
    void addTextListener(EditText editText){
        inputEdit.add(editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0){
                    onEditChanged(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() != 0){
                    editText.removeTextChangedListener(this);
                    editText.setText("*");
                    editText.addTextChangedListener(this);
                }
            }
        });
        editText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL)
                onEditDeleted();
            return false;
        });
    }
    @OnTextChanged(R2.id.price_input)
    void priceChange(CharSequence text){
        price.setText("USDT "+text);
    }
    void getBombInfo(){
        String url_BOMB_ROLE_URL = (String) SPUtils.get(RedPacketActivity.this,"BOMB_ROLE_URL","");
        JSONObject json = new JSONObject();
        json.put("language", WfcBaseActivity.getBaseLanguage());
        OKHttpHelper.postJson(url_BOMB_ROLE_URL, json.toString(), new SimpleCallback<String>() {
            @Override
            public void onSuccess1(String t) {

            }

            @Override
            public void onUiSuccess(String result) {
                try{
                    Log.d(TAG, "bomb result: "+result);
                    JSONObject json = RedPacketUtils.getData(RedPacketActivity.this, result);
                    if(json == null)
                        return;
                    JSONArray array = json.getJSONArray("count");
                    if(array != null){
                        for(Object o : array){
                            countRange.add((Integer) o);
                        }

                        packetCount.setHint(getString(R.string.input_count)+array.toString());
                    }

                    JSONObject balance = json.getJSONObject("balance");
                    if(balance != null){
                        priceMin = balance.getIntValue("min");
                        priceMax = balance.getIntValue("max");
                        priceInput.setHint(priceMin/100+" - "+priceMax/100);
                    }


                    //tvGameRole.setText(json.getString("gameRole"));
                    gameRules = json.getString("gameRole");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {
                Toast.makeText(RedPacketActivity.this, R.string.packet_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
    void orderPay(String code, int serial){
        LoadingDialog.showLoading(RedPacketActivity.this,"请稍后");
        long timestamp = System.currentTimeMillis();
        String userID = ChatManager.Instance().getUserId();
        long balance;
        if(packetType.equals("normal")){
            balance = (long)Math.floor(Double.parseDouble(edt_money.getText().toString())*100);
        }else{
            balance = (long)Math.floor(Double.parseDouble(priceInput.getText().toString())*100);
        }
        String text = packetText.getText().toString();
        if(text.isEmpty())
            text = getString(R.string.bless);
        JSONObject json = new JSONObject();
        json.put("command", "transfer");
        json.put("from", userID);
        JSONObject data = new JSONObject();
        switch(packetType){
            case "user":
            case "normal":
                data.put("type", "transaction");
                break;
            case "loot":
                data.put("type", "random");
                break;
            case "bomb":
                data.put("type", "bomb");
                break;
        }
        data.put("from", userID);
        data.put("to", conversation.target);
        data.put("count", packetCount.getText().toString());
        data.put("balance", balance);
        data.put("timestamp", timestamp);
        data.put("serial", serial+1);
        data.put("greetings", text);
        data.put("luckNum", luckInput.getText().toString());
        Log.d(TAG, "order pay data: "+data.toString());
        String encrypted = CryptUtils.aesEncrypt(data.toString().getBytes(), code);
        json.put("body", encrypted);
        String calc = json.getString("command")+json.getString("from")+json.getString("body");
        String hash = ChatManager.Instance().hashData(calc.getBytes());
        String sign = ChatManager.Instance().signData(hash.getBytes());
        json.put("hash", hash);
        json.put("sign", sign);
        json.put("language", WfcBaseActivity.getBaseLanguage());
        String finalText = text;
        Log.d(TAG, "order pay data: "+json.toString());
        String url_TRANSFER_URL = (String) SPUtils.get(RedPacketActivity.this,"TRANSFER_URL","");

        OKHttpHelper.postJson(url_TRANSFER_URL, json.toString(), new SimpleCallback<String>() {

            @Override
            public void onSuccess1(String t) {

            }

            @Override
            public void onUiSuccess(String result) {
                try{
                    Log.d(TAG, "order pay result: "+result);
                    /*JSONObject json = RedPacketUtils.getData(RedPacketActivity.this, result);
                    if(json == null)
                        return;*/
                    if (result == null) {
                        LoadingDialog.hideLoading();
                        Looper.prepare();
                        Toast.makeText(RedPacketActivity.this, getString(R.string.service_error), Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        return;
                    }

                    JSONObject jsonObject = JSONObject.parseObject(result);
                    if (jsonObject == null) {
                        LoadingDialog.hideLoading();
                        Looper.prepare();
                        Toast.makeText(RedPacketActivity.this, getString(R.string.service_error), Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        return;
                    }

                    String messageId = jsonObject.getString("data");

                    SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
                    String token = sp.getString("token", null);

                    OKHttpHelper.getResult(messageId, finalText, token, new SimpleCallback<String>() {

                        @Override
                        public void onSuccess1(String result) {
                            // 这里发送message

                            System.out.println("@@@   result="+result);
                            if(result == null || result.equals("")){
                                LoadingDialog.hideLoading();
                                Looper.prepare();
                                Toast.makeText(RedPacketActivity.this, getString(R.string.service_error), Toast.LENGTH_SHORT).show();
                                Looper.loop();
                                return;
                            }
                            Gson gson = new Gson();
                            RedPacketInfo redPacketInfo = gson.fromJson(result,RedPacketInfo.class);
                            if (redPacketInfo == null) {
                                LoadingDialog.hideLoading();
                                Looper.prepare();
                                Toast.makeText(RedPacketActivity.this, getString(R.string.service_error), Toast.LENGTH_SHORT).show();
                                Looper.loop();
                                return;
                            }
                            if(redPacketInfo.getCode() == 200){
                                /*String txid = json.getString("txid");
                                String urlQuery = json.getString("queryUrl");
                                String urlFetch = json.getString("url");*/

                                try {

                                    String txid = redPacketInfo.getData().getTxid();
                                    String urlQuery = redPacketInfo.getData().getQueryUrl();
                                    String urlFetch = redPacketInfo.getData().getUrl();
                                    String wallet = redPacketInfo.getWallet();
                                    String cointype = redPacketInfo.getCoinType();
                                    if(txid == null){
                                        Toast.makeText(RedPacketActivity.this, R.string.server_parameter_error, Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    if(urlQuery == null)
                                        urlQuery = "";
                                    if(urlFetch == null)
                                        urlFetch = "";
                                    data.put("txid", txid);
                                    data.put("text", finalText);
                                    data.put("packetID", txid);
                                    data.put("unpackID", txid);
                                    data.put("urlQuery", urlQuery);
                                    data.put("urlFetch", urlFetch);
                                    data.put("wallet",wallet);
                                    data.put("coinType",cointype);
                                    //data.put("dapp", "OSNS6qJXyTT3KrNVngopdCHwG1tv87z3fQB2txtLTEKTW2dS3N6");
                                    Intent intent = new Intent();
                                    intent.putExtra("id", txid);
                                    intent.putExtra("info", data.toString());
                                    intent.putExtra("text", finalText);
                                    setResult(Activity.RESULT_OK, intent);
                                    System.out.println("@@@  data="+data.toString());
                                    LoadingDialog.hideLoading();
                                    finish();

                                } catch (Exception e) {
                                    LoadingDialog.hideLoading();
                                    Looper.prepare();
                                    Toast.makeText(RedPacketActivity.this, getString(R.string.service_error), Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }


                            }

                        }

                        @Override
                        public void onFailure(int code, String message) {
                            //super.onFailure(code, message);

                            // 提示失败
                            LoadingDialog.hideLoading();
                            Looper.prepare();
                            Toast.makeText(RedPacketActivity.this, message, Toast.LENGTH_SHORT).show();
                            if(code == 40000006){
                                String url_WALLET_URL = (String) SPUtils.get(RedPacketActivity.this,"WALLET_URL","");
                                Intent intent = new Intent(RedPacketActivity.this, WebViewActivity.class);
                                intent.putExtra("url", url_WALLET_URL);
                                intent.putExtra("isSetting","&isSetting=1");
                                startActivity(intent);
                                finish();
                            }
                            Looper.loop();

                        }

                        @Override
                        public void onUiSuccess(String s) {

                        }

                        @Override
                        public void onUiFailure(int code, String msg) {
                            // 提示失败
                            LoadingDialog.hideLoading();
                            Looper.prepare();
                            Toast.makeText(RedPacketActivity.this, R.string.packet_failed, Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {
                Log.d(TAG, "code: "+code+", msg: "+msg);
                LoadingDialog.hideLoading();
                Looper.prepare();
                Toast.makeText(RedPacketActivity.this, R.string.packet_failed, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
    }

    private boolean getResult(String transferId,String finalText){

        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String token = sp.getString("token", null);
        String url_GETTRANSFEREST_URL = (String) SPUtils.get(RedPacketActivity.this,"GETTRANSFEREST_URL","");
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url_GETTRANSFEREST_URL+"?messageId="+transferId)
                .get()
                .addHeader("X-Token",token)
                .build();
        Call call = okHttpClient.newCall(request);

        //同步
        try {
            Response response = call.clone().execute();
            if(response.isSuccessful()){
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {


                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String result = response.body().string();
                        System.out.println("@@@   result="+result);
                        if(result == null || result.equals("")){
                            return;
                        }
                        Gson gson = new Gson();
                        RedPacketInfo redPacketInfo = gson.fromJson(result,RedPacketInfo.class);
                        if(redPacketInfo.getCode() == 200){
                                /*String txid = json.getString("txid");
                            String urlQuery = json.getString("queryUrl");
                            String urlFetch = json.getString("url");*/

                            String txid = redPacketInfo.getData().getTxid();
                            String urlQuery = redPacketInfo.getData().getQueryUrl();
                            String urlFetch = redPacketInfo.getData().getUrl();
                            if(txid == null){
                                Toast.makeText(RedPacketActivity.this, R.string.server_parameter_error, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JSONObject data = new JSONObject();
                            if(urlQuery == null)
                                urlQuery = "";
                            if(urlFetch == null)
                                urlFetch = "";
                            data.put("txid", txid);
                            data.put("text", finalText);
                            data.put("packetID", txid);
                            data.put("unpackID", txid);
                            data.put("urlQuery", urlQuery);
                            data.put("urlFetch", urlFetch);
                            //data.put("dapp", "OSNS6qJXyTT3KrNVngopdCHwG1tv87z3fQB2txtLTEKTW2dS3N6");
                            Intent intent = new Intent();
                            intent.putExtra("id", txid);
                            intent.putExtra("info", data.toString());
                            intent.putExtra("text", finalText);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }

                    }
                });
                return true;
            }else{
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    void makePacket(String verifyCode){
        String url_ACCOUNT_PREFIX_URL = (String) SPUtils.get(RedPacketActivity.this,"ACCOUNT_PREFIX_URL","");
        OKHttpHelper.postJson(url_ACCOUNT_PREFIX_URL+"/"+ChatManager.Instance().getUserId(),
                "", new SimpleCallback<String>() {
                    @Override
                    public void onSuccess1(String t) {

                    }

                    @Override
            public void onUiSuccess(String s) {
                try{
                    Log.d(TAG, "get serial result: "+s);
                    JSONObject json = RedPacketUtils.getData(RedPacketActivity.this, s);
                    if(json == null)
                        return;
                    int serial = json.getIntValue("serial");
                    orderPay(verifyCode, serial);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {
                Log.d(TAG, "code: "+code+", msg: "+msg);
                Looper.prepare();
                Toast.makeText(RedPacketActivity.this,msg,Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
    }
    @OnClick(R2.id.game_rule)
    void showGameRule(){
        AlertDialog.Builder dialog =  new AlertDialog.Builder(RedPacketActivity.this);
        dialog.setTitle(getString(R.string.game_rule))
            .setMessage(gameRules)
            .setPositiveButton(getString(R.string.confirm), (dialog1, which1) -> {
            }).show();
    }
}