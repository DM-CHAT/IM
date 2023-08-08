package cn.wildfirechat.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CollectInfo implements Parcelable {

    public String name;
    public String OsnID;
    public int type;
    public String collect;

    public CollectInfo(){}
    public CollectInfo(String OsnID){
        this.OsnID = OsnID;
    }

    protected CollectInfo(Parcel in) {
        this.OsnID = in.readString();
        this.collect = in.readString();
        this.name = in.readString();
        this.type = in.readInt();
    }

    public static final Creator<CollectInfo> CREATOR = new Creator<CollectInfo>() {
        @Override
        public CollectInfo createFromParcel(Parcel in) {
            return new CollectInfo(in);
        }

        @Override
        public CollectInfo[] newArray(int size) {
            return new CollectInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.name);
        parcel.writeString(this.collect);
        parcel.writeString(this.OsnID);
        parcel.writeInt(this.type);
    }
}
