package cn.wildfirechat.model;

import android.os.Parcel;
import android.os.Parcelable;

public class WalletsInfo implements Parcelable {

    public String name;
    public String OsnID;
    public String wallets;

    public WalletsInfo(){}
    public WalletsInfo(String OsnID){
        this.OsnID = OsnID;
    }

    protected WalletsInfo(Parcel in) {
        this.OsnID = in.readString();
        this.wallets = in.readString();
        this.name = in.readString();
    }

    public static final Creator<WalletsInfo> CREATOR = new Creator<WalletsInfo>() {
        @Override
        public WalletsInfo createFromParcel(Parcel in) {
            return new WalletsInfo(in);
        }

        @Override
        public WalletsInfo[] newArray(int size) {
            return new WalletsInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.name);
        parcel.writeString(this.wallets);
        parcel.writeString(this.OsnID);
    }
}
