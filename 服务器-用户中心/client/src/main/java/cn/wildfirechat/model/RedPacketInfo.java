
package cn.wildfirechat.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.JSONObject;

public class RedPacketInfo implements Parcelable {
    public String type;
    public String user;
    public String count;
    public String price;
    public String target;
    public String text;
    public String packetID;
    public String unpackID;
    public String urlQuery;
    public String urlFetch;
    public String luckNum;
    public String dapp;
    public String coinType;
    public String wallet;
    public long timestamp;
    public int state;

    public RedPacketInfo(){}
    public RedPacketInfo(JSONObject json) {
        this.type = json.getString("type");
        switch(this.type){
            case "transaction":
                this.type = "normal";
                break;
            case "random":
                this.type = "loot";
                break;
            case "bomb":
                this.type = "bomb";
                break;
        }
        this.user = json.getString("from");
        this.count = json.getString("count");
        this.price = json.getString("balance");
        this.target = json.getString("to");
        this.packetID = json.getString("txid");
        this.text = json.getString("greetings");
        this.unpackID = json.getString("txid");
        this.timestamp = json.getLongValue("timestamp");
        this.state = json.getIntValue("state");
        this.urlFetch = json.getString("urlFetch");
        this.urlQuery = json.getString("urlQuery");
        this.luckNum = json.getString("luckNum");
        this.dapp = json.containsKey("dapp") ? json.getString("dapp") : "";
        this.wallet = json.containsKey("wallet") ? json.getString("wallet") : "";
        this.coinType = json.containsKey("coinType") ? json.getString("coinType") : "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeString(this.user);
        dest.writeString(this.count);
        dest.writeString(this.price);
        dest.writeString(this.target);
        dest.writeString(this.packetID);
        dest.writeString(this.text);
        dest.writeString(this.unpackID);
        dest.writeLong(this.timestamp);
        dest.writeInt(this.state);
        dest.writeString(this.urlFetch);
        dest.writeString(this.urlQuery);
        dest.writeString(this.luckNum);
        dest.writeString(this.dapp);
        dest.writeString(this.wallet);
        dest.writeString(this.coinType);
    }

    protected RedPacketInfo(Parcel in) {
        this.type = in.readString();
        this.user = in.readString();
        this.count = in.readString();
        this.price = in.readString();
        this.target = in.readString();
        this.packetID = in.readString();
        this.text = in.readString();
        this.unpackID = in.readString();
        this.timestamp = in.readLong();
        this.state = in.readInt();
        this.urlFetch = in.readString();
        this.urlQuery = in.readString();
        this.luckNum = in.readString();
        this.dapp = in.readString();
        this.wallet = in.readString();
        this.coinType = in.readString();
    }

    public static final Creator<RedPacketInfo> CREATOR = new Creator<RedPacketInfo>() {
        @Override
        public RedPacketInfo createFromParcel(Parcel source) {
            return new RedPacketInfo(source);
        }

        @Override
        public RedPacketInfo[] newArray(int size) {
            return new RedPacketInfo[size];
        }
    };
}
