package cn.wildfire.chat.app.login.model;

public class NoticeInfo {


    /**
     * msg : success
     * code : 200
     * data : {"id":1,"remark":"　　通知格式 篇4\r\nxxxxxxxx车间处室：\r\n\r\n　　你单位存在以下问题：\r\n\r\n　　以上隐患、缺陷，必须定专人、定措施，在xxxxxxxx年xxx月xxx日前限期完成整改。\r\n\r\n　　特此通知！","create_time":1681106769000,"language":0,"state":1}
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
         * id : 1
         * remark : 　　通知格式 篇4
         xxxxxxxx车间处室：

         　　你单位存在以下问题：

         　　以上隐患、缺陷，必须定专人、定措施，在xxxxxxxx年xxx月xxx日前限期完成整改。

         　　特此通知！
         * create_time : 1681106769000
         * language : 0
         * state : 1
         */

        private int id;
        private String remark;
        private long create_time;
        private int language;
        private int state;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public long getCreate_time() {
            return create_time;
        }

        public void setCreate_time(long create_time) {
            this.create_time = create_time;
        }

        public int getLanguage() {
            return language;
        }

        public void setLanguage(int language) {
            this.language = language;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }
    }
}
