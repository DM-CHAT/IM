package cn.wildfirechat.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.JSONObject;

public class LitappInfo implements Parcelable{
    public String target;
    public String name;
    public String displayName;
    public String portrait;
    public String theme;
    public String themeUrl;
    public String url;
    public String info;
    public String param = "";
    public String urlParam = null;
    //public JSONObject urlParamJson;

    public LitappInfo() {
    }
    public LitappInfo(String target){
        this.target = target;
    }
    public LitappInfo(JSONObject json) {
        target = json.getString("target");
        name = json.getString("name");
        displayName = json.getString("displayName");
        theme = json.getString("theme");
        themeUrl = json.getString("themeUrl");
        url = json.getString("url");
        info = json.getString("info");
        param = json.getString("param");
        urlParam = json.getString("urlParam");
        portrait = json.getString("portrait");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.target);
        dest.writeString(this.name);
        dest.writeString(this.displayName);
        dest.writeString(this.portrait);
        dest.writeString(this.theme);
        dest.writeString(this.url);
        dest.writeString(this.info);
        dest.writeString(this.param);
        dest.writeString(this.urlParam);
    }

    protected LitappInfo(Parcel in) {
        this.target = in.readString();
        this.name = in.readString();
        this.displayName = in.readString();
        this.portrait = in.readString();
        this.theme = in.readString();
        this.url = in.readString();
        this.info = in.readString();
        this.param = in.readString();
        this.urlParam = in.readString();
    }

    public static final Creator<LitappInfo> CREATOR = new Creator<LitappInfo>() {
        @Override
        public LitappInfo createFromParcel(Parcel source) {
            return new LitappInfo(source);
        }

        @Override
        public LitappInfo[] newArray(int size) {
            return new LitappInfo[size];
        }
    };

    public JSONObject getDappInfo(){

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("target",target);
        jsonObject.put("name",name);
        jsonObject.put("displayName",displayName);
        jsonObject.put("portrait",portrait);
        jsonObject.put("theme",theme);
        jsonObject.put("url",url);
        jsonObject.put("info",info);
        jsonObject.put("param",param);
        jsonObject.put("themeUrl", themeUrl);

        return jsonObject;
    }
}
