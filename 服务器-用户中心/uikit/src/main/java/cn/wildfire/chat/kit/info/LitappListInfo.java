package cn.wildfire.chat.kit.info;

import java.util.List;

public class LitappListInfo {


    /**
     * msg : success
     * code : 200
     * data : [{"id":11,"data_json":"{\"displayName\":\"\",\"name\":\"OK HASH\",\"param\":\"\",\"portrait\":\"http://hashgame.luckmoney8888.com/im_img/hash.png\",\"target\":\"OSNS6qHzQ82DNFxPnEPFV5N6ZzCRp2CdrGd5JKPC4vRVNUPhsQ7\",\"theme\":\"\",\"url\":\"http://hashgame.luckmoney8888.com/hashgame/index.html?shareId\\u003dOSNU6nftHiaYjiaxqCaSwtKKKtJ3JBUwmwnfaN955KBzFBoVJ2L\"}","osnId":"OSNU6nftHiaYjiaxqCaSwtKKKtJ3JBUwmwnfaN955KBzFBoVJ2L","state":1},{"id":12,"data_json":"{\"displayName\":\"\",\"name\":\"OK HASH\",\"param\":\"\",\"portrait\":\"http://hashgame.luckmoney8888.com/im_img/hash.png\",\"target\":\"OSNS6qHzQ82DNFxPnEPFV5N6ZzCRp2CdrGd5JKPC4vRVNUPhsQ7\",\"theme\":\"\",\"url\":\"http://hashgame.luckmoney8888.com/hashgame/index.html?shareId\\u003dOSNU6ngAAQCRpMf3jopJ6bQjr4Uu568sx2FoqkK6SD6MVpSuJPD\"}","osnId":"OSNU6nftHiaYjiaxqCaSwtKKKtJ3JBUwmwnfaN955KBzFBoVJ2L","state":1}]
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
         * id : 11
         * data_json : {"displayName":"","name":"OK HASH","param":"","portrait":"http://hashgame.luckmoney8888.com/im_img/hash.png","target":"OSNS6qHzQ82DNFxPnEPFV5N6ZzCRp2CdrGd5JKPC4vRVNUPhsQ7","theme":"","url":"http://hashgame.luckmoney8888.com/hashgame/index.html?shareId\u003dOSNU6nftHiaYjiaxqCaSwtKKKtJ3JBUwmwnfaN955KBzFBoVJ2L"}
         * osnId : OSNU6nftHiaYjiaxqCaSwtKKKtJ3JBUwmwnfaN955KBzFBoVJ2L
         * state : 1
         */

        private int id;
        private String data_json;
        private String osnId;
        private int state;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getData_json() {
            return data_json;
        }

        public void setData_json(String data_json) {
            this.data_json = data_json;
        }

        public String getOsnId() {
            return osnId;
        }

        public void setOsnId(String osnId) {
            this.osnId = osnId;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }
    }
}
