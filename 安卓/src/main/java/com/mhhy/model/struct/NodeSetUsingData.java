package com.mhhy.model.struct;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;


@Data
public class NodeSetUsingData {
    private String user;
    private String node;
    private int state = 0;

    public NodeSetUsingData(JSONObject json){
        user = json.getString("user");
        node = json.getString("node");
        state = json.getIntValue("state");
    }
}
