package com.mhhy.model.struct;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class UserQueryProductData {
    String user;

    public UserQueryProductData(JSONObject json) {
        user = json.getString("user");
    }

}
