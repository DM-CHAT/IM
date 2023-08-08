package cn.wildfirechat.message;

import android.app.Activity;
import android.content.Context;
import android.os.Parcel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.wildfirechat.client.ClientService;
import cn.wildfirechat.client.R;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;

import static cn.wildfirechat.message.core.MessageContentType.ContentType_RedPacket;


@ContentTag(type = ContentType_RedPacket, flag = PersistFlag.Persist_And_Count)
public class RedPacketMessageContent extends MessageContent {
    public String id;
    public String text;
    public String info;
    public int state;

    public RedPacketMessageContent() {
    }

    public RedPacketMessageContent(JSONObject json){



        this.id = json.getString("packetID");
        this.text = json.getString("text");
        this.info = json.getString("info");

        if (this.info == null) {
            JSONObject infoJson = new JSONObject();
            infoJson.put("wallet", json.getString("wallet"));
            infoJson.put("coinType", json.getString("coinType"));
            this.info = infoJson.toJSONString();
        }

        System.out.println("@@@ info1 : " + info);

        this.state = json.getIntValue("state");
    }
    public RedPacketMessageContent(String id, int state, String text, String info) {
        this.id = id;
        this.text = text;
        this.info = info;
        System.out.println("@@@ info2 : " + info);
        this.state = state;
    }

    @Override
    public MessagePayload encode() {
        return super.encode();
    }


    @Override
    public void decode(MessagePayload payload) {
    }

    @Override
    public String digest(Message message, Context context) {
        if(message.conversation.type == Conversation.ConversationType.Group){
            return context.getString(R.string.red_packet_digest);
        }else{
            return context.getString(R.string.transfer);
        }

    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.id);
        dest.writeString(this.text);
        dest.writeString(this.info);
        dest.writeInt(this.state);
    }

    protected RedPacketMessageContent(Parcel in) {
        super(in);
        this.id = in.readString();
        this.text = in.readString();
        this.info = in.readString();
        this.state = in.readInt();
    }

    public static final Creator<RedPacketMessageContent> CREATOR = new Creator<RedPacketMessageContent>() {
        @Override
        public RedPacketMessageContent createFromParcel(Parcel source) {
            return new RedPacketMessageContent(source);
        }

        @Override
        public RedPacketMessageContent[] newArray(int size) {
            return new RedPacketMessageContent[size];
        }
    };
}
