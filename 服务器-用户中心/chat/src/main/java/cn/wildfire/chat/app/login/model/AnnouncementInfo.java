package cn.wildfire.chat.app.login.model;

import java.util.List;

public class AnnouncementInfo {


    /**
     * msg : success
     * code : 200
     * data : [{"id":1,"title":"中文标题","title_english":"英文标题","title_viet_nam":"越南标题","remark":"中文内容","remark_english":"英文内容","remark_viet_nam":"越南内容","create_time":1681106769000,"language":0,"state":0,"isRead":0},{"id":2,"title":"中文标题","title_english":"英文标题","title_viet_nam":"越南标题","remark":"中文内容","remark_english":"英文内容","remark_viet_nam":"越南内容","create_time":1681106769000,"language":0,"state":1,"isRead":0}]
     */

    private String msg;
    private int code;
    private List<DataBean> data;

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

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : 1
         * title : 中文标题
         * title_english : 英文标题
         * title_viet_nam : 越南标题
         * remark : 中文内容
         * remark_english : 英文内容
         * remark_viet_nam : 越南内容
         * create_time : 1681106769000
         * language : 0
         * state : 0
         * isRead : 0
         */

        private int id;
        private String title;
        private String title_english;
        private String title_viet_nam;
        private String remark;
        private String remark_english;
        private String remark_viet_nam;
        private long create_time;
        private int language;
        private int state;
        private int isRead;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle_english() {
            return title_english;
        }

        public void setTitle_english(String title_english) {
            this.title_english = title_english;
        }

        public String getTitle_viet_nam() {
            return title_viet_nam;
        }

        public void setTitle_viet_nam(String title_viet_nam) {
            this.title_viet_nam = title_viet_nam;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getRemark_english() {
            return remark_english;
        }

        public void setRemark_english(String remark_english) {
            this.remark_english = remark_english;
        }

        public String getRemark_viet_nam() {
            return remark_viet_nam;
        }

        public void setRemark_viet_nam(String remark_viet_nam) {
            this.remark_viet_nam = remark_viet_nam;
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

        public int getIsRead() {
            return isRead;
        }

        public void setIsRead(int isRead) {
            this.isRead = isRead;
        }
    }
}
