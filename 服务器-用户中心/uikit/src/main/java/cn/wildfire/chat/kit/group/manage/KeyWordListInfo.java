package cn.wildfire.chat.kit.group.manage;

import java.util.List;

public class KeyWordListInfo {


    /**
     * msg : success
     * code : 200
     * data : {"keywordList":[{"id":1,"osnId":null,"groupId":null,"content":"测试","timestamp":null,"license":null}],"type":0,"minute":0}
     */

    private String msg;
    private int code;
    private DataBean data;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * keywordList : [{"id":1,"osnId":null,"groupId":null,"content":"测试","timestamp":null,"license":null}]
         * type : 0
         * minute : 0
         */

        private int type;
        private int minute;
        private List<KeywordListBean> keywordList;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getMinute() {
            return minute;
        }

        public void setMinute(int minute) {
            this.minute = minute;
        }

        public List<KeywordListBean> getKeywordList() {
            return keywordList;
        }

        public void setKeywordList(List<KeywordListBean> keywordList) {
            this.keywordList = keywordList;
        }

        public static class KeywordListBean {
            /**
             * id : 1
             * osnId : null
             * groupId : null
             * content : 测试
             * timestamp : null
             * license : null
             */

            private int id;
            private Object osnId;
            private Object groupId;
            private String content;
            private Object timestamp;
            private Object license;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public Object getOsnId() {
                return osnId;
            }

            public void setOsnId(Object osnId) {
                this.osnId = osnId;
            }

            public Object getGroupId() {
                return groupId;
            }

            public void setGroupId(Object groupId) {
                this.groupId = groupId;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }

            public Object getTimestamp() {
                return timestamp;
            }

            public void setTimestamp(Object timestamp) {
                this.timestamp = timestamp;
            }

            public Object getLicense() {
                return license;
            }

            public void setLicense(Object license) {
                this.license = license;
            }
        }
    }
}
