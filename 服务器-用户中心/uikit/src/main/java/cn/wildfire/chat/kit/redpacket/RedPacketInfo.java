package cn.wildfire.chat.kit.redpacket;

public class RedPacketInfo {


    /**
     * msg : success
     * coinType : USD
     * code : 200
     * wallet : {"param":"","name":"JT wallet","portrait":"https://luckmoney8888.com/static/zolo.jpg","url":"https://luckmoney8888.com/#/dappbalance","target":"OSNS6qJE5Y5JqH9hosq3bhzydniWzRZM8F18MQ24gLsqQzque49","info":{"sign":"MEUCIHobT3lPD0JmRbhyqWxhbNdsYtR1pNkS7FOXk/Lgc7ltAiEA2A8prKXccTKLItl6G/MbsWCF51ipL1eslqmrPuDSE6c="}}
     * data : {"result":"success","queryUrl":"https://luckmoney8888.com/api/transfer/query","txid":"3e2dad4b88b00a0ffb7c1c6eef62eaff039d496dfb9eed40c94e93fc3e87cdee","type":"transaction","command":"transfer","hash":"BfjzGmkax7kWBTWMbu0Oauz8nl5VfwSpYSafWHqY9qs="}
     */

    private String msg;
    private String coinType;
    private int code;
    private String wallet;
    private DataBean data;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCoinType() {
        return coinType;
    }

    public void setCoinType(String coinType) {
        this.coinType = coinType;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getWallet() {
        return wallet;
    }

    public void setWallet(String wallet) {
        this.wallet = wallet;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * result : success
         * queryUrl : https://luckmoney8888.com/api/transfer/query
         * txid : 3e2dad4b88b00a0ffb7c1c6eef62eaff039d496dfb9eed40c94e93fc3e87cdee
         * type : transaction
         * command : transfer
         * hash : BfjzGmkax7kWBTWMbu0Oauz8nl5VfwSpYSafWHqY9qs=
         */

        private String result;
        private String queryUrl;
        private String txid;
        private String type;
        private String command;
        private String hash;
        private String url;

        public String getUrl(){
            return url;
        }

        public void setUrl(String url){
            this.url = url;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getQueryUrl() {
            return queryUrl;
        }

        public void setQueryUrl(String queryUrl) {
            this.queryUrl = queryUrl;
        }

        public String getTxid() {
            return txid;
        }

        public void setTxid(String txid) {
            this.txid = txid;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }
    }
}
