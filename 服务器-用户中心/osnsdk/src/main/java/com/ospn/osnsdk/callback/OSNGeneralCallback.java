package com.ospn.osnsdk.callback;

import com.alibaba.fastjson.JSONObject;

public interface OSNGeneralCallback {
    void onSuccess(JSONObject data);
    void onFailure(String error);
}
