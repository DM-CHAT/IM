package com.mhhy.model.struct;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class UserCreateOrderData {
    int lifecycle;
    String pay_id;

    public UserCreateOrderData(JSONObject json){
        lifecycle = json.getIntValue("lifecycle");
        pay_id = json.getString("payId");
    }
}
