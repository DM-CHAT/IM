package cn.wildfirechat;

import java.util.List;

public class ContentsInfo {


    /**
     * mentionedType : 1
     * mentionedTargets : ["OSNU6nftHiaYjiaxqCaSwtKKKtJ3JBUwmwnfaN955KBzFBoVJ2L"]
     * data : @25854863 5484
     * type : text
     */

    private int mentionedType;
    private String data;
    private String type;
    private List<String> mentionedTargets;

    public int getMentionedType() {
        return mentionedType;
    }

    public void setMentionedType(int mentionedType) {
        this.mentionedType = mentionedType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getMentionedTargets() {
        return mentionedTargets;
    }

    public void setMentionedTargets(List<String> mentionedTargets) {
        this.mentionedTargets = mentionedTargets;
    }
}
