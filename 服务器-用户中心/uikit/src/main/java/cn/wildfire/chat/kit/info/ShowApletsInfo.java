package cn.wildfire.chat.kit.info;

public class ShowApletsInfo {


    /**
     * msg : 操作成功
     * code : 200
     * data : {"dappUrlFront":"{\"param\":\"\",\"name\":\"zolo square\",\"url\":\"http://qiniu.feiyboy.com/test/1660831967841.jpg\",\"target\":\"OSNS6qJXowk8ipXWrzUpvS7QYp2XQcUo5xGWMbKRLXgEx349NQY\",\"info\":{\"sign\":\"MEYCIQCgomvfuAkG1mjUsKU/RS00jHPKSVNMK8o9wRo6ZfdHywIhAL3v6qLldF8nn66YMmkwEBwl8BcPPJjrwaZqnAfY2ohE\"}}","url":"\"http://qiniu.feiyboy.com/test/1660831967841.jpg\""}
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
         * dappUrlFront : {"param":"","name":"zolo square","url":"http://qiniu.feiyboy.com/test/1660831967841.jpg","target":"OSNS6qJXowk8ipXWrzUpvS7QYp2XQcUo5xGWMbKRLXgEx349NQY","info":{"sign":"MEYCIQCgomvfuAkG1mjUsKU/RS00jHPKSVNMK8o9wRo6ZfdHywIhAL3v6qLldF8nn66YMmkwEBwl8BcPPJjrwaZqnAfY2ohE"}}
         * url : "http://qiniu.feiyboy.com/test/1660831967841.jpg"
         */

        private String dappUrlFront;
        private String url;

        public String getDappUrlFront() {
            return dappUrlFront;
        }

        public void setDappUrlFront(String dappUrlFront) {
            this.dappUrlFront = dappUrlFront;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
