package com.mhhy.model.struct;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class UserQueryOrderData {
    String orderNo;
    String user;

    public UserQueryOrderData(JSONObject json) {

        orderNo = json.getString("orderNo");
        user = json.getString("user");
    }
}
