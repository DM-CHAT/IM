package com.ospn.osnsdk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ospn.osnsdk.callback.OSNGeneralCallback;
import com.ospn.osnsdk.callback.OSNGeneralCallbackT;
import com.ospn.osnsdk.callback.OSNListener;
import com.ospn.osnsdk.callback.OSNTransferCallback;
import com.ospn.osnsdk.callback.SYNCallback;
import com.ospn.osnsdk.data.OsnFriendInfo;
import com.ospn.osnsdk.data.OsnGroupInfo;
import com.ospn.osnsdk.data.OsnMemberInfo;
import com.ospn.osnsdk.data.OsnMessageInfo;
import com.ospn.osnsdk.data.OsnRequestInfo;
import com.ospn.osnsdk.data.OsnUserInfo;
import com.ospn.osnsdk.data.serviceInfo.OsnLitappInfo;
import com.ospn.osnsdk.data.serviceInfo.OsnServiceInfo;
import com.ospn.osnsdk.utils.ECUtils;
import com.ospn.osnsdk.utils.HttpUtils;
import com.ospn.osnsdk.utils.OsnUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static com.ospn.osnsdk.utils.OsnUtils.aesDecrypt;
import static com.ospn.osnsdk.utils.OsnUtils.aesEncrypt;
import static com.ospn.osnsdk.utils.OsnUtils.b64Encode;
import static com.ospn.osnsdk.utils.OsnUtils.logError;
import static com.ospn.osnsdk.utils.OsnUtils.logInfo;
import static com.ospn.osnsdk.utils.OsnUtils.makeMessage;
import static com.ospn.osnsdk.utils.OsnUtils.sha256;
import static com.ospn.osnsdk.utils.OsnUtils.takeMessage;
import static com.ospn.osnsdk.utils.OsnUtils.wrapMessage;

public class OSNManager implements Osnsdk {
    private String mOsnID = null;
    private String mOsnKey = null;
    private String mShadowKey = null;
    private String mTempID = null;
    private String mTempKey = null;
    private String mServiceID = null;
    private String mAesKey = null;
    private String mDeviceID = null;
    private String imLoginToken = null;
    private String mHideEnable1 = null;
    private String mHideEnable2 = null;
    private String mHideEnable3 = null;

    private boolean mRecallEnable = true;
    private boolean mDeleteEnable = true;

    private long mRID = System.currentTimeMillis();
    private boolean mLogined = false;
    private boolean mInitSync = false;
    private boolean mHearted = false;
    private long mMsgSyncID = 0;
    private boolean mMsgSynced = false;
    private String mHost = null;
    private final int mPort = 8100;
    public int mTimeoutCount = 0;
    public int mHeartCount = 0;
    private Socket mSock = null;
    private boolean isAvailable = true;
    private final Object mHearter = new Object();
    private final Semaphore mReader = new Semaphore(0, true);
    private OSNListener mOsnListener;
    private final Object mSendLock = new Object();
    private final ConcurrentHashMap<String, SYNCallback> mIDMap = new ConcurrentHashMap<>();
    //private final ExecutorService mExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final ExecutorService mExecutor = Executors.newCachedThreadPool();
    private static OSNManager INST = null;

    private OSNManager() {
    }
    public void writeLogger(String text){
        if(mOsnListener != null)
            mOsnListener.logInfo(text);
    }
    private JSONObject sendPackage(JSONObject json) {
        try {
            if (!mSock.isConnected())
                return null;
            String id;
            synchronized (this) {
                id = String.valueOf(mRID++);
            }
            json.put("id", id);

            logInfo(json.getString("command") + ": " + json.toString());
            byte[] jsonData = json.toString().getBytes();
            byte[] headData = new byte[4];
            headData[0] = (byte) ((jsonData.length >> 24) & 0xff);
            headData[1] = (byte) ((jsonData.length >> 16) & 0xff);
            headData[2] = (byte) ((jsonData.length >> 8) & 0xff);
            headData[3] = (byte) (jsonData.length & 0xff);
            synchronized (mSendLock) {
                OutputStream outputStream = mSock.getOutputStream();
                outputStream.write(headData);
                outputStream.write(jsonData);
                outputStream.flush();
            }

            final Object lock = new Object();
            final JSONObject[] result = {null};
            SYNCallback synCallback = (id1, json1) -> {
                try {
                    result[0] = json1;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                synchronized (lock) {
                    lock.notify();
                }
            };
            mIDMap.put(id, synCallback);
            synchronized (lock) {
                lock.wait(8000);
            }
            mIDMap.remove(id);
            return result[0];
        } catch (Exception e) {
            e.printStackTrace();
            logError(e);
        }
        return null;
    }

    private JSONObject sendPackage2(JSONObject json) {
        try {
            if (!mSock.isConnected())
                return null;
            String id;
            synchronized (this) {
                id = String.valueOf(mRID++);
            }
            json.put("id", id);

            logInfo(json.getString("command") + ": " + json.toString());
            byte[] jsonData = json.toString().getBytes();
            byte[] headData = new byte[4];
            headData[0] = (byte) ((jsonData.length >> 24) & 0xff);
            headData[1] = (byte) ((jsonData.length >> 16) & 0xff);
            headData[2] = (byte) ((jsonData.length >> 8) & 0xff);
            headData[3] = (byte) (jsonData.length & 0xff);
            synchronized (mSendLock) {
                OutputStream outputStream = mSock.getOutputStream();
                outputStream.write(headData);
                outputStream.write(jsonData);
                outputStream.flush();
            }

            final Object lock = new Object();
            final JSONObject[] result = {null};
            SYNCallback synCallback = (id1, json1) -> {
                try {
                    result[0] = json1;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                synchronized (lock) {
                    lock.notify();
                }
            };
            mIDMap.put(id, synCallback);
            synchronized (lock) {
                lock.wait(15000);
            }
            mIDMap.remove(id);
            return result[0];
        } catch (Exception e) {
            e.printStackTrace();
            logError(e);
        }
        return null;
    }

    private JSONObject imRespond(String command, JSONObject json, JSONObject original, OSNGeneralCallback callback) {
        if (json == null || original == null) {
            System.out.println("[format error] param null");
            return null;
        }
        logInfo(command + ": " + json);
        if (!isSuccess(json)) {
            logInfo("error: (" + command + ") " + errCode(json));
            if (callback != null)
                callback.onFailure(errCode(json));
            return null;
        }
        JSONObject data = OsnUtils.takeMessage(json, mOsnKey, mOsnID);
        if(data != null){
            data.put("msgHash", original.getString("hash"));
            mergeJson(json, data);
        }
        logInfo("data: "+(data==null?"null":data.toString()));
        if (callback != null) {
            if (data == null) {
                logInfo("error: (" + command + ") takeMessage");
                callback.onFailure("takeMessage");
            } else {
                if(data != null){
                    data.put("msgHash", original.getString("hash"));
                }
                System.out.println("[QuoteMessage] imRespond :" + original);
                System.out.println("[QuoteMessage] data :" + data);
                callback.onSuccess(data);
            }
        }
        return data;
    }

    private JSONObject imRequest(String command, String to, JSONObject data, OSNGeneralCallback callback) {
        try {
            // logInfo(command+": "+(data==null?"null":data.toString()));
            System.out.println("@@@ imRequest command:" + command);
            System.out.println("@@@ imRequest command data:" + data);
            JSONObject json = OsnUtils.makeMessage(command, mOsnID, to, data, mOsnKey);
            // logInfo("command : " + json);
            if (callback != null) {
                mExecutor.execute(() -> {
                    System.out.println("@@@ imRequest command enc data" + json);
                    JSONObject result = sendPackage(json);
                    System.out.println("@@@ [QuoteMessage] imRequest command recv data" + result);
                    if(result != null){
                        System.out.println("[QuoteMessage] imRespond 1");
                        imRespond(command, result, json, callback);
                    }else{
                        callback.onFailure("null");
                        //logInfo("@@@@@@" + json.getString("id")+" result null");
                    }
                });
                return json;
            }
            System.out.println("@@@ imRequest command enc data" + json);
            JSONObject result = sendPackage(json);
            System.out.println("@@@ imRequest command recv data" + result);
            if(result != null){
                System.out.println("[QuoteMessage] imRequest command recv data" + result);
                return imRespond(command, result, json, null);
            }
            logInfo(json.getString("id")+" result null");
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
            if (callback != null)
                callback.onFailure(e.toString());
        }
        return null;
    }

    private JSONObject imRequestForResend(String command, String to, JSONObject data, OSNGeneralCallback callback) {
        if (callback != null) {
            imRequest(command, to, data, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    if(callback != null)
                        callback.onSuccess(data);
                }
                @Override
                public void onFailure(String error) {
                    // 重发机制

                    resendMessage(command, to, data, callback);
                }
            });
            return null;
        } else {
            // 没有回调
            return imRequest(command, to, data, null);
        }
    }

    private JSONObject imRequest2(String command, String to, JSONObject data, OSNGeneralCallback callback) {
        try {
            logInfo(command+": "+(data==null?"null":data.toString()));
            JSONObject json = OsnUtils.makeMessage(command, mOsnID, to, data, mOsnKey);
            logInfo("command : " + json);
            if (callback != null) {
                mExecutor.execute(() -> {
                    JSONObject result = sendPackage2(json);
                    if(result != null){
                        System.out.println("[QuoteMessage] imRespond 3");
                        imRespond(command, result, json, callback);
                    }else{
                        callback.onFailure("null");
                        logInfo(json.getString("id")+" result null @@@");
                    }
                });
                return json;
            }
            JSONObject result = sendPackage2(json);
            if(result != null){
                System.out.println("[QuoteMessage] imRespond 4");
                return imRespond(command, result, json, null);
            }
            logInfo(json.getString("id")+" result null");
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
            if (callback != null)
                callback.onFailure(e.toString());
        }
        return null;
    }

