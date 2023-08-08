package cn.wildfire.chat.app.login.model;

import java.util.List;

public class StartInfo {


    /**
     * code : 200
     * data : [{"id":1,"iconUrl":"https://luckmoney8888.com/static/zolo.jpg","serviceName":"J-Talking","url":"https://luckmoney8888.com/#/login","serviceRemark":"杀戮空间阿拉山口的房价","appRemark":"阿斯顿发送到发斯蒂芬","appIntroduction":"大事发生打发斯蒂芬","createTime":"2022-09-28T14:34:39.000+08:00"},{"id":2,"iconUrl":"https://luckmoney8888.com/static/zolo.jpg","serviceName":"J-Talking beta","url":"http://127.0.0.1:81/#/login","serviceRemark":"阿斯顿发送到发到付","appRemark":"委托人如果是染发膏","appIntroduction":"回归甲方同意后几天染发膏","createTime":"2022-09-28T14:34:39.000+08:00"},{"id":7,"iconUrl":"https://luckmoney8888.com/static/logo-1.png","serviceName":"XChat","url":"https://luckmoney8888.com/dist1/#/login","serviceRemark":null,"appRemark":null,"appIntroduction":null,"createTime":null},{"id":8,"iconUrl":"https://luckmoney8888.com/static/logo-2.png","serviceName":"HT","url":"https://luckmoney8888.com/dist2/#/login","serviceRemark":null,"appRemark":null,"appIntroduction":null,"createTime":null},{"id":9,"iconUrl":"https://luckmoney8888.com/static/logo-3.png","serviceName":"Memo","url":"https://luckmoney8888.com/dist4/#/login","serviceRemark":null,"appRemark":null,"appIntroduction":null,"createTime":null},{"id":10,"iconUrl":"https://luckmoney8888.com/static/logo-4.png","serviceName":"Sport-Chat","url":"https://luckmoney8888.com/dist3/#/login","serviceRemark":null,"appRemark":null,"appIntroduction":null,"createTime":null}]
     * success : 操作成功
     */

    private int code;
    private String success;
    private List<DataBean> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
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
         * iconUrl : https://luckmoney8888.com/static/zolo.jpg
         * serviceName : J-Talking
         * url : https://luckmoney8888.com/#/login
         * serviceRemark : 杀戮空间阿拉山口的房价
         * appRemark : 阿斯顿发送到发斯蒂芬
         * appIntroduction : 大事发生打发斯蒂芬
         * createTime : 2022-09-28T14:34:39.000+08:00
         */

        private int id;
        private String iconUrl;
        private String serviceName;
        private String url;
        private String serviceRemark;
        private String appRemark;
        private String appIntroduction;
        private String createTime;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getServiceRemark() {
            return serviceRemark;
        }

        public void setServiceRemark(String serviceRemark) {
            this.serviceRemark = serviceRemark;
        }

        public String getAppRemark() {
            return appRemark;
        }

        public void setAppRemark(String appRemark) {
            this.appRemark = appRemark;
        }

        public String getAppIntroduction() {
            return appIntroduction;
        }

        public void setAppIntroduction(String appIntroduction) {
            this.appIntroduction = appIntroduction;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }
    }
}
