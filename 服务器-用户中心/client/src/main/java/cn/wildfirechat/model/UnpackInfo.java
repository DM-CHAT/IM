
package cn.wildfirechat.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.JSONObject;

public class UnpackInfo implements Parcelable {
    public String user;
    public String fetcher;
    public String price;
    public String packetID;
    public String unpackID;
    public long timestamp;
    public String coin;

    public UnpackInfo(){}
    public UnpackInfo(JSONObject json) {
        this.user = json.getString("user");
        this.fetcher = json.getString("fetcher");
        this.price = json.getString("price");
        this.packetID = json.getString("packetID");
        this.unpackID = json.getString("unpackID");
        this.timestamp = json.getLongValue("timestamp");
        this.coin = json.getString("coin");
    }
    public UnpackInfo(RedPacketInfo info){
        this.user = info.user;
        this.fetcher = info.target;
        this.price = info.price;
        this.packetID = info.packetID;
        this.unpackID = info.unpackID;
        this.timestamp = info.timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.user);
        dest.writeString(this.fetcher);
        dest.writeString(this.price);
        dest.writeString(this.packetID);
        dest.writeString(this.unpackID);
        dest.writeLong(this.timestamp);
        dest.writeString(this.coin);
    }

    protected UnpackInfo(Parcel in) {
        this.user = in.readString();
        this.fetcher = in.readString();
        this.price = in.readString();
        this.packetID = in.readString();
        this.unpackID = in.readString();
        this.timestamp = in.readLong();
        this.coin = in.readString();
    }

    public static final Creator<UnpackInfo> CREATOR = new Creator<UnpackInfo>() {
        @Override
        public UnpackInfo createFromParcel(Parcel source) {
            return new UnpackInfo(source);
        }

        @Override
        public UnpackInfo[] newArray(int size) {
            return new UnpackInfo[size];
        }
    };
}
