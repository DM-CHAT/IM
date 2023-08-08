package cn.wildfire.chat.kit.redpacket;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.icu.text.DecimalFormat;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.litapp.LitappActivity;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfire.chat.kit.utils.LoadingDialog;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.RedPacketInfo;
import cn.wildfirechat.model.UnpackInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback2;
import cn.wildfirechat.remote.OnLitappInfoUpdateListener;

public class RedPacketInfoActivity extends WfcBaseActivity {
    private static final String TAG = RedPacketInfoActivity.class.getSimpleName();
    @BindView(R2.id.portrait)
    ImageView portrait;
    @BindView(R2.id.name)
    TextView name;
    @BindView(R2.id.text)
    TextView text;
    @BindView(R2.id.red_packet_info)
    TextView packetInfo;
    @BindView(R2.id.details)
    ListView details;
    @BindView(R2.id.luck_no)
    TextView luckNo;
    @BindView(R2.id.luck_times)
    TextView luckTimes;
    @BindView(R2.id.luck_price)
    TextView luckPrice;
    @BindView(R2.id.luck_tip)
    TextView luckTip;
    @BindView(R2.id.query_balance)
    TextView queryBalance;
    @BindView(R2.id.me_price)
    TextView mePrice;
    @BindView(R2.id.iv_portrait)
    ImageView iv_portrait;
    @BindView(R2.id.tv_name)
    TextView tv_name;

    UnpackAdapter mAdapter = null;
    RedPacketInfo redPacketInfo = null;
    List<UnpackInfo> unpackInfoList = new ArrayList<>();
    long mMessageID = -1;
    String mGroupID = null;
    double odds;

    private String info;
    private String wallet;
    private String cointype;
    private String names;
    private String portraits;
    private String coin;

    class ViewHolder {
        ImageView portrait;
        TextView nameView;
        TextView priceView;
        TextView timeView;
        TextView infoView;

