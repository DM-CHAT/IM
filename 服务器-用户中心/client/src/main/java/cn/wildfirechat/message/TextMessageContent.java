/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.message;

import android.app.Activity;
import android.content.Context;
import android.os.Parcel;


import cn.wildfirechat.ContentsQuoteInfo;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.model.QuoteInfo;

import static cn.wildfirechat.message.core.MessageContentType.ContentType_Text;

import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * Created by heavyrain lee on 2017/12/6.
 */

@ContentTag(type = ContentType_Text, flag = PersistFlag.Persist_And_Count)
public class TextMessageContent extends MessageContent {
    public String content;
    // 引用信息
    private QuoteInfo quoteInfo;

    public TextMessageContent() {
        this.messageType = MessageContentMediaType.GENERAL;
    }

    public TextMessageContent(String content) {
        this.content = content;
        this.messageType = MessageContentMediaType.GENERAL;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {

        try {
            //JSONObject contentJson = JSONObject.parseObject(content);
            this.content = content;
        } catch (Exception e) {
        }
    }

    public void setContent2(String content) {
        // 这里传content进来
        try {

            TextMessageContentWithQuote content2 = new TextMessageContentWithQuote(content);
            this.content = content2.getContent();
            quoteInfo = content2.getQuote();
            this.mentionedType = content2.getMentionedType();
            this.mentionedTargets = content2.getMentionedTargets();

            if (quoteInfo != null) {
                System.out.println("[Quote] setContent2 ----------------------");
                System.out.println("[Quote] setContent2 content:" + this.content);
                System.out.println("[Quote] setContent2 digest:" + quoteInfo.getMessageDigest());
                System.out.println("[Quote] setContent2 hash:" + quoteInfo.getMessageHash());
                System.out.println("[Quote] setContent2 hash0:" + quoteInfo.getMessageHash0());
                System.out.println("[Quote] setContent2 data:" + content);
            }


        } catch (Exception e) {
            System.out.println("@@@@@ setContent2 Exception");
        }
    }

    public QuoteInfo getQuoteInfo() {
        return quoteInfo;
    }

    public void setQuoteInfo(QuoteInfo quoteInfo) {
        this.quoteInfo = quoteInfo;
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        payload.searchableContent = content;
        payload.mentionedType = mentionedType;
        payload.mentionedTargets = mentionedTargets;
        if (quoteInfo != null) {
            JSONObject object = new JSONObject();
            try {
                object.put("quote", quoteInfo.encode());
                payload.binaryContent = object.toString().getBytes();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return payload;
    }


    @Override
    public void decode(MessagePayload payload) {
        content = payload.searchableContent;
        mentionedType = payload.mentionedType;
        mentionedTargets = payload.mentionedTargets;
        if (payload.binaryContent != null && payload.binaryContent.length > 0) {
            try {
                JSONObject object = new JSONObject(new String(payload.binaryContent));
                quoteInfo = new QuoteInfo();
                quoteInfo.decode(object.optJSONObject("quote"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String digest(Message message, Context context) {
        return content;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.content);
        dest.writeParcelable(this.quoteInfo, flags);
    }

    protected TextMessageContent(Parcel in) {
        super(in);
        this.content = in.readString();
        this.quoteInfo = in.readParcelable(QuoteInfo.class.getClassLoader());
        this.messageType = MessageContentMediaType.GENERAL;
    }

    public static final Creator<TextMessageContent> CREATOR = new Creator<TextMessageContent>() {
        @Override
        public TextMessageContent createFromParcel(Parcel source) {
            return new TextMessageContent(source);
        }

        @Override
        public TextMessageContent[] newArray(int size) {
            return new TextMessageContent[size];
        }
    };
}
