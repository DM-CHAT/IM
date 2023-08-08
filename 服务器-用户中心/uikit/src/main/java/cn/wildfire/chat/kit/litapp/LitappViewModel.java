package cn.wildfire.chat.kit.litapp;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.WalletsInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.OnLitappInfoResultListener;
import cn.wildfirechat.remote.OnLitappInfoUpdateListener;

public class LitappViewModel extends ViewModel implements OnLitappInfoUpdateListener, OnLitappInfoResultListener {
    String TAG = LitappViewModel.class.getSimpleName();
    private MutableLiveData<List<LitappInfo>> litappInfoUpdateLiveData;
    private MutableLiveData<List<LitappInfo>> litappInfoResultLiveData;

    public LitappViewModel() {
        super();
        ChatManager.Instance().addLitappInfoUpdateListener(this);
        ChatManager.Instance().addLitappInfoResultListener(this);
    }
    @Override
    protected void onCleared() {
        ChatManager.Instance().removeLitappInfoUpdateListener(this);
    }

    public MutableLiveData<List<LitappInfo>> litappInfoLiveData() {
        if (litappInfoUpdateLiveData == null) {
            litappInfoUpdateLiveData = new MutableLiveData<>();
        }
        return litappInfoUpdateLiveData;
    }
    public MutableLiveData<List<LitappInfo>> litappResultLiveData() {
        if (litappInfoResultLiveData == null) {
            litappInfoResultLiveData = new MutableLiveData<>();
        }
        return litappInfoResultLiveData;
    }

    public LitappInfo getLitappInfo(String litappId, boolean refresh) {
        return ChatManager.Instance().getLitappInfo(litappId, refresh);
    }
    public List<LitappInfo> getLitappList(){
        return ChatManager.Instance().getLitappList();
    }

    public List<WalletsInfo> getWalletsInfo() {
        return ChatManager.Instance().getWalletsInfo();
    }

    @Override
    public void onLitappInfoUpdate(List<LitappInfo> litappInfos) {
        if (litappInfoUpdateLiveData != null) {
            litappInfoUpdateLiveData.setValue(litappInfos);
        }
    }
    @Override
    public void onLitappInfoResult(List<LitappInfo> litappInfos){
        Log.d(TAG, JSONObject.toJSONString(litappInfos));
        if (litappInfoResultLiveData != null) {
            litappInfoResultLiveData.setValue(litappInfos);
        }
    }
}