    private JSONObject imRequestForResend2(String command, String to, JSONObject data, OSNGeneralCallback callback) {
        if (callback != null) {
            imRequest2(command, to, data, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    if(callback != null)
                        callback.onSuccess(data);
                }
                @Override
                public void onFailure(String error) {
                    // 重发机制
                    resendMessage2(command, to, data, callback);
                }
            });
            return null;
        } else {
            // 没有回调
            return imRequest2(command, to, data, null);
        }
    }

    private boolean isSuccess(JSONObject json) {
        if (json == null) {
            return false;
        }
        if (json == null || !json.containsKey("errCode"))
            return false;
        String errCode = json.getString("errCode");
        return errCode.equalsIgnoreCase("success") || errCode.equalsIgnoreCase("0:success");
    }
    private String errCode(JSONObject json) {
        if (json == null)
            return "null";
        if (!json.containsKey("errCode"))
            return "none";
        return json.getString("errCode");
    }
    private boolean login(String user, String key, String type, OSNGeneralCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("command", "Login");
            json.put("type", type);
            json.put("user", user);
            if (type.equalsIgnoreCase("user")) {
                String token = String.valueOf(System.currentTimeMillis());
                imLoginToken = token;
                mOsnListener.setConfig("imLoginToken", imLoginToken);
                json.put("token", this.imLoginToken);
            } else {
                json.put("token", this.imLoginToken);
            }
            json.put("platform", "android");
            json.put("deviceID", mDeviceID);
            json.put("ver", "2");
            String challenge = String.valueOf(System.currentTimeMillis());
            System.out.println("@@@ user Login key:" + key);
            json.put("challenge", aesEncrypt(challenge, key));
            System.out.println("@@@ user Login data:" + json);
            JSONObject data = sendPackage(json);
            System.out.println("@@@ user Login return data:" + data);
            if (!isSuccess(data)) {
                String errCode = errCode(data);
                if (errCode.startsWith("10032:")) {
                    // 执行退出登录 !!!!!!!
                    mOsnListener.onConnectFailed("-1:KickOff");

                    return false;
                }
                if (callback != null)
                    callback.onFailure(errCode(data));
                return false;
            }

            String content = aesDecrypt(data.getJSONObject("content").getString("data"), key);
            System.out.println("@@@ user Login content:" + content);
            data = JSON.parseObject(content);
            /*String token = data.getString("token");
            if (token != null) {
                this.imLoginToken = token;
                mOsnListener.setConfig("imLoginToken", imLoginToken);
            }*/


            String challenge2 = data.getString("challenge");
            if (challenge2 == null) {
                logInfo("error message.");
                if (callback != null)
                    callback.onFailure(errCode(data));
                return false;
            }
            String challenge1 = String.valueOf(Long.parseLong(data.getString("challenge"))-1);
            if(!challenge.equalsIgnoreCase(challenge1)){
                logInfo("challenge no match");
                if (callback != null)
                    callback.onFailure(errCode(data));
                return false;
            }

            System.out.println("@@@ login return data : " + data);

            String osnID = mOsnID;
            mAesKey = data.getString("aesKey");
            mOsnID = data.getString("osnID");

            String osnKey = data.getString("osnKey");
            if (osnKey != null) {
                if (osnKey.length() > 0) {
                    mOsnKey = osnKey;
                }
            }

            mServiceID = data.getString("serviceID");

            mOsnListener.setConfig("osnID", mOsnID);
            mOsnListener.setConfig("osnKey", mOsnKey);
            mOsnListener.setConfig("aesKey", mAesKey);
            mOsnListener.setConfig("serviceID", mServiceID);

            mLogined = true;
            mOsnListener.onConnectSuccess("logined");
            if (callback != null)
                callback.onSuccess(json);
//            new Thread(()->{
//                try {
//                    Thread.sleep(10);
//                    synchronized (mHearter){
//                        mHearter.notify();
//                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }).start();
            System.out.println("@@@ start workSyncing osnID : " + mOsnID);
            new Thread(()-> workSyncing(mOsnID)).start();
            return true;
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
        }
        return false;
    }

    private JSONObject getLoginInfo(String osnId){

        try {
            JSONObject data = new JSONObject();
            data.put("command", "GetLoginInfo");
            data.put("from", osnId);

            JSONObject result = sendPackage(data);
            return result;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;

    }

    private boolean login2(String osnId, String password, String password2, OSNGeneralCallback callback) {
        try {
            System.out.println("@@@@ getLoginInfo id : " + osnId);
            JSONObject result = getLoginInfo(osnId);
            if (result == null) {
                System.out.println("@@@@ getLoginInfo error : " + result);
                return false;
            }

            if (!isSuccess(result)){
                System.out.println("@@@@ getLoginInfo error : " + result);
                return false;
            }

            String content = result.getString("content");
            JSONObject contentJson = JSONObject.parseObject(content);

            String[] pwdArr = password.split("-");
            JSONObject decData = takeMessage(contentJson, pwdArr[1], osnId);
            System.out.println("@@@@ getLoginInfo decode data : " + decData);
            String serviceId = decData.getString("serviceID");

            mServiceID = serviceId;

            return loginByOsnId(osnId, password, password2, decData);

        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
        }
        return false;
    }


    private boolean loginByOsnId(String osnId, String password, String password2, JSONObject loginData){

        try {
            /**
             * [0] VER2
             * [1] 通信私钥
             * [2] 暗私钥
             * **/
            String[] pwdArr = password.split("-");

            // 创建暗公钥
            String shadowPwd = pwdArr[2];
            byte[] priEnc = new byte[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                priEnc = Base64.getDecoder().decode(shadowPwd);
            }
            //System.out.println("@@@@ loginByOsnId aesDecrypt begin.");
            byte[] pri = aesDecrypt(priEnc, OsnUtils.sha256(password2.getBytes()));

            System.out.println("@@@ [login pri2 ] : " + ECUtils.toHexString(pri));

            System.out.println("@@@@ loginByOsnId aesDecrypt : " + pri);
            String pub2 = ECUtils.pri2pub(pri);
            System.out.println("@@@@ loginByOsnId pub2 : " + pub2.length());


            // 创建token
            String token = String.valueOf(System.currentTimeMillis());
            imLoginToken = token;
            mOsnListener.setConfig("imLoginToken", imLoginToken);

            // 获取service id ，来自GetLoginInfo的result
            String serviceId = loginData.getString("serviceId");
            if (serviceId == null) {
                serviceId = loginData.getString("serviceID");
            }
            mServiceID = serviceId;
            mOsnListener.setConfig("serviceID", mServiceID);

            // 生成签名

            String calc = osnId + loginData.getString("challenge") + token;
            String hash = ECUtils.osnHash(calc.getBytes());
            String ShadowPriKey = ECUtils.toOsnPrivacy(pri);
            System.out.println("@@@@ loginByOsnId LoginV2 ShadowPriKey : " + ShadowPriKey);
            String sign = ECUtils.osnSign(ShadowPriKey, hash.getBytes());

            JSONObject content = new JSONObject();

            content.put("pubKey", pub2);
            content.put("token", token);
            content.put("sign", sign);

            System.out.println("@@@@ loginByOsnId LoginV2 content : " + content);
            mOsnID = osnId;
            mOsnKey = pwdArr[1];
            JSONObject result = imRequest("LoginV2", mServiceID, content, null);
            System.out.println("@@@@ loginByOsnId LoginV2 result : " + result);
            if (result == null) {
                return false;
            }
            if (!isSuccess(result)) {
                return false;
            }

            System.out.println("@@@@ loginByOsnId LoginV2 success.");




            JSONObject decData = takeMessage(result, pwdArr[1], osnId);
            if (decData == null) {
                String content2 = result.getString("content");
                JSONObject contentJson = null;
                if (content2 == null) {
                    contentJson = result;
                }

                contentJson = JSONObject.parseObject(content2);
                decData = takeMessage(contentJson, pwdArr[1], osnId);
                if (decData == null) {
                    System.out.println("@@@@ loginByOsnId take message failed.");
                    return false;
                }
            }

            System.out.println("@@@@ loginByOsnId decode data : " + decData);

            mAesKey = decData.getString("aesKey");
            mOsnID = osnId;
            mOsnKey = pwdArr[1];

            System.out.println("@@@@ loginByOsnId mOsnKey : " + mOsnKey);

            mOsnListener.setConfig("osnID", mOsnID);
            mOsnListener.setConfig("osnKey", mOsnKey);
            mOsnListener.setConfig("aesKey", mAesKey);
            mOsnListener.setConfig("shadowKey", pwdArr[2]);


            System.out.println("@@@ start workSyncing osnID : " + mOsnID);
            new Thread(()-> workSyncing(mOsnID)).start();
            return true;
        } catch (Exception e) {

            System.out.println("@@@@ " + e.getMessage());
        }

        return false;

    }



    private void setMsgSync(long timestamp) {
        if(!mMsgSynced)
            return;
        if (timestamp < mMsgSyncID)
            return;
        mMsgSyncID = timestamp;
        if (mOsnListener != null)
            mOsnListener.setConfig("msgSync", String.valueOf(mMsgSyncID));
    }
    private void mergeJson(JSONObject json, JSONObject data){
        if (json == null || data == null) {
            return;
        }
        data.put("receive_from", json.getString("from"));
        data.put("receive_to", json.getString("to"));
        data.put("receive_hash", json.getString("hash"));
        data.put("receive_timestamp", json.getString("timestamp"));
    }
    private List<OsnMessageInfo> getMessages(JSONObject json) {
        List<OsnMessageInfo> messages = new ArrayList<>();
        if (json == null) {
            return messages;
        }
        try {
            JSONArray array = json.getJSONArray("msgList");
            logInfo("msgList: " + array.size());

            for (Object o : array) {
                json = JSON.parseObject((String) o);
                if (json.containsKey("command") && json.getString("command").equalsIgnoreCase("Message")) {
                    JSONObject data = takeMessage(json, mOsnKey, mOsnID);
                    if (data != null) {
                        mergeJson(json, data);
                        messages.add(OsnMessageInfo.toMessage(data));
                    }
                }
            }
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
        }
        return messages;
    }
    private void completePackage(JSONObject json){
        JSONObject data = null;
        if(mHearted)
            data = sendPackage(json);
        if(data == null) {
            //for(int i = 0; i < 3; ++i) {
            data = shortMessage(json, null);
                /*if(data != null)
                    break;*/
            //}
        }
        logInfo("Complete Replay: "+(data == null ? "null" : data.toString()));
    }


    public void completeMessage(String hash, String from){
        try {
            JSONObject data = new JSONObject();
            data.put("hash", hash);
            data.put("sign", ECUtils.osnSign(mOsnKey, hash.getBytes()));
            //System.out.println("@@@   completeMessage from : " + from);
            //System.out.println("@@@   completeMessage to : " + mOsnID);
            JSONObject json = wrapMessage("Complete", mOsnID, mServiceID, data, mOsnKey);
            //System.out.println("@@@   completeMessage send begin.");
            completePackage(json);
            //System.out.println("@@@   completeMessage send end.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    private String completeMessage(JSONObject info){
        try {
            String hash = info.getString("receive_hash");
            if (hash == null) {
                hash = info.getString("hash");
            }
            if (hash == null) {
                return hash;
            }
            pushComplete(hash);
            /*JSONObject data = new JSONObject();
            data.put("hash", hash);
            data.put("sign", ECUtils.osnSign(mOsnKey, hash.getBytes()));
            JSONObject json = wrapMessage("Complete", mOsnID, info.getString("from"), data, mOsnKey);
            completePackage(json);*/
            return hash;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    private void completeMessage(List<JSONObject> infos){
        try {
            HashMap<String, String> receiptList = new HashMap<>();
            for(JSONObject info : infos){
                String hash = info.getString("hash");
                String sign = ECUtils.osnSign(mOsnKey, hash.getBytes());
                receiptList.put(hash, sign);
            }
            JSONObject data = new JSONObject();
            data.put("receiptList", receiptList);
            JSONObject json = wrapMessage("Complete", mOsnID, mServiceID, data, mOsnKey);
            completePackage(json);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    private void completeMessages(List<String> hashSet){
        try {
            if (hashSet == null) {
                return;
            }
            if (hashSet.size() == 0) {
                return;
            }
            HashMap<String, String> receiptList = new HashMap<>();
            for(String hash : hashSet){
                //String hash = info.getString("hash");
                String sign = ECUtils.osnSign(mOsnKey, hash.getBytes());
                receiptList.put(hash, sign);
            }
            JSONObject data = new JSONObject();
            data.put("receiptList", receiptList);
            JSONObject json = wrapMessage("Complete", mOsnID, mServiceID, data, mOsnKey);
            completePackage(json);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public boolean syncGroup() {
        try {
            JSONObject json = imRequest("GetGroupList", mServiceID, null, null);
            if (json == null)
                return false;
            JSONArray groupList = json.getJSONArray("groupList");
            System.out.println("sync group cleanGroupFav ");
            mOsnListener.cleanGroupFav();
            for (Object o : groupList) {
                OsnGroupInfo groupInfo = new OsnGroupInfo();
                groupInfo.groupID = (String) o;
                System.out.println("sync group : " + groupInfo.groupID);
                mOsnListener.onGroupUpdate("SyncGroup", groupInfo, null);
            }
            return true;
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
        }
        return false;
    }

    public boolean syncFriend() {
        try {
            System.out.println("@@@ syncFriend begin."+mServiceID);
            JSONObject json = imRequest("GetFriendList", mServiceID, null, null);
            if (json == null) {
                logInfo(" @@@   friend list is null");
                return false;
            }
            JSONArray friendList = json.getJSONArray("friendList");
            List<OsnFriendInfo> friendInfoList = new ArrayList<>();
            if(friendList != null){
                for (Object o : friendList) {
                    OsnFriendInfo friendInfo = new OsnFriendInfo();
                    friendInfo.state = OsnFriendInfo.Syncst;
                    friendInfo.userID = mOsnID;
                    friendInfo.friendID = (String) o;
                    friendInfoList.add(friendInfo);
                }
            }
            if (friendInfoList.size() != 0)
                mOsnListener.onFriendsUpdate(friendInfoList);
            return true;
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
        }
        return false;
    }
    private long syncMessage(long timestamp, int count) {
        try {
            JSONObject data = new JSONObject();
            data.put("timestamp", timestamp);
            data.put("count", count);
            JSONObject json = imRequest("MessageSync", mServiceID, data, null);
            if (json != null) {
                JSONArray array = json.getJSONArray("msgList");
                if(array != null){
                    logInfo("msgList: " + array.size());

                    boolean flag = false;
                    List<JSONObject> messageInfos = new ArrayList<>();

                    for (Object o : array) {
                        data = JSON.parseObject((String) o);
                        if (data.getString("command").equalsIgnoreCase("Message")) {
                            messageInfos.add(data);
                            flag = true;
                        } else {
                            if (flag) {
                                flag = false;
                                handleMessageReceive(messageInfos);
                                messageInfos.clear();
                            }
                            handleMessage((String) o);
                        }
                        timestamp = data.getLongValue("timestamp");
                    }
                    if (!messageInfos.isEmpty())
                        handleMessageReceive(messageInfos);
                    return timestamp;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logError(e);
        }
        return 0;
    }
    private JSONObject shortMessage(JSONObject json, OSNGeneralCallback callback){
        try{
            System.out.println("[QuoteMessage] shortMessage json:" + json);
            String msgHash = json.getString("hash");
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(mHost, mPort), 5000);
                if (!socket.isConnected()) {
                    logInfo("connect failure");
                    if(callback != null)
                        callback.onFailure("connect failure");
                    return null;
                }
                logInfo(json.getString("command") + ": " + json.toString());
                byte[] jsonData = json.toString().getBytes();
                byte[] headData = new byte[4];
                headData[0] = (byte) ((jsonData.length >> 24) & 0xff);
                headData[1] = (byte) ((jsonData.length >> 16) & 0xff);
                headData[2] = (byte) ((jsonData.length >> 8) & 0xff);
                headData[3] = (byte) (jsonData.length & 0xff);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(headData);
                outputStream.write(jsonData);
                outputStream.flush();

                byte[] head = new byte[4];
                while (true) {
                    if (workRead(socket, head, 4) <= 0)
                        break;
                    int length = ((head[0] & 0xff) << 24) | ((head[1] & 0xff) << 16) | ((head[2] & 0xff) << 8) | (head[3] & 0xff);
                    byte[] socketData = new byte[length];
                    if (workRead(socket, socketData, length) <= 0)
                        break;
                    String msg = new String(socketData);
                    json = JSON.parseObject(msg);
                    logInfo("json: " + json.toString());
                    if (isSuccess(json)) {

                        if (callback != null) {
                            json.put("msgHash", msgHash);
                            callback.onSuccess(json);
                        }

                        /*socket.shutdownOutput();
                        socket.shutdownInput();
                        socket.close();
                        return json;*/
                    }else {
                        if (callback != null)
                            callback.onFailure(errCode(json));
                    }
                    socket.shutdownOutput();
                    socket.shutdownInput();
                    socket.close();
                    return json;
                }
            }catch (Exception e){
                logError(e);
                e.printStackTrace();
            }
            /*socket.shutdownOutput();
            socket.shutdownInput();*/
            if(socket.isConnected()) {
                socket.shutdownOutput();
                socket.shutdownInput();
            }
            socket.close();
        }catch (Exception e){
            logError(e);
            e.printStackTrace();
        }
        if(callback != null)
            callback.onFailure("");
        return null;
    }

    private JSONObject shortMessage2(JSONObject json, OSNGeneralCallback callback){
        try{
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(mHost, mPort), 15000);
                if (!socket.isConnected()) {
                    logInfo("connect failure");
                    if(callback != null)
                        callback.onFailure("connect failure");
                    return null;
                }
                logInfo(json.getString("command") + ": " + json.toString());
                byte[] jsonData = json.toString().getBytes();
                byte[] headData = new byte[4];
                headData[0] = (byte) ((jsonData.length >> 24) & 0xff);
                headData[1] = (byte) ((jsonData.length >> 16) & 0xff);
                headData[2] = (byte) ((jsonData.length >> 8) & 0xff);
                headData[3] = (byte) (jsonData.length & 0xff);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(headData);
                outputStream.write(jsonData);
                outputStream.flush();

                byte[] head = new byte[4];
                while (true) {
                    if (workRead(socket, head, 4) <= 0)
                        break;
                    int length = ((head[0] & 0xff) << 24) | ((head[1] & 0xff) << 16) | ((head[2] & 0xff) << 8) | (head[3] & 0xff);
                    byte[] socketData = new byte[length];
                    if (workRead(socket, socketData, length) <= 0)
                        break;
                    String msg = new String(socketData);
                    json = JSON.parseObject(msg);
                    logInfo("json: " + json.toString());
                    if (isSuccess(json)) {

                        if (callback != null)
                            callback.onSuccess(json);
                        /*socket.shutdownOutput();
                        socket.shutdownInput();
                        socket.close();
                        return json;*/
                    }else {
                        if (callback != null)
                            callback.onFailure(errCode(json));
                    }
                    socket.shutdownOutput();
                    socket.shutdownInput();
                    socket.close();
                    return json;
                }
            }catch (Exception e){
                logError(e);
                e.printStackTrace();
            }
            /*socket.shutdownOutput();
            socket.shutdownInput();*/
            if(socket.isConnected()) {
                socket.shutdownOutput();
                socket.shutdownInput();
            }
            socket.close();
        }catch (Exception e){
            logError(e);
            e.printStackTrace();
        }
        if(callback != null)
            callback.onFailure("");
        return null;
    }

    private void handleAddFriend(JSONObject data) {
        try {
            OsnRequestInfo request = new OsnRequestInfo();
            request.reason = data.getString("reason");
            request.userID = data.getString("receive_from");
            request.friendID = data.getString("receive_to");
            request.timeStamp = data.getLong("receive_timestamp");
            request.isGroup = false;
            request.data = data;
            mOsnListener.onRecvRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
            logError(e);
        }
    }
    private void handleAgreeFriend(JSONObject data) {
        logInfo("agreeFriend data: " + data.toString());
    }
    private void handleInviteGroup(JSONObject data) {
        try {
            //邀请加入群组 originalUser 邀请 friendID 加入 userID
            OsnRequestInfo request = new OsnRequestInfo();
            request.reason = data.getString("reason");
            request.userID = data.getString("receive_from");
            request.friendID = data.getString("receive_to");
            request.timeStamp = data.getLong("receive_timestamp");
            request.originalUser = data.getString("originalUser");
            request.isGroup = true;
            request.isApply = false;
            request.data = data;
            mOsnListener.onRecvRequest(request);
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
        }
    }
    private void handleJoinGroup(JSONObject data) {
        try {
            System.out.println("[OSNManager] handleJoinGroup data:" + data);
            //审批邀请入群 originalUser 邀请 target 加入 userID
            //审批加入群组 targetUser 申请加入 userID
            OsnRequestInfo request = new OsnRequestInfo();
            request.reason = data.getString("reason");
            request.userID = data.getString("receive_from");
            request.friendID = data.getString("receive_to");
            request.timeStamp = data.getLong("receive_timestamp");
            request.originalUser = data.getString("originalUser");
            request.targetUser = data.getString("userID");
            request.isGroup = true;
            request.isApply = true;
            request.data = data;
            mOsnListener.onRecvRequest(request);
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
        }
    }

    private boolean checkGroupMessage(JSONObject data) {
        String originalUser = data.getString("originalUser");
        if (originalUser == null) {
            return false;
        }
        String text = data.getString("content");
        if (text == null) {
            return false;
        }
        String sign = data.getString("sign");
        if (sign == null) {
            return false;
        }

        String calc = originalUser + text + data.getString("timestamp");
        String hash = ECUtils.osnHash(calc.getBytes());
        return ECUtils.osnVerify(originalUser, hash.getBytes(), sign);
    }
    private void handleMessageReceive(List<JSONObject> json) {
        try {
            List<OsnMessageInfo> messageInfos = new ArrayList<>();
            for (JSONObject o : json) {
                JSONObject data = takeMessage(o, mOsnKey, mOsnID);

                if (data != null) {
                    //System.out.println("thread message   message = " + data);

                    String from = o.getString("from");
                    if (from.startsWith("OSNG")) {
                        if (!checkGroupMessage(data)) {
                            // 没有签名的可能会有群造假问题
                            //System.out.println("thread message    message group message has no sign.");
                        }
                    }


                    mergeJson(o, data);
                    data.put("hash0", o.getString("hash0"));
                    OsnMessageInfo messageInfo = OsnMessageInfo.toMessage(data);
                    messageInfos.add(messageInfo);
                }
                //completeMessage(o);
            }
            List<String> hashSet = mOsnListener.onRecvMessage(messageInfos);
            pushComplete(hashSet);
            //completeMessages(hashSet);
        } catch (Exception e) {
            e.printStackTrace();
            logError(e);
        }
    }
    private void handleMessageSet(JSONObject data) {
        try {
            OsnMessageInfo messageInfo = OsnMessageInfo.toMessage(data);
            mOsnListener.onRecvMessage(Collections.singletonList(messageInfo));
        } catch (Exception e) {
            e.printStackTrace();
            logError(e);
        }
    }
    private void handleGroupUpdate(JSONObject data, JSONObject notice) {
        try {
            JSONArray array = data.getJSONArray("infoList");
            OsnGroupInfo groupInfo = OsnGroupInfo.toGroupInfo(data);
            groupInfo.notice = notice;
            if (notice.containsKey("timestamp")) {
                groupInfo.noticeServerTime = notice.getLongValue("timestamp");
            }
            mOsnListener.onGroupUpdate(data.getString("state"), groupInfo, array == null ? null : array.toJavaList(String.class));
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
        }
    }
    private void handleUserUpdate(JSONObject data) {
        try {
            OsnUserInfo userInfo = OsnUserInfo.toUserInfo(data);
            JSONArray array = data.getJSONArray("infoList");
            List<String> keys = new ArrayList<>(array.toJavaList(String.class));
            mOsnListener.onUserUpdate(userInfo, keys);
        } catch (Exception e) {
            e.printStackTrace();
            logError(e);
        }
    }
    private void handleFriendUpdate(JSONObject data) {
        try {
            OsnFriendInfo friendInfo = OsnFriendInfo.toFriendInfo(data);
            mOsnListener.onFriendUpdate(Collections.singletonList(friendInfo));
        } catch (Exception e) {
            e.printStackTrace();
            logError(e);
        }
    }
    private void handleServiceInfo(JSONObject data) {
        try{
            String type = data.getString("type");
            if(type.equalsIgnoreCase("infos")){
                mOsnListener.onServiceInfo(OsnServiceInfo.toServiceInfos(data));
            }
        }catch (Exception e){
            e.printStackTrace();
            logError(e);
        }
    }

    private void handleResultAddFriend(JSONObject data) {
        try{
            System.out.println("@@@@     data="+data);

        }catch (Exception e){
            e.printStackTrace();
            logError(e);
        }
    }

    private void handleResult(JSONObject data) {
        try{
            // result的处理，首先是进行解密
            OsnUtils.logInfo("[handleResult] : " + data);
            //JSONObject msgData = getContent(data, mTempKey, mTempID);
            //OsnUtils.logInfo("[Result] : " + msgData);
            List<OsnLitappInfo> litappInfos = new ArrayList<>();
            JSONArray array = data.getJSONArray("dapps");
            for(Object o : array){
                litappInfos.add(OsnLitappInfo.toLitappInfo((JSONObject) o));
            }
            logInfo(JSONObject.toJSONString(litappInfos));
            mOsnListener.onFindResult(litappInfos);


            // 用tempAcc 对resultData进行解密




        }catch (Exception e){
            e.printStackTrace();
            logError(e);
        }
    }

    private void handleEncData(JSONObject data) {
        try{
            String command = data.getString("command");
            System.out.println("@@@@     command="+command);
            if (command.equals("Recall")) {


                if (!mRecallEnable) {
                    System.out.println("=== not allow recall ===");
                    return;
                }

                String from = data.getString("from");
                String to = data.getString("to");
                String messageHash = data.getString("messageHash");
                String messageHash0 = data.getString("messageHash0");
                String sign = data.getString("sign");

                /*if (!mOsnID.equals(to)){
                    // 出错处理
                    System.out.println("@@@ 1");
                    return;
                }*/

                String calc = command + from + to + messageHash;
                String hash = ECUtils.osnHash(calc.getBytes());
                if (!ECUtils.osnVerify(from, hash.getBytes(), sign)){
                    // 出错处理
                    System.out.println("@@@ 2");
                    return;
                }
                //发送时间小于两分钟,撤销消息
                mOsnListener.onReceiveRecall(data);


            } else if (command.equals("DeleteMessage")) {

                if (!mDeleteEnable) {
                    System.out.println("=== not allow recall ===");
                    return;
                }

                String from = data.getString("from");
                String to = data.getString("to");
                String messageHash = data.getString("messageHash");
                String sign = data.getString("sign");

                /*if (!mOsnID.equals(to)){
                    // 出错处理
                    System.out.println("@@@ 1");
                    return;
                }*/

                String calc = command + from + to + messageHash;
                String hash = ECUtils.osnHash(calc.getBytes());
                if (!ECUtils.osnVerify(from, hash.getBytes(), sign)){
                    // 出错处理
                    System.out.println("@@@ 2");
                    return;
                }
                //发送时间小于两分钟,撤销消息
                mOsnListener.onDeleteMessageTo(data);

            }





        }catch (Exception e){
            e.printStackTrace();
            logError(e);
        }
    }

    private void handleGroupInfo(JSONObject data) {
        try{
            mOsnListener.onGroupInfo(data);
        }catch (Exception e){
            e.printStackTrace();
            logError(e);
        }
    }

    private void handleUserInfo(JSONObject data) {
        try{
            mOsnListener.onUserInfo(data);
        }catch (Exception e){
            e.printStackTrace();
            logError(e);
        }
    }



    private void handleMessage(String msg) {
        try {
            JSONObject json = JSON.parseObject(msg);
            String command = json.getString("command");
            //logInfo(command + ": " + json.toString());

            System.out.println("recv message command : " + command);
            if (command.equals("Replay")){
                System.out.println("@@@ command 1 : " + command);
            }

            long timestamp = 0;
            if (json.containsKey("timestamp")) {
                timestamp = json.getLongValue("timestamp");
            }
            setMsgSync(json.getLongValue("timestamp"));

            JSONObject data = null;
            if (command.equalsIgnoreCase("Result")){
                data = OsnUtils.takeMessage(json, mTempKey, mTempID);
                System.out.println("recv message result : " + data);
            } else {
                //System.out.println("recv message mOsnID : " + mOsnID);
                //System.out.println("recv message mOsnKey : " + mOsnKey);
                data = OsnUtils.takeMessage(json, mOsnKey, mOsnID);
            }

            if (data == null) {
                //System.out.println("recv message [" + command + "] error: takeMessage");
                //System.out.println("recv message take message error : " + json);
                //completeMessage(json);
                return;
            }
            //logInfo(command + ": "+data.toString());
            mergeJson(json, data);
            switch (command) {
                case "RejectMember":
                case "RejectFriend":
                    completeMessage(data);
                    break;
                case "AddFriend":
                    handleAddFriend(data);
                    completeMessage(data);
                    break;
                case "AgreeFriend":
                    handleAgreeFriend(data);
                    break;
                case "Invitation":
                    //case "InviteGroup":
                    handleInviteGroup(data);
                    completeMessage(data);
                    break;
                case "JoinGroup":
                    handleJoinGroup(data);
                    completeMessage(data);
                    break;
                case "Message":
                    pushMessage(json);
                    //handleMessageReceive(Collections.singletonList(json));
                    break;
                case "SetMessage":
                    handleMessageSet(data);
                    break;
                case "UserUpdate":
                    handleUserUpdate(data);
                    completeMessage(data);
                    break;
                case "FriendUpdate":
                    handleFriendUpdate(data);
                    break;
                case "GroupUpdate":
                    String hash = json.getString("hash");
                    String state = data.getString("state");
                    System.out.println("[GroupUpdate] state : " + state+
                            " hash : " + hash);
                    if (hash.equalsIgnoreCase("ViEAjidK14BdkEYmA3HPP819OAex9A4OjUF7Z1Y4nVU=")
                            || hash.equalsIgnoreCase("zQqSLSCkNJXa6n2adxW+QrOyz29tYNV6yx/mvp9qKYA=")
                            || hash.equalsIgnoreCase("up0FkZ0rtyoWGnRYIIkUoK/44edELKuTO906BE9elCI=")
                            || hash.equalsIgnoreCase("bl57nnffA55yV1uabigyHAlDeyoZe31zzQcy89zuwKo=")
                            || hash.equalsIgnoreCase("WOKvsSVgOUOIN1lkaqQAqJg9qRkok5S6rDAU5m9yPlY=")
                            || hash.equalsIgnoreCase("W9n3sF/VRKZCFVd5u1hlzD983b6AS+hXnqarvc7ZYU4=")
                    ){
                        String ret = completeMessage(data);
                        System.out.println("[GroupUpdate] completeMessage hash : " + ret);
                    } else
                    {
                        handleGroupUpdate(data, json);
                        completeMessage(data);
                    }
                    break;
                case "KickOff":
                    mOsnListener.onConnectFailed("-1:KickOff");
                    break;
                case "ServiceInfo":
                    handleServiceInfo(data);
                    break;
                case "Result":
                    handleResult(data);
                    //completeMessage(data);
                    break;
                case "EncData":
                    handleEncData(data);
                    completeMessage(data);
                    break;
                /*case "UpAttribute":
                    handleAddFriend(data);
                    break;*/
                case "GroupInfo":
                    System.out.println("[OSNManager] receive GroupInfo : " + data);
                    handleGroupInfo(data);
                    break;
                case "UserInfo":
                    System.out.println("@@@   receive UserInfo");
                    handleUserInfo(data);
                    break;
                default:
                    logInfo("@@@  unknown command: " + command);
                    break;
            }
        } catch (Exception e) {
            System.out.println("@@@ error message : " + msg);
            logError(e);
            e.printStackTrace();

        }
    }

    private void workSyncing(String osnID){
        if (osnID == null) {
            return;
        }
        if (!mInitSync || (osnID != null && !osnID.equalsIgnoreCase(mOsnID))) {
            if (syncFriend() && syncGroup()) {
                mInitSync = true;
                mOsnListener.setConfig("initSync", "true");
            }
        }
//        long timestamp = mMsgSyncID;
//        while (true) {
//            timestamp = syncMessage(timestamp, 20);
//            if (timestamp == 0 || timestamp == mMsgSyncID)
//                break;
//            mMsgSyncID = timestamp;
//        }
//        mMsgSynced = true;
//        setMsgSync(mMsgSyncID);
    }

    public List<JSONObject> msgQueue = new ArrayList<>();
    public Object msgQueuelock = new Object();
    public long lastTime = 0;
    private void workProcessMessage() {
        System.out.println("start thread message begin.");
        while (true) {
            System.out.println("thread message begin : " + System.currentTimeMillis());
            processMessageQueue();
        }

    }
    private void processMessageQueue() {

        List<JSONObject> msgList = new ArrayList<>();

        try {

            // 主线程等待消息
            synchronized (msgQueuelock) {
                msgQueuelock.wait(500);
            }

            //System.out.println("thread message recv notify");

            if (msgQueue.size() == 0) {
                return;
            }

            //System.out.println("thread message check time");
            // 计算等待时间
            long nowTime = System.currentTimeMillis();
            long deltaTime = nowTime - lastTime;
            if (deltaTime < 1000) {
                // 时间不够，等待
                try {
                    Thread.sleep(1000 - deltaTime);
                } catch (Exception e) {
                }
            }

            //System.out.println("thread message wait end.");

            // 处理消息
            synchronized (msgQueuelock) {
                msgList.addAll(msgQueue);
                msgQueue.clear();
            }
            lastTime = System.currentTimeMillis();
            System.out.println("thread message count : " + msgList.size());
            handleMessageReceive(msgList);
            System.out.println("thread message handleMessageReceive end.");

        } catch (Exception e) {

        }
    }
    private void pushMessage(JSONObject json) {
        //System.out.println("thread message push");
        synchronized (msgQueuelock){
            //System.out.println("thread message push 2");
            msgQueue.add(json);
            msgQueuelock.notify();
        }
    }



    public List<String> msgStrQueue = new ArrayList<>();
    public Object msgStrQueueLock = new Object();
    private void workProcessMessageStr() {
        while (true) {
            processMessageQueueStr();
        }
    }
    private void processMessageQueueStr() {
        List<String> msgList = new ArrayList<>();
        try {
            // 主线程等待消息
            synchronized (msgStrQueueLock) {
                msgStrQueueLock.wait(500);
            }

            if (msgStrQueue.size() == 0) {
                return;
            }

            // 处理消息
            synchronized (msgStrQueueLock) {
                msgList.addAll(msgStrQueue);
                msgStrQueue.clear();
            }
            for (String msg : msgList) {
                System.out.println("@@@ recv message process.");
                handleMessage(msg);
            }
        } catch (Exception e) {

        }
    }
    private void pushMessageStr(String msg) {
        synchronized (msgStrQueueLock){
            System.out.println("@@@ recv message push");
            msgStrQueue.add(msg);
            msgStrQueueLock.notify();
        }
    }




    public List<String> completeQueue = new ArrayList<>();
    public Object completeQueuelock = new Object();

    private void pushComplete(List<String> hashList) {
        synchronized (completeQueuelock){
            completeQueue.addAll(hashList);
            completeQueuelock.notify();
        }
    }
    private void pushComplete(String hash) {
        synchronized (completeQueuelock){
            completeQueue.add(hash);
            completeQueuelock.notify();
        }
    }
    private void workProcessComplete() {
        System.out.println("start thread message complete begin.");
        while (true) {
            //System.out.println("thread message begin : " + System.currentTimeMillis());
            processCompleteQueue();
        }
    }
    private void processCompleteQueue() {

        List<String> hashList = new ArrayList<>();

        try {

            // 主线程等待消息
            /*synchronized (completeQueuelock) {
                completeQueuelock.wait(2000);
            }*/

            //System.out.println("thread message recv notify");

            if (completeQueue.size() == 0) {
                return;
            }

            Thread.sleep(1000);

            // 获取hash
            synchronized (completeQueuelock) {
                hashList.addAll(completeQueue);
                completeQueue.clear();
            }
            System.out.println("thread message complete count:" + hashList.size());
            completeMessages(hashList);
            System.out.println("thread message completeMessages end");

        } catch (Exception e) {

        }

    }

    private int workRead(Socket socket, byte[] data, int size) throws IOException {
        int ret = 0, idx = 0;
        InputStream inputStream = socket.getInputStream();
        while(idx < size){
            ret = inputStream.read(data, idx, size-idx);
            if(ret <= 0){
                logInfo("sock read error: "+ret);
                break;
            }
            idx += ret;
        }
        return ret;
    }
    private int workRead(byte[] data, int size) throws IOException {
        return workRead(mSock, data, size);
    }
    private void workReceive(){
        logInfo("Start worker thread.");
        while (true) {
            try {
                logInfo("wait for network");
                if(!isAvailable)
                    mReader.acquire();
                logInfo("connect to server: " + mHost);
                mLogined = false;
                mHearted = false;
                mMsgSynced = false;
                mSock = new Socket();
                try {
                    mSock.connect(new InetSocketAddress(mHost, mPort), 3000);
                } catch (SocketTimeoutException e) {
                    logInfo(e.toString());
                } catch (Exception e) {
                    logInfo(e.toString());
                    Thread.sleep(1000);
                }
                if (!mSock.isConnected()) {
                    mSock.shutdownOutput();
                    mSock.shutdownInput();
                    mSock.close();
                    mExecutor.execute(() -> mOsnListener.onConnectFailed("sock connect error"));
                    continue;
                }
                //mHearter.release();
                synchronized (mHearter){
                    mHearter.notify();
                }
                mExecutor.execute(() -> mOsnListener.onConnectSuccess("connected"));
                logInfo("connect to server success");

                try {
                    byte[] head = new byte[4];
                    while (true) {
                        if(workRead(head, 4) <= 0)
                            break;
                        int length = ((head[0] & 0xff) << 24) | ((head[1] & 0xff) << 16) | ((head[2] & 0xff) << 8) | (head[3] & 0xff);

                        byte[] data = new byte[length];
                        if(workRead(data, length) <= 0)
                            break;
                        String msg = new String(data);
                        JSONObject json = JSON.parseObject(msg);
                        String command = json.getString("command");
                        if (isCommandMessage(command)) {
                            pushMessageStr(msg);
                        } else {
                            mExecutor.execute(() -> {
                                //logInfo("workReceive: "+msg);

                                String id = json.getString("id");
                                if (id != null) {
                                    SYNCallback callback = mIDMap.get(id);
                                    if (callback != null) {
                                        callback.onCallback(id, json);
                                        return;
                                    }
                                }
                                handleMessage(msg);
                            });
                        }
                    }
                    mExecutor.execute(() -> mOsnListener.onConnectFailed("sock read error"));
                } catch (Exception e) {
                    logError(e);
                    e.printStackTrace();
                    mExecutor.execute(() -> mOsnListener.onConnectFailed(e.toString()));
                }
                mSock.shutdownOutput();
                mSock.shutdownInput();
                mSock.close();
            } catch (Exception e) {
                logError(e);
                e.printStackTrace();
                mExecutor.execute(() -> mOsnListener.onConnectFailed(e.toString()));
            }
        }
    }
    private boolean isCommandMessage(String command){
        System.out.println("@@@ recv message command : " + command);
        try {
            if (command.equalsIgnoreCase("Message")) {
                return true;
            }
            if (command.equalsIgnoreCase("GroupUpdate")) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }
    private void workHeater(){
        logInfo("Start heart thread.");

        JSONObject json = new JSONObject();
        json.put("command", "Heart");

        while (true) {
            try {
                synchronized (mHearter){
                    mHearter.wait(20000);
                }
                if (mSock != null && mSock.isConnected()) {
                    if(mLogined){
                        ++mHeartCount;
                        JSONObject result = sendPackage(json);
                        if (!isSuccess(result)) {
                            ++mTimeoutCount;
                            logInfo("heart timeout");
                            mSock.shutdownOutput();
                            mSock.shutdownInput();
                            mSock.close();
                        }else{
                            mHearted = true;
                        }
                    }else if(mOsnID != null){
                        loginWithOsnID(mOsnID, null);
                    }
                }
            } catch (Exception e) {
                logError(e);
                e.printStackTrace();
            }
        }
    }

    static boolean threadFlag = false;
    private void initWorker() {
        if (mSock != null)
            return;
        new Thread(this::workReceive).start();
        new Thread(this::workHeater).start();

        if (!threadFlag) {
            threadFlag = true;
            new Thread(this::workProcessMessage).start();
            new Thread(this::workProcessMessageStr).start();
            new Thread(this::workProcessComplete).start();
        }



        networkOn();
//        new Thread(()->{
//            try {
//                Thread.sleep(100);
//                reConnect();
//            }catch (Exception e){
//            }
//        }).start();
        //reConnect();
    }

    public static OSNManager Instance() {
        if (INST == null)
            INST = new OSNManager();
        return INST;
    }

    public void initSDK(String ip, OSNListener listener) {
        mHost = ip;
        mOsnListener = listener;
        if (listener == null) {
            return;
        }
        imLoginToken = mOsnListener.getConfig("imLoginToken");
        mOsnID = mOsnListener.getConfig("osnID");
        mOsnKey = mOsnListener.getConfig("osnKey");
        mShadowKey = mOsnListener.getConfig("shadowKey");
        mAesKey = mOsnListener.getConfig("aesKey");
        mServiceID = mOsnListener.getConfig("serviceID");
        mHideEnable1 = mOsnListener.getConfig("hideEnable1");
        mHideEnable2 = mOsnListener.getConfig("hideEnable2");
        mHideEnable3 = mOsnListener.getConfig("hideEnable3");

        {
            String str = mOsnListener.getConfig("mRecallEnable");
            if (str != null) {
                if (str.equals("0")) {
                    mRecallEnable = false;
                }
            }
        }

        {
            String str = mOsnListener.getConfig("mDeleteEnable");
            if (str != null) {
                if (str.equals("0")) {
                    mDeleteEnable = false;
                }
            }
        }




        String msgSync = mOsnListener.getConfig("msgSync");
        mMsgSyncID = (msgSync == null ? 0 : Long.parseLong(msgSync));
        if (mMsgSyncID == 0) {
            mMsgSyncID = System.currentTimeMillis();
            mOsnListener.setConfig("msgSync", String.valueOf(mMsgSyncID));
        }
        String initSync = mOsnListener.getConfig("initSync");
        mInitSync = initSync != null && initSync.equalsIgnoreCase("true");
        mDeviceID = mOsnListener.getConfig("deviceID");
        if (mDeviceID == null) {
            mDeviceID = UUID.randomUUID().toString();
            mOsnListener.setConfig("deviceID", mDeviceID);
        }
        initWorker();
    }
    public void createTempAccount(){
        //String tempID = mOsnListener.getConfig("tempID");
        //String tempKey = mOsnListener.getConfig("tempKey");
        if (mTempID == null || mTempKey == null) {
            String[] newID = ECUtils.createOsnID("user");

            if (mOsnListener != null) {
                mOsnListener.setConfig("tempID", newID[0]);
                mOsnListener.setConfig("tempKey", newID[1]);
            }

            mTempID = newID[0];
            mTempKey = newID[1];
        }
    }

    public String[] createOsnIdFromMnemonic(byte[] seed, String password) {
        return ECUtils.createOsnIdFromMnemonic(seed, password);
    }

    public String getShadowKeyPubId(String decPwd) {

        try {
            byte[] priEnc = new byte[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                priEnc = Base64.getDecoder().decode(mShadowKey);
            }

            byte[] pri = aesDecrypt(priEnc, OsnUtils.sha256(decPwd.getBytes()));
            System.out.println("@@@ dapp getShadowKeyPubId aesDecrypt : " + pri);
            String pub2 = ECUtils.pri2pub(pri);
            System.out.println("@@@ dapp getShadowKeyPubId pub2 : " + pub2.length());
            return pub2;
        } catch (Exception e) {

        }
        return "";

    }

    public String signByShadowKey(String hash, String decPwd) {
        try {
            byte[] priEnc = new byte[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                priEnc = Base64.getDecoder().decode(mShadowKey);
            }

            byte[] pri = aesDecrypt(priEnc, OsnUtils.sha256(decPwd.getBytes()));
            System.out.println("@@@@ signByShadowKey aesDecrypt : " + pri);
            String pub2 = ECUtils.pri2pub(pri);
            System.out.println("@@@@ signByShadowKey pub2 : " + pub2.length());


            // 生成签名

            String ShadowPriKey = ECUtils.toOsnPrivacy(pri);
            System.out.println("@@@@ signByShadowKey ShadowPriKey : " + ShadowPriKey);
            String sign = ECUtils.osnSign(ShadowPriKey, hash.getBytes());
            return sign;
        } catch (Exception e) {

        }
        return "";
    }

    public void setHideEnable1(String str) {
        if (mOsnListener != null)
            mOsnListener.setConfig("hideEnable1", str);
    }


    public void setEnable(String key, String value) {

        if (key == null || value == null) {
            return;
        }

        if (key.equals("recall")) {

            if (value.equals("0")) {
                mRecallEnable = false;
            } else {
                mRecallEnable = true;
            }

            mOsnListener.setConfig("mRecallEnable", value);


        } else if (key.equals("delete")) {

            if (value.equals("0")) {
                mDeleteEnable = false;
            } else {
                mDeleteEnable = true;
            }

            mOsnListener.setConfig("mDeleteEnable", value);
        }
    }
    public boolean getEnable(String key) {
        if (key == null) {
            return false;
        }
        if (key.equals("delete")) {
            return mDeleteEnable;
        }
        if (key.equals("recall")){
            return mRecallEnable;
        }

        return false;
    }

    public void setHideEnable2(String str) {
        if (mOsnListener != null)
            mOsnListener.setConfig("hideEnable2", str);
    }

    public void setHideEnable3(String str) {
        if (mOsnListener != null)
            mOsnListener.setConfig("hideEnable3", str);
    }

    public boolean getHideEnable() {
        //mHideEnable1 = mOsnListener.getConfig("hideEnable1");
        //mHideEnable2 = mOsnListener.getConfig("hideEnable2");

        if (mHideEnable1 != null) {
            if (mHideEnable1.equals("1")) {
                return true;
            }
        }
        if (mHideEnable2 != null) {
            if (mHideEnable2.equals("1")) {
                return true;
            }
        }

        return false;
    }

    public boolean getHideEnable3() {
        //mHideEnable1 = mOsnListener.getConfig("hideEnable1");
        //mHideEnable2 = mOsnListener.getConfig("hideEnable2");

        if (mHideEnable3 != null) {
            if (mHideEnable3.equals("1")) {
                return true;
            }
        }

        return false;
    }


    public void resetHost(String ip) {
        try {
            mHost = ip;
            logInfo("reset host to: "+ip);
            mOsnListener.onConnectFailed("reset host");
            if (mSock != null) {
                mSock.shutdownOutput();
                mSock.shutdownInput();
                mSock.close();
            }
            networkOn();
        } catch (Exception e) {
            e.printStackTrace();
            logError(e);
        }
    }
    public void networkOn(){
        isAvailable = true;
        if (mReader != null)
            mReader.release();
    }
    public void networkOff(){
        isAvailable = false;
    }

    public void register(String userName, String password, String serviceID, OSNGeneralCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("username", userName);
            json.put("password", password);
            imRequest("Register", serviceID, json, callback);
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
            if (callback != null)
                callback.onFailure(e.toString());
        }
    }
    public boolean loginWithOsnID(String userID, OSNGeneralCallback callback) {
        return login(userID, mAesKey, "osn", callback);
    }
    public boolean loginV2(String osnId, String password, String password2, OSNGeneralCallback callback) {
        try {
            return login2(osnId, password, password2, callback);
        } catch (Exception e) {

        }
        return false;
    }
    public boolean loginWithName(String userName, String password, OSNGeneralCallback callback) {
        try {
            return login(userName, b64Encode(sha256(password.getBytes())), "user", callback);
        } catch (Exception e) {

        }
        return false;
    }

    public void logout(OSNGeneralCallback callback) {
        try {
            mOsnID = null;
            mLogined = false;
            mInitSync = false;
            mOsnListener.setConfig("osnID", null);
            mOsnListener.setConfig("initSync", "false");

            if (mSock != null && mSock.isConnected()) {
                mSock.shutdownOutput();
                mSock.shutdownInput();
                mSock.close();
            }

            if (callback != null)
                callback.onSuccess(null);
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
            if (callback != null)
                callback.onFailure(e.toString());
        }
    }
    public String getUserID() {
        return mOsnID;
    }
    public String getServiceID() {
        return mServiceID;
    }
    public OsnUserInfo getUserInfo(String userID, OSNGeneralCallbackT<OsnUserInfo> callback) {
        try {

            if (callback == null) {
                JSONObject json = imRequest("GetUserInfo", userID, null, null);
                if (json != null)
                    return OsnUserInfo.toUserInfo(json);
            } else {
                imRequest("GetUserInfo", userID, null, new OSNGeneralCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            callback.onSuccess(OsnUserInfo.toUserInfo(data));
                        } catch (Exception e) {
                            logError(e);
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onFailure(error);
                    }
                });
            }
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
        }
        return null;
    }
    public OsnGroupInfo getGroupInfo(String groupID, OSNGeneralCallbackT<OsnGroupInfo> callback) {
        try {
            if (callback == null) {
                JSONObject json = imRequest("GetGroupInfo", groupID, null, null);
                if (json != null) {
                    System.out.println("[OSNManager] getGroupInfo 1 : " + json);
                    return OsnGroupInfo.toGroupInfo(json);
                }
            } else {
                System.out.println("[OSNManager] getGroupInfo 1 : " + groupID);
                imRequest("GetGroupInfo", groupID, null, new OSNGeneralCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            System.out.println("[OSNManager] getGroupInfo 2 : " + data);
                            callback.onSuccess(OsnGroupInfo.toGroupInfo(data));
                        } catch (Exception e) {
                            logError(e);
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        System.out.println("[OSNManager] GetGroupInfo failed. " + error);
                        callback.onFailure(error);
                    }
                });
            }
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
        }
        return null;
    }
    public void getGroupDetail(String groupID, String type, OSNGeneralCallback callback){
        try {
            JSONObject data = new JSONObject();
            if (type.equalsIgnoreCase("notice"))
                data.put("type", "notice");
            imRequest("GetGroupDetail", groupID, data, callback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public List<OsnMemberInfo> getMemberInfo(String groupID, OSNGeneralCallbackT<List<OsnMemberInfo>> callback) {

        getMemberInfoZone(groupID, 0, 25, callback);

        return null;

        /*try {
            if (callback == null) {
                JSONObject json = imRequest("GetMemberInfo", groupID, null, null);
                if (json != null)
                    return OsnMemberInfo.toMemberInfos(json);
            } else {
                imRequest("GetMemberInfo", groupID, null, new OSNGeneralCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            callback.onSuccess(OsnMemberInfo.toMemberInfos(data));
                        } catch (Exception e) {
                            logError(e);
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onFailure(error);
                    }
                });
            }
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
        }
        return null;*/
    }
    public void getMemberInfoZone(String groupID, int start, int count, OSNGeneralCallbackT<List<OsnMemberInfo>> callback){
        try {
            System.out.println("@@@ getMemberInfoZone start:"+start+ " size:"+count+ " group:"+groupID);
            JSONObject data = new JSONObject();
            data.put("begin", start);
            data.put("size", count);
            imRequest2("GetMemberZone", groupID, data, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess(OsnMemberInfo.toMemberInfos(data));
                    } catch (Exception e) {
                        logError(e);
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    System.out.println("@@@ getMemberInfoZone failed." + error);
                    callback.onFailure(error);
                }
            });
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
            callback.onFailure(e.getLocalizedMessage());
        }
    }
    public OsnServiceInfo getServiceInfo(String serviceID, OSNGeneralCallbackT<OsnServiceInfo> callback) {
        try {
            if (callback == null) {
                JSONObject json = imRequest("GetServiceInfo", serviceID, null, null);
                if (json != null)
                    return OsnServiceInfo.toServiceInfo(json);
            } else {
                imRequest("GetServiceInfo", serviceID, null, new OSNGeneralCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            callback.onSuccess(OsnServiceInfo.toServiceInfo(data));
                        } catch (Exception e) {
                            logError(e);
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onFailure(error);
                    }
                });
            }
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
        }
        return null;
    }
    public void findServiceInfo(String keyword, OSNGeneralCallbackT<List<OsnServiceInfo>> callback) {
        try {
            if(callback == null || keyword == null || keyword.isEmpty()){
                return;
            }
            JSONObject json = new JSONObject();
            json.put("keyword", keyword);
            json.put("type", "findService");
            imRequest("Broadcast", null, json, null);
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
        }
    }
    public OsnFriendInfo getFriendInfo(String friendID, OSNGeneralCallbackT<OsnFriendInfo> callback) {
        try {
            JSONObject data = new JSONObject();
            data.put("friendID", friendID);
            if (callback == null) {
                JSONObject json = imRequest("GetFriendInfo", mServiceID, data, null);
                if (json != null)
                    return OsnFriendInfo.toFriendInfo(json);
            } else {
                imRequest("GetFriendInfo", mServiceID, null, new OSNGeneralCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            callback.onSuccess(OsnFriendInfo.toFriendInfo(data));
                        } catch (Exception e) {
                            logError(e);
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onFailure(error);
                    }
                });
            }
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
        }
        return null;
    }
    public void modifyUserInfo(List<String> keys, OsnUserInfo userInfo, OSNGeneralCallback callback) {
        try {
            JSONObject data = new JSONObject();
            for (String k : keys) {
                if (k.equalsIgnoreCase("displayName"))
                    data.put("displayName", userInfo.displayName);
                else if (k.equalsIgnoreCase("portrait"))
                    data.put("portrait", userInfo.portrait);
                else if (k.equalsIgnoreCase("urlSpace"))
                    data.put("urlSpace", userInfo.urlSpace);
                else if (k.equalsIgnoreCase("describes"))
                    data.put("describes", userInfo.describes);
            }
            imRequestForResend("SetUserInfo", mServiceID, data, callback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public void modifyFriendInfo(List<String> keys, OsnFriendInfo friendInfo, OSNGeneralCallback callback) {
        try {
            JSONObject data = new JSONObject();
            data.put("friendID", friendInfo.friendID);
            for (String k : keys) {
                if (k.equalsIgnoreCase("remarks"))
                    data.put("remarks", friendInfo.remarks);
                else if (k.equalsIgnoreCase("state"))
                    data.put("state", friendInfo.state);
            }
            imRequestForResend("GetFriendInfo", mServiceID, data, callback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }



    public List<String> getFriendList(OSNGeneralCallbackT<List<String>> callback) {
        try {
            if (callback == null) {
                JSONObject json = imRequest("GetFriendList", mServiceID, null, null);
                if (json != null) {
                    List<String> friendInfoList = new ArrayList<>();
                    JSONArray friendList = json.getJSONArray("friendList");
                    for (Object o : friendList)
                        friendInfoList.add((String) o);
                    return friendInfoList;
                }
            } else {
                imRequest("GetFriendList", mServiceID, null, new OSNGeneralCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            List<String> friendInfoList = new ArrayList<>();
                            JSONArray friendList = data.getJSONArray("friendList");
                            for (Object o : friendList)
                                friendInfoList.add((String) o);
                            callback.onSuccess(friendInfoList);
                        } catch (Exception e) {
                            logError(e);
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onFailure(error);
                    }
                });
            }
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
        }
        return null;
    }
    public List<String> getGroupList(OSNGeneralCallbackT<List<String>> callback) {
        try {
            if (callback == null) {
                JSONObject json = imRequest("GetGroupList", mServiceID, null, null);
                if (json != null) {
                    List<String> groupInfoList = new ArrayList<>();
                    JSONArray groupList = json.getJSONArray("groupList");
                    for (Object o : groupList)
                        groupInfoList.add((String) o);
                    return groupInfoList;
                }
            } else {
                imRequest("GetGroupList", mServiceID, null, new OSNGeneralCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            List<String> groupInfoList = new ArrayList<>();
                            JSONArray groupList = data.getJSONArray("groupList");
                            for (Object o : groupList)
                                groupInfoList.add((String) o);
                            callback.onSuccess(groupInfoList);
                        } catch (Exception e) {
                            logError(e);
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onFailure(error);
                    }
                });
            }
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
        }
        return null;
    }
    public void inviteFriend(String userID, String reason, OSNGeneralCallback callback) {
        JSONObject data = new JSONObject();
        data.put("reason", reason);
        imRequestForResend("AddFriend", userID, data, callback);
    }

    public void recall(String from, String to, String msgHash, OSNGeneralCallback callback) {
        try {
            //System.out.println("@@@ from " + from);
            //System.out.println("@@@ to " + to);
            JSONObject data = new JSONObject();
            data.put("command", "Recall");
            data.put("from", from);
            data.put("to", to);
            data.put("messageHash", msgHash);

            String calc = "Recall" + from + to + msgHash;
            String hash = ECUtils.osnHash(calc.getBytes());
            String sign = ECUtils.osnSign(mOsnKey, hash.getBytes());
            data.put("sign", sign);

            imRequest("EncData", to, data, callback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //key = "AddFriend"   value = "0","1", "2"
    public void setRole(String key, String value, OSNGeneralCallback callback) {

        JSONObject roleJson = new JSONObject();
        roleJson.put(key, value);
        JSONObject data = new JSONObject();

        data.put("role", roleJson.toJSONString());

        imRequestForResend("UpRole", mServiceID, data, callback);
    }


    public void AllowGroupAddFriend(String value, String groupId, OSNGeneralCallback callback) {

        /**
         * 数据格式
         * key:AllowAddFriend
         * value:yes or no
         * **/
        //System.out.println("@@@ from " + from);
        //System.out.println("@@@ to " + to);
        JSONObject data = new JSONObject();
        data.put("key", "AllowAddFriend");
        data.put("value", value);
        //data.put("to", to);

        imRequestForResend("UpAttribute", groupId, data, callback);
    }

    public void UpGroupDapp(String value, String groupId, OSNGeneralCallback callback) {

        /**
         * 数据格式
         * key:Dapp
         * value:dappinfo
         * **/
        //System.out.println("@@@ from " + from);
        //System.out.println("@@@ to " + to);
        JSONObject data = new JSONObject();
        data.put("key", "Dapp");
        data.put("value", value);
        //data.put("to", to);

        imRequestForResend("UpAttribute", groupId, data, callback);
    }

    public void IsGroupForward(String value, String groupId, OSNGeneralCallback callback) {

        /**
         * 数据格式
         * key:AllowAddFriend
         * value:yes or no
         * **/
        //System.out.println("@@@ from " + from);
        //System.out.println("@@@ to " + to);
        JSONObject data = new JSONObject();
        data.put("key", "isGroupForward");
        data.put("value", value);
        //data.put("to", to);

        imRequestForResend("UpAttribute", groupId, data, callback);
    }

    public void IsGroupCopy(String value, String groupId, OSNGeneralCallback callback) {

        /**
         * 数据格式
         * key:AllowAddFriend
         * value:yes or no
         * **/
        //System.out.println("@@@ from " + from);
        //System.out.println("@@@ to " + to);
        JSONObject data = new JSONObject();
        data.put("key", "isGroupCopy");
        data.put("value", value);
        //data.put("to", to);

        imRequestForResend("UpAttribute", groupId, data, callback);
    }

    public void IsClearChats(String value, String groupId, OSNGeneralCallback callback) {

        /**
         * 数据格式
         * key:AllowAddFriend
         * value:yes or no
         * **/
        //System.out.println("@@@ from " + from);
        //System.out.println("@@@ to " + to);
        JSONObject data = new JSONObject();
        data.put("key", "clearTimes");
        data.put("value", value);
        //data.put("to", to);

        imRequestForResend("UpAttribute", groupId, data, callback);
    }

    public void TopMessage(String time, String value, String groupId, OSNGeneralCallback callback) {

        /**
         * 数据格式
         * key:AllowAddFriend
         * value:yes or no
         * **/
        //System.out.println("@@@ from " + from);
        //System.out.println("@@@ to " + to);
        JSONObject data = new JSONObject();
        data.put("key", time);
        data.put("value", value);
        //data.put("to", to);

        imRequestForResend("UpAttribute", groupId, data, callback);
    }

    public void DeleteTopMessage(List<String> time,String groupId, OSNGeneralCallback callback) {

        JSONObject data = new JSONObject();
        data.put("keys", time);

        imRequestForResend("RemoveAttribute", groupId, data, callback);
    }


    public void UpAttribute(String key, String value, String groupId, OSNGeneralCallback callback) {


        //System.out.println("@@@ from " + from);
        //System.out.println("@@@ to " + to);
        JSONObject data = new JSONObject();
        data.put("key", key);
        data.put("value", value);
        //data.put("to", to);

        imRequestForResend("UpAttribute", groupId, data, callback);
    }

    public void UpDescribes(String key, String value, String user, OSNGeneralCallback callback) {

        /**
         * 数据格式
         * key:AllowAddFriend
         * value:yes or no
         * **/
        //System.out.println("@@@ from " + from);
        //System.out.println("@@@ to " + to);
        JSONObject describesJson = new JSONObject();
        describesJson.put(key,value);
        JSONObject data = new JSONObject();
        data.put("data", describesJson.toJSONString());

        imRequestForResend("UpDescribes", mServiceID, data, callback);
    }

    public void UpDescribe(String key, String value, String groupId, OSNGeneralCallback callback) {

        /**
         * 数据格式
         * key:joinType
         * value:"password"  "verify"  "free"  "member"  "admin"  "licence"  "none"
         * **/

        JSONObject data = new JSONObject();
        data.put("key", key);
        data.put("value", value);

        imRequestForResend("UpDescribe", groupId, data, callback);
    }

    public void UpPrivateInfo(String key, String value, String groupId, OSNGeneralCallback callback) {

        /**
         * 数据格式
         * key:joinPwd
         * value:"password"
         * **/

        JSONObject data = new JSONObject();
        data.put("key", key);
        data.put("value", value);

        imRequestForResend("UpPrivateInfo", groupId, data, callback);
    }



    public void RemoveDescribes(String key, OSNGeneralCallback callback) {

        /**
         * 数据格式
         * key:AllowAddFriend
         * value:yes or no
         * **/
        JSONObject data = new JSONObject();
        {
            JSONObject cmd = new JSONObject();
            cmd.put("command", key);
            data.put("data", cmd);
        }


        imRequestForResend("RemoveDescribes", mServiceID, data, callback);
    }

    public void DeleteToMessage(String from, String to, String msgHash, OSNGeneralCallback callback) {

        try {
            JSONObject data = new JSONObject();
            String command = "DeleteMessage";
            data.put("command", command);
            data.put("from", from);
            data.put("to", to);
            data.put("messageHash", msgHash);

            String calc = command + from + to + msgHash;
            String hash = ECUtils.osnHash(calc.getBytes());
            String sign = ECUtils.osnSign(mOsnKey, hash.getBytes());
            data.put("sign", sign);

            imRequest("EncData", to, data, callback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }


    public void deleteFriend(String userID, OSNGeneralCallback callback) {
        JSONObject data = new JSONObject();
        data.put("friendID", userID);
        imRequestForResend("DelFriend", mServiceID, data, callback);
    }
    public void acceptFriend(String userID, OSNGeneralCallback callback) {
        imRequest("AgreeFriend", userID, null, callback);
    }
    public void rejectFriend(String userID, OSNGeneralCallback callback) {
        imRequest("RejectFriend", userID, null, callback);
    }
    public void acceptMember(String userID, String groupID, OSNGeneralCallback callback) {
        JSONObject data = new JSONObject();
        data.put("userID", userID);
        imRequest("AgreeMember", groupID, data, callback);
    }
    public void rejectMember(String userID, String groupID, OSNGeneralCallback callback) {
        JSONObject data = new JSONObject();
        data.put("userID", userID);
        imRequest("RejectMember", userID, data, callback);
    }
    public void getOwnerSign(String groupID, OSNGeneralCallback callback){
        JSONObject data = new JSONObject();
        data.put("groupID", groupID);
        data.put("owner", mOsnID);
        data.put("timestamp", System.currentTimeMillis());
        imRequest("GetOwnerSign", groupID, data, callback);
    }
    public void getGroupSign(String groupID, String info, OSNGeneralCallback callback){
        try {
            JSONObject data = JSON.parseObject(info);
            imRequest2("GetGroupSign", groupID, data, callback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public void setGroupOwner(String groupID, String owner, OSNGeneralCallback callback){
        JSONObject data = new JSONObject();
        data.put("osnID", owner);
        imRequest("NewOwner", groupID, data, callback);
    }
    public void addGroupManager(String groupID, List<String> memberIds, OSNGeneralCallback callback){
        JSONObject data = new JSONObject();
        data.put("adminList", memberIds);
        imRequestForResend("AddAdmin", groupID, data, callback);
    }
    public void delGroupManager(String groupID, List<String> memberIds, OSNGeneralCallback callback){
        JSONObject data = new JSONObject();
        data.put("adminList", memberIds);
        imRequestForResend("DelAdmin", groupID, data, callback);
    }
    public void saveGroup(String groupID, int status, OSNGeneralCallback callback){
        JSONObject data = new JSONObject();
        data.put("groupID", groupID);
        data.put("status", status);
        imRequestForResend("SaveGroup", mServiceID, data, callback);
    }
    public void orderPay(String info, OSNGeneralCallback callback){
        try {
            JSONObject data = JSON.parseObject(info);
            imRequest("OrderPay", mServiceID, data, callback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    public void getRedPacket(String info, String userID, OSNGeneralCallback callback){
        try {
            JSONObject data = JSON.parseObject(info);
            imRequest("GetRedPacket", userID, data, callback);
        } catch (Exception e) {

        }
    }

    /*public JSONObject sendMessage(String text, String userID, OSNGeneralCallback callback) {
        try {

            if(mHearted) {
                JSONObject data = new JSONObject();
                data.put("content", text);
                if (userID.startsWith("OSNG")) {
                    data.put("originalUser", mOsnID);

                    try {
                        long timestamp = System.currentTimeMillis();
                        data.put("timestamp", timestamp);
                        String calc = mOsnID + text + timestamp;
                        String hash = ECUtils.osnHash(calc.getBytes());
                        String sign = ECUtils.osnSign(mOsnKey, hash.getBytes());
                        data.put("sign", sign);
                    } catch (Exception e) {

                    }
                    System.out.println("@@@    message send : " + data);
                }

                System.out.println("[QuoteMessage] send message");
                JSONObject msg = imRequest("Message", userID, data, new OSNGeneralCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        if(callback != null) {
                            callback.onSuccess(data);
                        }
                    }
                    @Override
                    public void onFailure(String error) {
                        if(error.contains("10009")) {
                            callback.onFailure(error);
                            System.out.println("@@@    发送失败   10009");
                            return;
                        }
                        System.out.println("[QuoteMessage] resend message");
                        sendMessageAlone(text, userID, callback);
                    }
                });

                //
                return msg;


            }else{
                System.out.println("[QuoteMessage] resend");
                sendMessageAlone(text, userID, callback);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        return null;
    }*/

    public JSONObject sendMessage(String text, String userID, OSNGeneralCallback callback) {
        try {

            JSONObject data = new JSONObject();
            data.put("content", text);
            if (userID.startsWith("OSNG")) {
                data.put("originalUser", mOsnID);
                try {
                    long timestamp = System.currentTimeMillis();
                    data.put("timestamp", timestamp);
                    String calc = mOsnID + text + timestamp;
                    String hash = ECUtils.osnHash(calc.getBytes());
                    String sign = ECUtils.osnSign(mOsnKey, hash.getBytes());
                    data.put("sign", sign);
                } catch (Exception e) {

                }
            }

            // make message
            JSONObject msgData = OsnUtils.makeMessage("Message", mOsnID, userID, data, mOsnKey);
            if (msgData == null) {
                if (callback != null) {
                    callback.onFailure("make message failed.");
                }
                return null;
            }

            if (mHearted) {
                return sendMessageForResend(msgData, callback);
            } else {
                System.out.println("[sendMessage] resend");
                sendMessageAloneOnlySend(msgData, callback);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public JSONObject sendMessageAloneOnlySend(JSONObject json, OSNGeneralCallback callback) {
        try {
            if (callback == null) {
                return shortMessage(json);
            } else {
                shortMessage(json, callback);
            }
        } catch (Exception e) {
            System.out.println("[sendMessageAloneOnlySend] "+e.getMessage());
        }
        return null;
    }

    private JSONObject shortMessage(JSONObject json){
        try{
            System.out.println("[QuoteMessage] shortMessage json:" + json);
            String msgHash = json.getString("hash");
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(mHost, mPort), 5000);
                if (!socket.isConnected()) {
                    logInfo("connect failure");
                    return null;
                }
                logInfo(json.getString("command") + ": " + json.toString());
                byte[] jsonData = json.toString().getBytes();
                byte[] headData = new byte[4];
                headData[0] = (byte) ((jsonData.length >> 24) & 0xff);
                headData[1] = (byte) ((jsonData.length >> 16) & 0xff);
                headData[2] = (byte) ((jsonData.length >> 8) & 0xff);
                headData[3] = (byte) (jsonData.length & 0xff);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(headData);
                outputStream.write(jsonData);
                outputStream.flush();

                byte[] head = new byte[4];
                while (true) {
                    if (workRead(socket, head, 4) <= 0)
                        break;
                    int length = ((head[0] & 0xff) << 24) | ((head[1] & 0xff) << 16) | ((head[2] & 0xff) << 8) | (head[3] & 0xff);
                    byte[] socketData = new byte[length];
                    if (workRead(socket, socketData, length) <= 0)
                        break;
                    String msg = new String(socketData);
                    json = JSON.parseObject(msg);
                    logInfo("json: " + json.toString());
                    if (isSuccess(json)) {
                        json.put("msgHash", msgHash);
                    } else {
                        json = null;
                    }
                    socket.shutdownOutput();
                    socket.shutdownInput();
                    socket.close();
                    return json;
                }
            }catch (Exception e){
                logError(e);
                e.printStackTrace();
            }
            if(socket.isConnected()) {
                socket.shutdownOutput();
                socket.shutdownInput();
            }
            socket.close();
        }catch (Exception e){
            logError(e);
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject sendMessageForResend(JSONObject data, OSNGeneralCallback callback) {

        String command = data.getString("command");
        if (callback != null) {
            mExecutor.execute(() -> {
                JSONObject result = sendPackage(data);
                System.out.println("[sendMessageForResend] result : " + result);
                if(result != null){
                    System.out.println("[sendMessageForResend] imRespond 1");
                    imRespond(command, result, data, callback);
                }else{
                    System.out.println("[sendMessageForResend] resend.");
                    sendMessageAloneOnlySend(data, callback);
                }
            });
        } else {
            JSONObject result = sendPackage(data);
            System.out.println("[sendMessageForResend] result : " + result);
            if(result != null){
                return imRespond(command, result, data, null);
            } else {
                System.out.println("[sendMessageForResend] resend.");
                return sendMessageAloneOnlySend(data, null);
            }
        }

        return null;
    }

    public JSONObject sendMessageNoCallback(String text, String userID) {

        try {
            if (mHearted) {
                JSONObject data = new JSONObject();
                data.put("content", text);
                if (userID.startsWith("OSNG"))
                    data.put("originalUser", mOsnID);
                JSONObject msg = imRequest("Message", userID, data, new OSNGeneralCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {

                    }
                    @Override
                    public void onFailure(String error) {

                    }
                });

                //
                return msg;
            }else{
                sendMessageAlone(text, userID, null);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        return null;
    }

    public void recall(String to, String msgHash, OSNGeneralCallback callback) {
        recall(mOsnID, to, msgHash, callback);
    }


    public void allowGroupAddFriend(String value,String groupId, OSNGeneralCallback callback){
        AllowGroupAddFriend(value, groupId, callback);
    }

    public void isGroupForward(String value,String groupId, OSNGeneralCallback callback){
        IsGroupForward(value, groupId, callback);
    }

    public void isGroupCopy(String value,String groupId, OSNGeneralCallback callback){
        IsGroupCopy(value, groupId, callback);
    }

    public void isClearChats(String value,String groupId, OSNGeneralCallback callback){
        IsClearChats(value, groupId, callback);
    }

    public void topMessage(String time, String value,String groupId, OSNGeneralCallback callback){
        TopMessage(time,value, groupId, callback);
    }

    public void deleteTopMessage(List<String> time,String groupId, OSNGeneralCallback callback){
        DeleteTopMessage(time, groupId, callback);
    }

    public void upDescribes(String key,String value,String user,OSNGeneralCallback callback){
        UpDescribes(key,value,user,callback);
    }

    public void upDescribe(String key,String value,String group,OSNGeneralCallback callback){
        UpDescribe(key,value,group,callback);
    }

    public void upPrivateInfo(String key,String value,String group,OSNGeneralCallback callback){
        UpPrivateInfo(key,value,group,callback);
    }

    public void upAttribute(String key,String value,String groupId,OSNGeneralCallback callback){
        UpAttribute(key,value,groupId,callback);
    }

    public void removeDescribes(String key, OSNGeneralCallback callback){
        RemoveDescribes(key, callback);
    }


    public void deleteMessageTo(String to, String msgHash, OSNGeneralCallback callback) {
        DeleteToMessage(mOsnID, to, msgHash, callback);
    }
    public void sendMessageAlone(String text, String userID, OSNGeneralCallback callback) {
        try {
            JSONObject data = new JSONObject();
            data.put("content", text);
            if (userID.startsWith("OSNG"))
                data.put("originalUser", mOsnID);
            logInfo("send alone");
            JSONObject json = OsnUtils.makeMessage("Message", mOsnID, userID, data, mOsnKey);
            shortMessage(json, callback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void resendMessage(String command, String to, JSONObject data, OSNGeneralCallback callback) {
        try {
            JSONObject json = OsnUtils.makeMessage(command, mOsnID, to, data, mOsnKey);
            shortMessage(json, callback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void resendMessage2(String command, String to, JSONObject data, OSNGeneralCallback callback) {
        try {
            JSONObject json = OsnUtils.makeMessage(command, mOsnID, to, data, mOsnKey);
            shortMessage2(json, callback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendDynamic (String text, OSNGeneralCallback callback){
        try {
            JSONObject data = JSON.parseObject(text);
            imRequest("Dynamic", mServiceID, data, callback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    public void sendBroadcast (String content, OSNGeneralCallback callback){

        try {
            JSONObject data = JSON.parseObject(content);
            imRequest("Broadcast", null, data, callback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    public void deleteMessage(String hash, String osnID, OSNGeneralCallback callback){

        try {
            JSONObject data = new JSONObject();
            String target = osnID == null || osnID.startsWith("OSNU") ? mServiceID : osnID;
            data.put("type", "delete");
            data.put("osnID", target);
            data.put("messageHash", hash);
            imRequest("SetMessage", target, data, callback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }
    public List<OsnMessageInfo> loadMessage(String userID, long timestamp, int count, boolean before, OSNGeneralCallbackT<List<OsnMessageInfo>> callback) {
        try {
            JSONObject data = new JSONObject();
            data.put("userID", userID);
            data.put("timestamp", timestamp);
            data.put("count", count);
            data.put("before", before);
            if (callback == null) {
                JSONObject json = imRequest("MessageLoad", mServiceID, data, null);
                if (json == null)
                    return null;
                return getMessages(json);
            }
            imRequest("MessageLoad", mServiceID, data, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    List<OsnMessageInfo> messages = getMessages(data);
                    callback.onSuccess(messages);
                }

                @Override
                public void onFailure(String error) {
                    callback.onFailure(error);
                }
            });
        } catch (Exception e) {
            logError(e);
            e.printStackTrace();
        }
        return null;
    }
    public void createGroup(String groupName, List<String> member, int type, String portrait, OSNGeneralCallback callback) {
        JSONArray array = new JSONArray();
        array.addAll(member);
        JSONObject data = new JSONObject();
        data.put("name", groupName);
        data.put("type", type);
        data.put("portrait", portrait);
        data.put("userList", array);
        imRequest("CreateGroup", mServiceID, data, callback);
    }
    public void createGroup2(String groupName, List<String> member, int type, String portrait, String owner2, OSNGeneralCallback callback) {
        JSONArray array = new JSONArray();
        array.addAll(member);
        JSONObject data = new JSONObject();
        data.put("command", "CreateGroup");
        data.put("owner", mOsnID);
        data.put("name", groupName);
        data.put("type", type);
        data.put("portrait", portrait);
        data.put("userList", array);
        imRequestForResend2("EncData", mServiceID, data, callback);
    }

    public void joinGroup(String groupID, String reason, String invitation, OSNGeneralCallback callback) {
        JSONObject data = new JSONObject();
        data.put("reason", reason);
        data.put("invitation", invitation);
        imRequest(invitation == null ? "JoinGroup" : "JoinGrp", groupID, data, callback);
    }
    public void rejectGroup(String groupID, OSNGeneralCallback callback) {
        imRequest("RejectGroup", groupID, null, callback);
    }
    public void addMember(String groupID, List<String> members, OSNGeneralCallback callback) {
        JSONArray array = new JSONArray();
        array.addAll(members);
        JSONObject data = new JSONObject();
        data.put("state", "AddMember");
        data.put("memberList", array);
        imRequestForResend("AddMember", groupID, data, callback);
    }
    public void delMember(String groupID, List<String> members, OSNGeneralCallback callback) {
        JSONArray array = new JSONArray();
        array.addAll(members);
        JSONObject data = new JSONObject();
        data.put("state", "DelMember");
        data.put("memberList", array);
        imRequestForResend("DelMember", groupID, data, callback);
    }
    public void quitGroup(String groupID, OSNGeneralCallback callback) {
        JSONObject data = new JSONObject();
        data.put("state", "QuitGroup");
        imRequestForResend("QuitGroup", groupID, data, callback);
    }
    public void dismissGroup(String groupID, OSNGeneralCallback callback) {
        JSONObject data = new JSONObject();
        data.put("state", "DelGroup");
        imRequestForResend("DelGroup", groupID, data, callback);
    }
    public void muteGroup(String groupID, boolean state, int mode, List<String> members, OSNGeneralCallback callback){
        JSONObject data = new JSONObject();
        data.put("mute", state);
        data.put("mode", mode);
        if (members != null){
            data.put("members", members);
        }

        imRequestForResend("Mute", groupID, data, callback);
    }
    public void billboard(String groupID, String text, OSNGeneralCallback callback){
        JSONObject data = new JSONObject();
        data.put("text", text);
        imRequestForResend("Billboard", groupID, data, callback);
    }
    public void setTempAccount(String tempAcc, OSNGeneralCallback callback){
        JSONObject data = new JSONObject();
        data.put("tempAcc", tempAcc);
        imRequest("SetTemp", mServiceID, data, callback);
    }
    public void findObject(String text, OSNGeneralCallback callback){

        String tempAcc = mTempID;//mOsnListener.getConfig("tempID");
        JSONObject data = new JSONObject();
        data.put("command", "findObj");
        data.put("from", tempAcc);
        data.put("text", text);
        logInfo("[findObject] data : " + data);
        imRequest("FindObj", mServiceID, data, callback);
    }

    public void modifyGroupInfo(List<String> keys, OsnGroupInfo groupInfo, OSNGeneralCallback callback) {
        try {
            JSONObject data = new JSONObject();
            for (String k : keys) {
                if (k.equalsIgnoreCase("name"))
                    data.put("name", groupInfo.name);
                else if (k.equalsIgnoreCase("portrait"))
                    data.put("portrait", groupInfo.portrait);
                else if (k.equalsIgnoreCase("type"))
                    data.put("type", groupInfo.type);
                else if (k.equalsIgnoreCase("joinType"))
                    data.put("joinType", groupInfo.joinType);
                else if (k.equalsIgnoreCase("passType"))
                    data.put("passType", groupInfo.passType);
                else if (k.equalsIgnoreCase("mute"))
                    data.put("mute", groupInfo.mute);
                else if (k.equalsIgnoreCase("attribute"))
                    data.put("attribute", groupInfo.attribute);
                else if (k.equalsIgnoreCase("billboard"))
                    data.put("billboard", groupInfo.billboard);
            }
            logInfo("info: "+data.toString());
            imRequestForResend("SetGroupInfo", groupInfo.groupID, data, callback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    public void modifyMemberInfo(List<String> keys, OsnMemberInfo memberInfo, OSNGeneralCallback callback) {
        try {
            JSONObject data = new JSONObject();
            for (String k : keys) {
                if (k.equalsIgnoreCase("nickName"))
                    data.put("nickName", memberInfo.nickName);
                else if(k.equalsIgnoreCase("status"))
                    data.put("status", memberInfo.status);
                else if(k.equalsIgnoreCase("type"))
                    data.put("type", memberInfo.type);
            }
            imRequestForResend("SetMemberInfo", memberInfo.groupID, data, callback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    public void uploadData(String fileName, String type, byte[] data, OSNTransferCallback callback) {
        if (callback == null) {
            HttpUtils.upload("http://" + mHost + ":8800/", type, fileName, data, null);
            return;
        }
        new Thread(() -> {
            HttpUtils.upload("http://" + mHost + ":8800/", type, fileName, data, callback);
        }).start();
    }
    public void uploadData(String fileName, String type, InputStream inputStream, OSNTransferCallback callback){
        if (callback == null) {
            HttpUtils.upload("http://" + mHost + ":8800/", type, fileName, inputStream, null);
            return;
        }
        new Thread(() -> {
            HttpUtils.upload("http://" + mHost + ":8800/", type, fileName, inputStream, callback);
        }).start();
    }
    public void downloadData(String remoteUrl, String localPath, OSNTransferCallback callback) {
        if (callback == null) {
            HttpUtils.download(remoteUrl, localPath, null);
            return;
        }
        new Thread(() -> {
            HttpUtils.download(remoteUrl, localPath, callback);
        }).start();
    }

    public void downloadData(String remoteUrl, String localPath, String password) {

        new Thread(() -> {
            HttpUtils.download(remoteUrl, localPath, null);
            // 解压缩

        }).start();
    }

    public void lpLogin(OsnLitappInfo litappInfo, String url, OSNGeneralCallback callback) {
        try {
            System.out.println("@@@ 1235");
            simpleLogin(litappInfo.target, url, callback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    public void simpleLogin(String target, String url, OSNGeneralCallback callback) {

        new Thread(() -> {
            String error = null;
            try {
                System.out.println("@@@ 1236");
                long randClient = System.currentTimeMillis();
                JSONObject json = new JSONObject();
                json.put("command", "GetServerInfo");
                json.put("user", mOsnID);
                json.put("random", randClient);
                logInfo("GetServerInfo data: "+json.toString());
                System.out.println("@@@登录的参数是："+json);
                System.out.println("@@@登录的url是："+url);
                String data = HttpUtils.doPost(url, json.toString());
                logInfo("GetServerInfo result: "+data);
                System.out.println("@@@ 结果是："+data);

                json = JSON.parseObject(data);
                String serviceID = json.getString("serviceID");
                String randServer = json.getString("random");
                String serverInfo = json.getString("serviceInfo");
                String session = json.getString("session");

                if (!serviceID.equalsIgnoreCase(target)) {
                    error = "serviceID no equals litappID: " + target + ", serviceID: " + serviceID;
                } else {
                    data = mOsnID + randClient + serviceID + randServer + serverInfo;
                    String hash = ECUtils.osnHash(data.getBytes());
                    String sign = json.getString("sign");
                    logInfo("sign data: " + data);
                    //logInfo("hash: " + hash);
                    //logInfo("sign: " + sign);
                    if (hash.equalsIgnoreCase(json.getString("hash")) &&
                            ECUtils.osnVerify(serviceID, hash.getBytes(), sign)) {
                        hash = ECUtils.osnHash((serviceID + randServer + mOsnID + randClient).getBytes());
                        sign = ECUtils.osnSign(mOsnKey, hash.getBytes());
                        json.clear();
                        json.put("command", "Login");
                        json.put("user", mOsnID);
                        json.put("hash", hash);
                        json.put("sign", sign);
                        json.put("session", session);
                        String jsonString = json.toString();
                        logInfo("Login data: "+json.toString());
                        System.out.println("@@@ 登录传递的参数是："+json);
                        System.out.println("@@@ 登录url是："+url);
                        data = HttpUtils.doPost(url, json.toString());
                        logInfo("Login result: "+data);
                        System.out.println("@@@ 登录结果是："+data);

                        json = JSON.parseObject(data);

                        if (!isSuccess(json))
                            error = errCode(json);
                        else {
//                            String aa = "{\"token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlZGIyZjY4YjI1OGQ0NjUyOWUwZWQ5Yzg4NjZjMWZjZSI6IntcImNyZWF0ZWREYXRlXCI6MTY4MTI4NjgzNzEyNyxcImludGVyZmFjZU51bVwiOjEwLFwibG9naW5EYXRlXCI6MTY4MTM1MTY5MzYyMixcInBob25lTm9cIjpcIk9TTlU2bmZ0SGlhWWppYXhxQ2FTd3RLS0t0SjNKQlV3bXduZmFOOTU1S0J6RkJvVkoyTFwiLFwidGltZVN0YW1wXCI6MTY4MTM1MTY5MzYyMixcInR5cGVcIjoxLFwidXNlcklkXCI6MjIyMjI5Nn0ifQ.5GuviRxbaxdLc3EOXZEb2BLwGTyCT6ExqbEcLrhRcrU\"}";
//                            JSONObject jsonObject = JSONObject.parseObject(aa);
//                            ECUtils.ecEncrypt2("OSNU6nftHiaYjiaxqCaSwtKKKtJ3JBUwmwnfaN955KBzFBoVJ2L", jsonObject.toString().getBytes());
//                            String data = new String(ECUtils.ecDecrypt2("VK0MHcCAQEEIPufVQqSPXvP6v0g6zNPYPjhTcY60xyCGUBIZTjqWGUtoAoGCCqGSM49AwEHoUQDQgAEG0sbtxtNLtLibsgAhEb5lsoQRf0yX4+rkW+POeF6aTGEIZlIQGFWuh35lrFVL8EyX4zK3BB4T/bUawDqnZXLmA==", json.getString("sessionKey")));
//                            //logInfo("mOsnKey:"+mOsnKey);
                            data = new String(ECUtils.ecDecrypt2(mOsnKey, json.getString("sessionKey")));
                            logInfo("decrypt data: "+data);
                            int index = data.indexOf("\0");
                            logInfo("index==========="+index);
                            if (index != -1){
                                logInfo("======================");
                                data = data.substring(0, index);
                                logInfo("decrypt data111111111111111: "+data);
                            }
                            json = JSON.parseObject(data);
                            System.out.println("@@@ 返回给前端的数据是"+ json);
                            callback.onSuccess(json);
                        }
                    } else
                        error = "verify error";
                }
            } catch (Exception e) {
                logError(e);
                e.printStackTrace();
                error = e.toString();
            }
            if (error != null)
                callback.onFailure(error);
        }).start();
    }

    public String hashData(byte[] data){
        String hash = ECUtils.osnHash(data);
        logInfo("hash: "+hash);
        return hash;
    }
    public String signData(byte[] data) {
        return ECUtils.osnSign(mOsnKey, data);
    }
    public boolean verifyData(String osnID, byte[] data, String sign){
        return ECUtils.osnVerify(osnID, data, sign);
    }
    public String encryptData(String osnID, byte[] data){
        return ECUtils.ecEncrypt2(osnID, data);
    }
}
