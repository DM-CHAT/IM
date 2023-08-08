package cn.wildfire.chat.kit.litapp;

import com.alibaba.fastjson.JSONObject;

public class FileUploadSetup {
    /**
     * aliyun, http, oss, ipfst
     * **/
    public String type;// = json.getString("type");


    /**
     * aliyun oss 使用参数：
     * AccessKeyId
     * AccessKeySecret
     * ENDPOINT
     * BUCKETNAME
     * **/
    public String AccessKeyId;// = json.getString("AccessKeyId");
    public String AccessKeySecret;// = json.getString("AccessKeySecret");
    public String ENDPOINT;// = json.getString("ENDPOINT");
    public String BUCKETNAME;// = json.getString("BUCKETNAME");

    public String childDir;

    public FileUploadSetup() {

    }

    public FileUploadSetup(JSONObject json) {
        type = null;
        type = json.getString("type");
        AccessKeyId = json.getString("AccessKeyId");
        AccessKeySecret = json.getString("AccessKeySecret");
        ENDPOINT = json.getString("ENDPOINT");
        BUCKETNAME = json.getString("BUCKETNAME");
        childDir = json.getString("childDir");
    }
}
