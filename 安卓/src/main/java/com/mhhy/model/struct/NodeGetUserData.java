package com.mhhy.model.struct;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class NodeGetUserData {
    private String user;

    public NodeGetUserData(JSONObject json){
        user = json.getString("user");
    }
}