        public ViewHolder(View view) {
            portrait = view.findViewById(R.id.portrait);
            nameView = view.findViewById(R.id.name);
            priceView = view.findViewById(R.id.price);
            timeView = view.findViewById(R.id.time);
            infoView = view.findViewById(R.id.info);
            view.setTag(this);
        }
        public void setInfo(UnpackInfo info){
            UserInfo userInfo = ChatManager.Instance().getUserInfo(info.fetcher, false);
            System.out.println("@@@    userInfo="+userInfo.displayName);
            if(userInfo == null){
                Glide.with(RedPacketInfoActivity.this).load(R.mipmap.default_header).into(portrait);
                nameView.setText("");
            }else{
                Glide.with(RedPacketInfoActivity.this).load(userInfo.portrait).into(portrait);
                nameView.setText(userInfo.displayName);
            }
            double price = Double.parseDouble(info.price)/1000000;
            priceView.setText(coin +"   "+ String.format("%.6f", price));
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_pattern));
            timeView.setText(dateFormat.format(new Date(info.timestamp)));
        }
    }
    class UnpackAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return unpackInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return unpackInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_red_pakcet_info,null);
                holder = new ViewHolder(convertView);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            UnpackInfo info  = (UnpackInfo)getItem(position);
            holder.setInfo(info);
            return convertView;
        }
    }
    @Override
    protected int contentLayout() {
        return R.layout.activity_red_packet_info;
    }
    @Override
    public void afterViews() {
        mAdapter = new UnpackAdapter();
        details.setAdapter(mAdapter);
        setTitleBackgroundResource(R.color.red_packet_info, false);
        yincangToolbar();

        String packetID = getIntent().getStringExtra("packetID");
        mMessageID = getIntent().getLongExtra("messageID", -1);
        mGroupID = getIntent().getStringExtra("groupID");
        info = getIntent().getStringExtra("info");
        if(info != null){
            JSONObject jsonObject = JSONObject.parseObject(info);
            wallet = jsonObject.getString("wallet");
            if(wallet != null){
                JSONObject jsonObject1 = JSONObject.parseObject(wallet);
                names = jsonObject1.getString("name");
                portraits = jsonObject1.getString("portrait");
            }
        }
        tv_name.setText(names);
        Glide.with(this).load(portraits).apply(new RequestOptions().placeholder(R.mipmap.default_header).centerCrop()).into(iv_portrait);

        Log.d(TAG, "packetID: "+packetID+", messageID: "+mMessageID+", groupID: "+mGroupID);
        if(packetID == null)
            return;
        redPacketInfo = getIntent().getParcelableExtra("redPacketInfo");
        if(redPacketInfo == null){
            redPacketInfo = ChatManager.Instance().getRedPacket(packetID);
            if(redPacketInfo == null){
                Log.d(TAG, "redPacketInfo is null");
                return;
            }
        }
        if(redPacketInfo.type.equalsIgnoreCase("normal")){
            getNormalRedPacket();
        }else if(redPacketInfo.type.equalsIgnoreCase("loot")){
            getGroupRedPacket();
        }else if(redPacketInfo.type.equalsIgnoreCase("bomb")){
            listGroupPacket();
        }
    }
    void startQueryBalance(LitappInfo litappInfo){
        Intent intent = new Intent(this, LitappActivity.class);
        intent.putExtra("litappInfo", litappInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @OnClick(R2.id.iv_back)
    void onBack(){
        finish();
    }
    @OnClick(R2.id.query_balance)
    void onQueryBalance(){
        LitappInfo litappInfo = ChatManager.Instance().getLitappInfo(redPacketInfo.dapp, false);
        if(litappInfo == null){
            Log.d(TAG, "query dapp: "+redPacketInfo.dapp);
            ChatManager.Instance().addLitappInfoUpdateListener(new OnLitappInfoUpdateListener() {
                @Override
                public void onLitappInfoUpdate(List<LitappInfo> litappInfos) {
                    ChatManager.Instance().removeLitappInfoUpdateListener(this);
                    startQueryBalance(litappInfos.get(0));
                }
            });
        }else{
            Log.d(TAG, "start dapp: "+litappInfo.target);
            startQueryBalance(litappInfo);
        }
    }
    @OnClick(R2.id.iv_portrait)
    void portraitOnClick(){
        LitappInfo litappInfo = new LitappInfo();

        try {

            JSONObject jsonObject = JSONObject.parseObject(wallet);
            System.out.println("@@@   wallet="+wallet);
            litappInfo.url = jsonObject.getString("url");
            litappInfo.info = jsonObject.getString("info");
            litappInfo.portrait = jsonObject.getString("portrait");
            litappInfo.target = jsonObject.getString("target");



            System.out.println("@@@    target ="+litappInfo.target);

            if (litappInfo.target.equals("OSNS6qJE5Y5JqH9hosq3bhzydniWzRZM8F18MQ24gLsqQzque49")) {
                return;
            }


            litappInfo.name = jsonObject.getString("name");
            litappInfo.displayName = "";
            litappInfo.theme = "";
            litappInfo.param = jsonObject.getString("param");
            System.out.println("@@@    param2="+jsonObject.getString("param"));

            Intent intent = new Intent(RedPacketInfoActivity.this,LitappActivity.class);
            intent.putExtra("litappInfo",litappInfo);
            startActivity(intent);

        } catch (Exception e) {

        }

    }

    void updateList(){
        UserInfo userInfo = ChatManager.Instance().getUserInfo(redPacketInfo.user, false);
        if(userInfo != null){
            Glide.with(this).load(userInfo.portrait).apply(new RequestOptions().placeholder(R.mipmap.default_header).centerCrop()).into(portrait);
            name.setText(getString(R.string.red_come_from)+" "+userInfo.displayName);
        }else{
            portrait.setVisibility(View.GONE);
            name.setText("");
        }
        text.setText(redPacketInfo.text);
        double allPrice = 0;
        boolean isWin = false;
        UnpackInfo meInfo = null;
        String userId = ChatManager.Instance().getUserId();
        for(UnpackInfo info : unpackInfoList){
         //   allPrice += Integer.parseInt(info.price);
            allPrice += Double.parseDouble(info.price);
            if(info.price.endsWith(redPacketInfo.luckNum))
                isWin = true;
            if(info.fetcher.equalsIgnoreCase(userId))
                meInfo = info;
        }
        allPrice /= 1000000;
        packetInfo.setText(unpackInfoList.size()+" "+getString(R.string.count_packet)+" "+String.format("%.6f", allPrice)+getString(R.string.yuan));
        if(redPacketInfo.type.equalsIgnoreCase("bomb")){
            if(isWin){
                luckNo.setText(getString(R.string.luck_tail)+": "+redPacketInfo.luckNum);
                if(userInfo != null) {
                    luckTip.setText(getString(R.string.bless0) + "[" +userInfo.displayName+"]"+getString(R.string.win));
                }else{
                    luckTip.setText(getString(R.string.bless0) + "[" +getString(R.string.red_packet_owner)+"]"+getString(R.string.win));
                }
                luckTimes.setText(getString(R.string.luck_times)+": "+odds/10);
                luckPrice.setText(getString(R.string.luck_price)+": "+(allPrice*odds/10));
                luckTip.setVisibility(View.VISIBLE);
                luckTimes.setVisibility(View.VISIBLE);
                luckPrice.setVisibility(View.VISIBLE);
            }else{
                luckNo.setText(getString(R.string.luck_no)+": "+redPacketInfo.luckNum);
            }
            luckNo.setVisibility(View.VISIBLE);
        }else if(redPacketInfo.type.equalsIgnoreCase("loot")){
            if(meInfo != null) {
                double price = Double.parseDouble(meInfo.price)/1000000;
                String coinType = "USD";

                if(info != null){
                    JSONObject jsonObject = JSONObject.parseObject(info);
                    String coinType2 = jsonObject.getString("coinType");
                    if(coinType2 != null){
                        if (coinType2.length() != 0) {
                            coinType = coinType2;
                        }
                    }
                }


                mePrice.setText(coinType + " " + String.format("%.6f", price));
                mePrice.setVisibility(View.VISIBLE);
            }
        }
        if(redPacketInfo.dapp != null && !redPacketInfo.dapp.isEmpty()){
            queryBalance.setVisibility(View.VISIBLE);
        }
        mAdapter.notifyDataSetChanged();
    }
    void listGroupPacket(){
        JSONObject data = new JSONObject();
        data.put("get", "txid");
        data.put("txid", redPacketInfo.packetID);
        Log.d(TAG, "list actors data: "+data.toString());
        OKHttpHelper.postJson(redPacketInfo.urlQuery, data.toString(), new SimpleCallback<String>() {
            @Override
            public void onSuccess1(String t) {

            }

            @Override
            public void onUiSuccess(String result) {
                try{
                    LoadingDialog.hideLoading();
                    Log.d(TAG, "list actors result: "+result);
                    JSONObject json = RedPacketUtils.getData(RedPacketInfoActivity.this, result);
                    if(json == null)
                        return;
                    odds = json.getIntValue("odds");
                    coin = json.getString("coin");
                    JSONArray array = json.getJSONArray("actors");
                    if(array == null)
                        return;
                    for(Object o : array){
                        json = (JSONObject) o;
                        UnpackInfo unpackInfo = new UnpackInfo();
                        unpackInfo.packetID = redPacketInfo.packetID;
                        unpackInfo.unpackID = redPacketInfo.packetID;
                        unpackInfo.fetcher = json.getString("osnid");
                        unpackInfo.price = json.getString("balance");
                        unpackInfo.timestamp = json.getLongValue("timestamp");
                        unpackInfoList.add(unpackInfo);
                    }
                    updateList();
                    updateMessage();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {
                LoadingDialog.hideLoading();
                Log.d(TAG, "code: "+code+", msg: "+msg);
                Toast.makeText(RedPacketInfoActivity.this, getString(R.string.error_hit)+code, Toast.LENGTH_SHORT).show();
            }
        });
    }
    void openGroupRedPacket(String info){
        try{
            JSONObject json = JSON.parseObject(info);
            String hash = json.getString("txid")+json.getString("groupID")+json.getString("from")+json.getString("timestamp");
            json.put("hash", ChatManager.Instance().hashData(hash.getBytes()));
            json.put("sign", ChatManager.Instance().signData(hash.getBytes()));
            Log.d(TAG, "openRedPacket info: "+json.toString());
            OKHttpHelper.postJson(redPacketInfo.urlFetch, json.toString(), new SimpleCallback<String>() {

                @Override
                public void onSuccess1(String t) {

                }

                @Override
                public void onUiSuccess(String s) {
                    Log.d(TAG, "openRedPacket result: "+s);
                    listGroupPacket();
                }

                @Override
                public void onUiFailure(int code, String msg) {
                    LoadingDialog.hideLoading();
                    Log.d(TAG, "code: "+code+", msg: "+msg);
                    Toast.makeText(RedPacketInfoActivity.this, getString(R.string.error_hit)+code, Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    void getGroupRedPacket(){
        if(redPacketInfo.state == 0) {
            LoadingDialog.showLoading(this, null);
            Log.d(TAG, "urlQuery: " + redPacketInfo.urlQuery);
            Log.d(TAG, "urlFetch: " + redPacketInfo.urlFetch);
            JSONObject data = new JSONObject();
            data.put("get", "txid");
            data.put("txid", redPacketInfo.packetID);
            Log.d(TAG, "price data: " + data.toString());

            try {
                //Log.d(TAG, "price result: " + result);
                /*JSONObject json = RedPacketUtils.getData(RedPacketInfoActivity.this, result);
                if(json == null)
                    return;
                if(json.getIntValue("balance") == 0){
                    Toast.makeText(RedPacketInfoActivity.this, R.string.red_packet_finish, Toast.LENGTH_SHORT).show();
                    return;
                }*/
                new Thread(()->{
                    long timestamp = System.currentTimeMillis();
                    JSONObject sign = new JSONObject();
                    sign.put("groupID", mGroupID);
                    sign.put("from", ChatManager.Instance().getUserId());
                    sign.put("txid", redPacketInfo.unpackID);
                    sign.put("timestamp", String.valueOf(timestamp));
                    Log.d(TAG, "getGroupSign data: "+sign.toString());
                    ChatManager.Instance().getGroupSign(mGroupID, sign.toString(), new GeneralCallback2() {
                        @Override
                        public void onSuccess(String result) {
                            Log.d(TAG, "getGroupSign result: "+result);
                            runOnUiThread(()->{
                                openGroupRedPacket(result);
                            });
                        }

                        @Override
                        public void onFail(int errorCode) {
                            Log.d(TAG, "code: " + errorCode);
                            LoadingDialog.hideLoading();
                            Looper.prepare();
                            Toast.makeText(RedPacketInfoActivity.this, getString(R.string.error_hit)+"get group sign error.", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    });
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }









            /*OKHttpHelper.postJson(redPacketInfo.urlQuery, data.toString(), new SimpleCallback<String>() {
                @Override
                public void onSuccess1(String t) {

                }

                @Override
                public void onUiSuccess(String result) {
                    try {
                        Log.d(TAG, "price result: " + result);
                        JSONObject json = RedPacketUtils.getData(RedPacketInfoActivity.this, result);
                        if(json == null)
                            return;
                        if(json.getIntValue("balance") == 0){
                            Toast.makeText(RedPacketInfoActivity.this, R.string.red_packet_finish, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        new Thread(()->{
                            long timestamp = System.currentTimeMillis();
                            JSONObject sign = new JSONObject();
                            sign.put("groupID", mGroupID);
                            sign.put("from", ChatManager.Instance().getUserId());
                            sign.put("txid", redPacketInfo.unpackID);
                            sign.put("timestamp", String.valueOf(timestamp));
                            Log.d(TAG, "getGroupSign data: "+sign.toString());
                            ChatManager.Instance().getGroupSign(mGroupID, sign.toString(), new GeneralCallback2() {
                                @Override
                                public void onSuccess(String result) {
                                    Log.d(TAG, "getGroupSign result: "+result);
                                    runOnUiThread(()->{
                                        openGroupRedPacket(result);
                                    });
                                }

                                @Override
                                public void onFail(int errorCode) {
                                    Log.d(TAG, "code: " + errorCode);
                                    LoadingDialog.hideLoading();
                                    Looper.prepare();
                                    Toast.makeText(RedPacketInfoActivity.this, getString(R.string.error_hit)+"get group sign error.", Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }
                            });
                        }).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onUiFailure(int code, String msg) {
                    LoadingDialog.hideLoading();
                    Log.d(TAG, "code: " + code + ", msg: " + msg);
                    Toast.makeText(RedPacketInfoActivity.this, getString(R.string.error_hit) + code, Toast.LENGTH_SHORT).show();
                }
            });*/


        }else{
            listGroupPacket();
        }
    }
    void getNormalRedPacket(){
        LoadingDialog.showLoading(this, null);
        Log.d(TAG, "url: "+redPacketInfo.urlQuery);
        JSONObject data = new JSONObject();
        data.put("get", "txid");
        data.put("txid", redPacketInfo.unpackID);
        Log.d(TAG, "data: "+data.toString());
        OKHttpHelper.postJson(redPacketInfo.urlQuery, data.toString(), new SimpleCallback<String>() {
            @Override
            public void onSuccess1(String t) {

            }

            @Override
            public void onUiSuccess(String result) {
                try{
                    LoadingDialog.hideLoading();
                    Log.d(TAG, "result: "+result);
                    UnpackInfo unpackInfo = new UnpackInfo(redPacketInfo);
                    unpackInfoList.add(unpackInfo);
                    updateList();
                    updateMessage();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {
                LoadingDialog.hideLoading();
                Log.d(TAG, "code: "+code+", msg: "+msg);
                Toast.makeText(RedPacketInfoActivity.this, getString(R.string.error_hit)+code, Toast.LENGTH_SHORT).show();
            }
        });
    }
    void updateMessage(){
        if(mMessageID != -1)
            ChatManager.Instance().openRedPacket(redPacketInfo.packetID, redPacketInfo.unpackID, mMessageID);
    }
}