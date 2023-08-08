package cn.wildfirechat.model;

import android.os.Parcel;
import android.os.Parcelable;

public class OrgTag implements Parcelable {
    public int id;
    public String tagName;
    public int tagId;


    public OrgTag(){
    }

    public OrgTag(String tagName, int tagId){
        this.tagName =tagName;
        this.tagId = tagId;
    }

    protected OrgTag(Parcel in) {
        id = in.readInt();
        tagName = in.readString();
        tagId = in.readInt();
    }

    public static final Creator<OrgTag> CREATOR = new Creator<OrgTag>() {
        @Override
        public OrgTag createFromParcel(Parcel in) {
            return new OrgTag(in);
        }

        @Override
        public OrgTag[] newArray(int size) {
            return new OrgTag[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeInt(this.id);
        dest.writeString(this.tagName);
        dest.writeInt(this.tagId);
    }
}
