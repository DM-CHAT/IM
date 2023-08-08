package cn.wildfirechat.message;

import static cn.wildfirechat.message.core.MessageContentType.ContentType_Call;

import android.content.Context;
import android.os.Parcel;

import com.alibaba.fastjson.JSONArray;

import java.util.List;

import cn.wildfirechat.client.R;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;

@ContentTag(type = ContentType_Call, flag = PersistFlag.Persist)
public class CallMessageContent extends MessageContent {

    public static int typeVideo = 0;
    public static int typeAudio = 1;
    public static int modeSingle = 0;
    public static int modeMultiple = 1;
    public static int actionInvite = 0;
    public static int actionAnswer = 1;
    public static int actionReject = 2;
    public static int actionFinish = 3;
    public static int actionStream = 4;
    public static int actionStreams = 5;
    public static int actionCancel = 6;

    public int id;
    public int type;
    public int mode;
    public int action;
    public int duration = -1;
    public String status = "";
    public String url;
    public String user;
    public List<String> urls;
    public List<String> users;
    public String voiceBaseUrl;
    public String voiceHostUrl;

    public CallMessageContent(){
        this.messageType = MessageContentMediaType.CALL;
    }
    @Override
    public void decode(MessagePayload payload) {
    }

    @Override
    public String digest(Message message, Context context) {
        return context.getString(R.string.audio_and_video_call);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(type);
        dest.writeInt(mode);
        dest.writeInt(action);
        dest.writeInt(duration);
        dest.writeString(status);
        dest.writeString(url);
        dest.writeString(user);
        dest.writeString(JSONArray.toJSONString(urls));
        dest.writeString(JSONArray.toJSONString(users));
        dest.writeString(voiceBaseUrl);
        dest.writeString(voiceHostUrl);
    }
    public CallMessageContent(Parcel in) {
        super(in);
        id = in.readInt();
        type = in.readInt();
        mode = in.readInt();
        action = in.readInt();
        duration = in.readInt();
        status = in.readString();
        url = in.readString();
        user = in.readString();
        urls = JSONArray.parseArray(in.readString(), String.class);
        users = JSONArray.parseArray(in.readString(), String.class);
        voiceBaseUrl = in.readString();
        voiceHostUrl = in.readString();
    }
    public static final Creator<CallMessageContent> CREATOR = new Creator<CallMessageContent>() {
        @Override
        public CallMessageContent createFromParcel(Parcel source) {
            return new CallMessageContent(source);
        }

        @Override
        public CallMessageContent[] newArray(int size) {
            return new CallMessageContent[size];
        }
    };

}
