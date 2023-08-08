package cn.wildfire.chat.kit.info;


import java.util.List;

public class AboutUsBean2 {

    public String msg;
    public Integer code;
    public List<DataBean> data;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        public Integer id;
        public String dapp_url_front;
        public String dapp_url_back;
        public String create_time;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getDapp_url_front() {
            return dapp_url_front;
        }

        public void setDapp_url_front(String dapp_url_front) {
            this.dapp_url_front = dapp_url_front;
        }

        public String getDapp_url_back() {
            return dapp_url_back;
        }

        public void setDapp_url_back(String dapp_url_back) {
            this.dapp_url_back = dapp_url_back;
        }

        public String getCreate_time() {
            return create_time;
        }

        public void setCreate_time(String create_time) {
            this.create_time = create_time;
        }
    }
}
