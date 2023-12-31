/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.model;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

/**
 * @author heavyrain lee
 * @date 2017/12/6
 */

public class Conversation implements Parcelable {

    public enum ConversationType {
        // 单聊
        Single(0),
        // 群聊
        Group(1),
        // 聊天室
        ChatRoom(2),
        //频道
        Channel(3),
        //通知
        Notify(4),
        //服务
        Service(5),
        Call(6);


        private int value;

        ConversationType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static ConversationType type(int type) {
            ConversationType conversationType = null;
            switch (type) {
                case 0:
                    conversationType = Single;
                    break;
                case 1:
                    conversationType = Group;
                    break;
                case 2:
                    conversationType = ChatRoom;
                    break;
                case 3:
                    conversationType = Channel;
                    break;
                case 4:
                    conversationType = Notify;
                    break;
                case 5:
                    conversationType = Service;
                    break;
                case 6:
                    conversationType = Call;
                default:
                    throw new IllegalArgumentException("type " + type + " is invalid");
            }
            return conversationType;
        }
    }

    public ConversationType type;
    public String target;
    // 可以用来做自定义会话，区分不同业务线
    public int line;
    public boolean enable3 = false;
    public boolean enable2 = false;

    public boolean enableVoip = false;


    public boolean redPacketBomb = false;

    public String decKey;



    public Conversation(ConversationType type, String target, int line) {
        this.type = type;
        this.target = target;
        this.line = line;
    }

    public Conversation(ConversationType type, String target) {
        this.type = type;
        this.target = target;
        this.line = 0;
    }

    public Conversation(int type, String target) {
        if (type == 1)
            this.type = ConversationType.Group;
        this.target = target;
        this.line = 0;
    }



    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Conversation) {
            Conversation c = (Conversation) obj;
            return type == c.type && target.equals(c.target) && line == c.line;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return hashCode(type, target, line);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeString(this.target);
        dest.writeInt(this.line);
        dest.writeString(this.decKey);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected Conversation(Parcel in) {
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : ConversationType.values()[tmpType];
        this.target = in.readString();
        this.line = in.readInt();
        this.decKey = in.readString();
    }

    public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public Conversation createFromParcel(Parcel source) {
            return new Conversation(source);
        }

        @Override
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static int hashCode(Object... a) {
        if (a == null)
            return 0;

        int result = 1;

        for (Object element : a)
            result = 31 * result + (element == null ? 0 : element.hashCode());

        return result;
    }
}
