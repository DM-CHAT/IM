package cn.wildfire.chat.app.login.model;

public class AppVersionInfo {


    /**
     * msg : success
     * code : 200
     * data : {"apkUrl":"https://luckmoney8888.com/dmChat/DM.apk","updateVersion":0,"androidCode":26,"androidVersion":"1.2.6","iosCode":26,"remark":"优化用户体验","iosVersion":"1.2.4","iosUrl":"https://mdetrq.cc/Xkw","androidShow":1,"user":"OSNU6nfzNd5V4HCZHfPwYksS14BPRjggxyRYfJp5R3jXjPiCPpV,","isShow":1}
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
         * apkUrl : https://luckmoney8888.com/dmChat/DM.apk
         * updateVersion : 0
         * androidCode : 26
         * androidVersion : 1.2.6
         * iosCode : 26
         * remark : 优化用户体验
         * iosVersion : 1.2.4
         * iosUrl : https://mdetrq.cc/Xkw
         * androidShow : 1
         * user : OSNU6nfzNd5V4HCZHfPwYksS14BPRjggxyRYfJp5R3jXjPiCPpV,
         * isShow : 1
         */

        private String apkUrl;
        private int updateVersion;
        private int androidCode;
        private String androidVersion;
        private int iosCode;
        private String remark;
        private String iosVersion;
        private String iosUrl;
        private int androidShow;
        private String user;
        private int isShow;

        public String getApkUrl() {
            return apkUrl;
        }

        public void setApkUrl(String apkUrl) {
            this.apkUrl = apkUrl;
        }

        public int getUpdateVersion() {
            return updateVersion;
        }

        public void setUpdateVersion(int updateVersion) {
            this.updateVersion = updateVersion;
        }

        public int getAndroidCode() {
            return androidCode;
        }

        public void setAndroidCode(int androidCode) {
            this.androidCode = androidCode;
        }

        public String getAndroidVersion() {
            return androidVersion;
        }

        public void setAndroidVersion(String androidVersion) {
            this.androidVersion = androidVersion;
        }

        public int getIosCode() {
            return iosCode;
        }

        public void setIosCode(int iosCode) {
            this.iosCode = iosCode;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getIosVersion() {
            return iosVersion;
        }

        public void setIosVersion(String iosVersion) {
            this.iosVersion = iosVersion;
        }

        public String getIosUrl() {
            return iosUrl;
        }

        public void setIosUrl(String iosUrl) {
            this.iosUrl = iosUrl;
        }

        public int getAndroidShow() {
            return androidShow;
        }

        public void setAndroidShow(int androidShow) {
            this.androidShow = androidShow;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public int getIsShow() {
            return isShow;
        }

        public void setIsShow(int isShow) {
            this.isShow = isShow;
        }
    }
}
