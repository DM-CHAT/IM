package cn.wildfire.chat.kit.info;

import java.util.List;

public class KeyWordList {


    /**
     * keywordList : [{"content":"测试3","id":4},{"content":"测试2","id":3},{"content":"测试1","id":2},{"content":"测试","id":1}]
     * minute : 0
     * type : 0
     */

    private int minute;
    private int type;
    private List<KeywordListBean> keywordList;

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<KeywordListBean> getKeywordList() {
        return keywordList;
    }

    public void setKeywordList(List<KeywordListBean> keywordList) {
        this.keywordList = keywordList;
    }

    public static class KeywordListBean {
        /**
         * content : 测试3
         * id : 4
         */

        private String content;
        private int id;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
