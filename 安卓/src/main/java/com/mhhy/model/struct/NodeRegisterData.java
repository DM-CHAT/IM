package com.mhhy.model.struct;

import com.alibaba.fastjson.JSONObject;

public class NodeRegisterData {
    public String IP;
    public String region;

    public NodeRegisterData(JSONObject json){
        IP = json.getString("IP");
        region = json.getString("region");
    }
}
