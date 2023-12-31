package cn.wildfirechat.message;

import android.app.Activity;
import android.content.Context;
import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.client.R;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.model.LitappInfo;

import static cn.wildfirechat.message.core.MessageContentType.ContentType_Card;


@ContentTag(type = ContentType_Card, flag = PersistFlag.Persist_And_Count)
public class CardMessageContent extends MessageContent {
    /**
     * 0，用户；1，群组；4，DAPP
     */
    private int type;
    private String target;  //share: 发布分享的目标名字，比如
    // 用户名，一般是type为用户时使用
    private String name;
    private String displayName;
    private String portrait;
    private String theme;
    private String url;
    private String info;
    private String param = "";

    public static final int CardType_User = 0;
    public static final int CardType_Group = 1;
    public static final int CardType_ChatRoom = 2;
    public static final int CardType_Channel = 3;
    public static final int CardType_Litapp = 4;
    public static final int CardType_Share = 5;

    public CardMessageContent() {
    }

    public CardMessageContent(int type, String target, String name, String displayName, String portrait) {
        this.type = type;
        this.name = name;
        this.target = target;
        this.displayName = displayName;
        this.portrait = portrait;
    }

    // Dapp 的卡片没有传param 和info？ 发过去以后如何校验？
    public CardMessageContent(int type, String target, String name, String displayName, String portrait, String theme, String url) {
        this.type = type;
        this.name = name;
        this.target = target;
        this.displayName = displayName;
        this.portrait = portrait;
        this.theme = theme;
        this.url = url;
    }

    public CardMessageContent(LitappInfo info) {
        type = CardType_Litapp;
        target = info.target;
        name = info.name;
        displayName = info.displayName;
        portrait = info.portrait;
        theme = info.theme;
        url = info.url;
        this.info = info.info;
        param = info.param;
    }

    public CardMessageContent(com.alibaba.fastjson.JSONObject json2) {
        LitappInfo dappInfo = new LitappInfo(json2);
        type = CardType_Litapp;
        target = dappInfo.target;
        name = dappInfo.name;
        displayName = dappInfo.displayName;
        portrait = dappInfo.portrait;
        theme = dappInfo.theme;
        url = dappInfo.url;
        this.info = dappInfo.info;
        param = dappInfo.param;
    }


    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTheme(){
        return theme;
    }
    public void setTheme(String theme){
        this.theme = theme;
    }
    public String getUrl(){
        return url;
    }
    public void setUrl(String url){
        this.url = url;
    }
    public String getInfo(){
        return info;
    }
    public void setInfo(String info){
        this.info = info;
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        payload.content = target;
        try {
            JSONObject objWrite = new JSONObject();
            objWrite.put("t", type);
            objWrite.put("n", name);
            objWrite.put("d", displayName);
            objWrite.put("p", portrait);

            payload.binaryContent = objWrite.toString().getBytes();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return payload;
    }


    @Override
    public void decode(MessagePayload payload) {
        target = payload.content;
        try {
            if (payload.binaryContent != null) {
                JSONObject jsonObject = new JSONObject(new String(payload.binaryContent));
                type = jsonObject.optInt("t");
                name = jsonObject.optString("n");
                displayName = jsonObject.optString("d");
                portrait = jsonObject.optString("p");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String digest(Message message, Context context) {
        if (type == 0)
            return context.getString(R.string.card_user_digest) + displayName;
        else if (type == 1)
            return context.getString(R.string.card_group_digest) + displayName;
        else if (type == 2)
            return context.getString(R.string.card_room_digest) + displayName;
        else if (type == 3)
            return context.getString(R.string.card_channel_digest) + displayName;
        return context.getString(R.string.card_digest) + displayName;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.type);
        dest.writeString(this.target);
        dest.writeString(this.name != null ? this.name : "");
        dest.writeString(this.displayName != null ? this.displayName : "");
        dest.writeString(this.portrait != null ? this.portrait : "");
        dest.writeString(this.theme != null ? this.theme : "");
        dest.writeString(this.url != null ? this.url : "");
        dest.writeString(this.info != null ? this.info : "");
    }

    protected CardMessageContent(Parcel in) {
        super(in);
        this.type = in.readInt();
        this.target = in.readString();
        this.name = in.readString();
        this.displayName = in.readString();
        this.portrait = in.readString();
        this.theme = in.readString();
        this.url = in.readString();
        this.info = in.readString();
    }

    public static final Creator<CardMessageContent> CREATOR = new Creator<CardMessageContent>() {
        @Override
        public CardMessageContent createFromParcel(Parcel source) {
            return new CardMessageContent(source);
        }

        @Override
        public CardMessageContent[] newArray(int size) {
            return new CardMessageContent[size];
        }
    };
}
