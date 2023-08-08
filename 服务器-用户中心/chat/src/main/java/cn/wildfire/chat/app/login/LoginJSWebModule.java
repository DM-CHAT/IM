package cn.wildfire.chat.app.login;

import android.app.Activity;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.wildfirechat.model.ModifyMyInfoEntry;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;

import static cn.wildfirechat.model.ModifyMyInfoType.Modify_DisplayName;
import static cn.wildfirechat.model.ModifyMyInfoType.Modify_Portrait;

public class LoginJSWebModule {
    Activity mActivity;
    LoginCallback mCallback;
    final Object mLock = new Object();
    String eMissData = "-1:miss data";
    String eLoginFail = "-2:login failed";
    String eTimeout = "-3:time out";
    String eUnknownCmd = "-100:unknown cmd";
    String eResultError = "-200:operation error";
    static String TAG = "WebCallModelVue";

    static class RunBoot implements Runnable {
        JSONObject json;
        JSONObject result;
        LoginJSWebModule vue;
        RunBoot(LoginJSWebModule vue, JSONObject json){
            this.vue = vue;
            this.json = json;
        }
        @Override
        public void run() {
            result = vue.process(json);
        }
    }
    public interface LoginCallback{
        void onLogin(JSONObject data);
        void onMnemonicMode(JSONObject data);
    }
    public LoginJSWebModule(Activity activity){
        mActivity = activity;
    }
    public void setCallback(LoginCallback callback){
        mCallback = callback;
    }
    JSONObject setError(String error, JSONObject json){
        if(json == null)
            json = new JSONObject();
        json.put("errCode", error == null ? "0:success" : error);
        return json;
    }

    public JSONObject doLogin(JSONObject json){
        mCallback.onLogin(json);
        return setError(null, null);
    }

    public JSONObject doMnemonicMode(JSONObject json){
        mCallback.onMnemonicMode(json);
        return setError(null, null);
    }

    public JSONObject doLogout(JSONObject json){
        ChatManager.Instance().disconnect(true, true);
        return setError(null, null);
    }
    public JSONObject getUserInfo(JSONObject json){
        UserInfo userInfo = ChatManager.Instance().getUserInfo(null, false);
        json.clear();
        json.put("userInfo", userInfo);
        return setError(null, json);
    }
    public JSONObject setUserInfo(JSONObject json){
        List<ModifyMyInfoEntry> keys = new ArrayList<>();
        if(json.containsKey("displayName"))
            keys.add(new ModifyMyInfoEntry(Modify_DisplayName, json.getString("displayName")));
        else if(json.containsKey("portrait"))
            keys.add(new ModifyMyInfoEntry(Modify_Portrait, json.getString("portrait")));
        final boolean[] result = {false};
        final Object lock = new Object();
        ChatManager.Instance().modifyMyInfo(keys, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result[0] = true;
                synchronized (lock){
                    lock.notify();
                }
            }

            @Override
            public void onFail(int errorCode) {
                synchronized (lock){
                    lock.notify();
                }
            }
        });
        try {
            synchronized (lock) {
                lock.wait();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return setError(result[0] ? null : eResultError, json);
    }
    public JSONObject process(JSONObject json){
        String command = json.getString("command");
        switch(command){
            case "Login":
                System.out.println("@@@ login info : " + json);
                json = doLogin(json);
                break;
            case "Logout":
                json = doLogout(json);
                break;
            case "GetUserInfo":
                json = getUserInfo(json);
                break;
            case "SetUserInfo":
                json = setUserInfo(json);
                break;
            case "MnemonicMode":
                json = doMnemonicMode(json);
                break;
            default:
                json = setError(eUnknownCmd, null);
                break;
        }
        synchronized (mLock){
            mLock.notify();
        }
        return json;
    }

    @android.webkit.JavascriptInterface
    public String run(String args) {
        String result = null;
        Log.d(TAG,"args: "+args);
        try {
            JSONObject json = JSON.parseObject(args);
            if (json == null)
                return setError(eMissData, null).toString();
            RunBoot runBoot = new RunBoot(this, json);
            mActivity.runOnUiThread(runBoot);
            //new Thread(runBoot).start();
            synchronized (mLock) {
                mLock.wait(50000);
            }
            if(runBoot.result != null)
                result = runBoot.result.toString();
        } catch (Exception e){
            e.printStackTrace();
            result = setError("-1:exception", null).toString();
        }
        if(result == null)
            result = setError(eTimeout, null).toString();
        Log.d(TAG,"result: "+result);
        return result;
    }
}

