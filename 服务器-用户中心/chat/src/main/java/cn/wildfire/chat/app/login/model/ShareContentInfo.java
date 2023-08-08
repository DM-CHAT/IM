package cn.wildfire.chat.app.login.model;

public class ShareContentInfo {


    /**
     * msg : success
     * code : 200
     * data : {"url_ios_share":"https://luckmoney8888.com/dmChat/index.html","isActive":0,"url_ios":"https://luckmoney8888.com/im_img/ios_share/share","url":"https://luckmoney8888.com/dmChat/index.html","url_android":"https://luckmoney8888.com/im_img/android/share"}
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
         * url_ios_share : https://luckmoney8888.com/dmChat/index.html
         * isActive : 0
         * url_ios : https://luckmoney8888.com/im_img/ios_share/share
         * url : https://luckmoney8888.com/dmChat/index.html
         * url_android : https://luckmoney8888.com/im_img/android/share
         */

        private String url_ios_share;
        private int isActive;
        private String url_ios;
        private String url;
        private String url_android;

        public String getUrl_ios_share() {
            return url_ios_share;
        }

        public void setUrl_ios_share(String url_ios_share) {
            this.url_ios_share = url_ios_share;
        }

        public int getIsActive() {
            return isActive;
        }

        public void setIsActive(int isActive) {
            this.isActive = isActive;
        }

        public String getUrl_ios() {
            return url_ios;
        }

        public void setUrl_ios(String url_ios) {
            this.url_ios = url_ios;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUrl_android() {
            return url_android;
        }

        public void setUrl_android(String url_android) {
            this.url_android = url_android;
        }
    }
}
