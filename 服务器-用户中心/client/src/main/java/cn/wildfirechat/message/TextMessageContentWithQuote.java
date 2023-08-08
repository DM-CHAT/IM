package cn.wildfirechat.message;



import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.util.List;

import cn.wildfirechat.model.QuoteInfo;

public class TextMessageContentWithQuote {

    public String contentString;
    private JSONObject json;


    public TextMessageContentWithQuote(String content) {
        contentString = content;
        try {
            json = JSONObject.parseObject(content);
        } catch (Exception e) {

        }
    }

    public String getContent() {
        if (json != null) {
            if (json.containsKey("text"))
                return json.getString("text");
            if (json.containsKey("data"))
                return json.getString("data");
        }
        return null;
    }

    public int getMentionedType() {
        if (json != null) {
            try {
                return json.getIntValue("mentionedType");
            }catch (Exception e) {

            }
        }
        return 0;
    }

    public List<String> getMentionedTargets() {

        if (json != null) {
            try {
                List<String> list = json.getObject("mentionedTargets", new TypeReference<List<String>>(){});
                return list;
            } catch (Exception e) {

            }
        }
        return null;
    }

    public QuoteInfo getQuote() {
        if (json != null) {
            try {
                JSONObject quoteJson = json.getJSONObject("quoteInfo");
                QuoteInfo quoteInfo = new QuoteInfo();
                if (quoteJson.containsKey("u")) {
                    long u = quoteJson.getLongValue("u");
                    String i = quoteJson.getString("i");
                    String n = quoteJson.getString("n");
                    String d = quoteJson.getString("d");
                    String hash = quoteJson.getString("hash");
                    String hash0 = quoteJson.getString("hash0");
                    quoteInfo.setQuoteInfo(u,i,n,d,hash,hash0);
                } else {
                    long u = quoteJson.getLongValue("messageUid");
                    String i = quoteJson.getString("userId");
                    String n = quoteJson.getString("userDisplayName");
                    String d = quoteJson.getString("messageDigest");
                    String hash = quoteJson.getString("messageHash");
                    String hash0 = quoteJson.getString("messageHash0");
                    quoteInfo.setQuoteInfo(u,i,n,d,hash,hash0);
                }

                return quoteInfo;
            }catch (Exception e) {

            }
        }
        return null;
    }

}
