package cn.wildfire.chat.app.login.model;

import com.google.gson.annotations.SerializedName;

public class AccountPasswordInfo {


    /**
     * msg : success
     * code : 200
     * data : {"osn_password":"6706d758934b559a0d09","osn_id":"OSNU6nftHiaYjiaxqCaSwtKKKtJ3JBUwmwnfaN955KBzFBoVJ2L","osn_node":"8.219.11.57","json":{"APP_DEVICE":"https://luckmoney8888.com/api/user/device","AccessKeySecret":"j6wqfCVeswhxelFdqBwI0XcXCs2Xbd","GROUP_PROFIT_URL":"https://luckmoney8888.com/index.html#/profit?group_id=","redPack":"https://luckmoney8888.com/#/redPack","MainDapp1":"eyJwYXJhbSI6IiIsIm5hbWUiOiJldmF0Y29pbiB3ZWIiLCJ1cmwiOiJodHRwczovL2x1Y2ttb25leTg4ODguY29tL3N0YXRpYy9ldmF0Y29pbi9pbmRleC5odG1sIiwidGFyZ2V0IjoiT1NOUzZxSkQxUXI5UllheU1lbnBQZjVDdkZBakxyN2pMZkNaVFJ2RmpRa0dobmdhVmpHIiwiaW5mbyI6eyJzaWduIjoiTUVVQ0lRQy9ZdUZnNU1IY0UzOUJ0SmpOd1Zhd3d0djNWZzFqTCt0S0pLdE0xNGdoWlFJZ09WeFRZak95MTk4QjVLYm92ZDYyb3p4Rnk5U0E3ZnhvSDlHdUhuYlMzVFE9In19","BOMB_ROLE_URL":"https://luckmoney8888.com/api/transfer/getBombRole","RemoteTempFilePath":"https://zolo-image1.oss-ap-southeast-1.aliyuncs.com/","voiceBaseUrl":"webrtc://8.219.219.91:1985/live/android/","TRANSFER_URL":"https://luckmoney8888.com/api/transfer/transfer","GroupList":"https://luckmoney8888.com/api/program/list","HOST_IP":"8.219.11.57","GroupPortraitDirectory":"groupPortrait/","WALLET_URL":"https://bingmingff.top/#/wallet","HIDE_ENABLE":"0","APP_URL":"https://luckmoney8888.com/static/download.html","SET_GROUP_URL":"https://luckmoney8888.com/api/transfer/upgradeGroup","AddGroup":"https://luckmoney8888.com/api/groupProgram/add","DEL_ACCOUNT_URL":"https://luckmoney8888.com/api/user/delAccount","ACCOUNT_PREFIX_URL":"https://luckmoney8888.com/api/account/getAccount","QueryAplets":"https://luckmoney8888.com/api/groupProgram/findOne","UserPortraitDirectory":"userPortrait/","GETTRANSFEREST_URL":"https://luckmoney8888.com/api/transfer/getTransferResult","LOGIN_URL":"https://luckmoney8888.com/#/login","GROUP_ZERO":"https://luckmoney8888.com/api/group/zeroGroupOwner","KEFU_LIST":"https://luckmoney8888.com/api/config/custormer","SET_NAME":"https://luckmoney8888.com/api/user/setName","TempDirectory":"temp/","create_group_url":"https://luckmoney8888.com/api/im/createGroup","BombRole":"https://luckmoney8888.com/api/bomb_role/hideEnable","PREFIX":"https://luckmoney8888.com/api","AccessKeyId":"LTAI5tLKaJ9GyGCbAwrmUsnu","Alias":"25888777","ENDPOINT":"https://oss-ap-southeast-1.aliyuncs.com","voiceHostUrl":"http://8.219.219.91:1985","BUCKETNAME":"zolo-image1","GROUP_UPDATE_COUNT":"https://luckmoney8888.com/api/group/getUpgroupUserCount","JpushAppKey":"d66520e25d695b02bc1f925f"},"osn_username":"df29cdc4e54019871128","token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJwaG9uZSI6ImNrXzEzNjE2MEAxNjMuY29tIiwiZXhwIjo0ODMxOTQ1MTIyLCJ1c2VySWQiOiIxNTcyNTY0ODg2MTg4NTg0OTYxIn0.HuYyIFsf7IxTAh5RZHnteiyMJIS-UNJNIzoW5HNMUoY","refreshToken":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJwaG9uZSI6ImNrXzEzNjE2MEAxNjMuY29tIiwiZXhwIjo0ODMxOTQ1MTIyLCJ1c2VySWQiOiIxNTcyNTY0ODg2MTg4NTg0OTYxIn0.HuYyIFsf7IxTAh5RZHnteiyMJIS-UNJNIzoW5HNMUoY"}
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
         * osn_password : 6706d758934b559a0d09
         * osn_id : OSNU6nftHiaYjiaxqCaSwtKKKtJ3JBUwmwnfaN955KBzFBoVJ2L
         * osn_node : 8.219.11.57
         * json : {"APP_DEVICE":"https://luckmoney8888.com/api/user/device","AccessKeySecret":"j6wqfCVeswhxelFdqBwI0XcXCs2Xbd","GROUP_PROFIT_URL":"https://luckmoney8888.com/index.html#/profit?group_id=","redPack":"https://luckmoney8888.com/#/redPack","MainDapp1":"eyJwYXJhbSI6IiIsIm5hbWUiOiJldmF0Y29pbiB3ZWIiLCJ1cmwiOiJodHRwczovL2x1Y2ttb25leTg4ODguY29tL3N0YXRpYy9ldmF0Y29pbi9pbmRleC5odG1sIiwidGFyZ2V0IjoiT1NOUzZxSkQxUXI5UllheU1lbnBQZjVDdkZBakxyN2pMZkNaVFJ2RmpRa0dobmdhVmpHIiwiaW5mbyI6eyJzaWduIjoiTUVVQ0lRQy9ZdUZnNU1IY0UzOUJ0SmpOd1Zhd3d0djNWZzFqTCt0S0pLdE0xNGdoWlFJZ09WeFRZak95MTk4QjVLYm92ZDYyb3p4Rnk5U0E3ZnhvSDlHdUhuYlMzVFE9In19","BOMB_ROLE_URL":"https://luckmoney8888.com/api/transfer/getBombRole","RemoteTempFilePath":"https://zolo-image1.oss-ap-southeast-1.aliyuncs.com/","voiceBaseUrl":"webrtc://8.219.219.91:1985/live/android/","TRANSFER_URL":"https://luckmoney8888.com/api/transfer/transfer","GroupList":"https://luckmoney8888.com/api/program/list","HOST_IP":"8.219.11.57","GroupPortraitDirectory":"groupPortrait/","WALLET_URL":"https://bingmingff.top/#/wallet","HIDE_ENABLE":"0","APP_URL":"https://luckmoney8888.com/static/download.html","SET_GROUP_URL":"https://luckmoney8888.com/api/transfer/upgradeGroup","AddGroup":"https://luckmoney8888.com/api/groupProgram/add","DEL_ACCOUNT_URL":"https://luckmoney8888.com/api/user/delAccount","ACCOUNT_PREFIX_URL":"https://luckmoney8888.com/api/account/getAccount","QueryAplets":"https://luckmoney8888.com/api/groupProgram/findOne","UserPortraitDirectory":"userPortrait/","GETTRANSFEREST_URL":"https://luckmoney8888.com/api/transfer/getTransferResult","LOGIN_URL":"https://luckmoney8888.com/#/login","GROUP_ZERO":"https://luckmoney8888.com/api/group/zeroGroupOwner","KEFU_LIST":"https://luckmoney8888.com/api/config/custormer","SET_NAME":"https://luckmoney8888.com/api/user/setName","TempDirectory":"temp/","create_group_url":"https://luckmoney8888.com/api/im/createGroup","BombRole":"https://luckmoney8888.com/api/bomb_role/hideEnable","PREFIX":"https://luckmoney8888.com/api","AccessKeyId":"LTAI5tLKaJ9GyGCbAwrmUsnu","Alias":"25888777","ENDPOINT":"https://oss-ap-southeast-1.aliyuncs.com","voiceHostUrl":"http://8.219.219.91:1985","BUCKETNAME":"zolo-image1","GROUP_UPDATE_COUNT":"https://luckmoney8888.com/api/group/getUpgroupUserCount","JpushAppKey":"d66520e25d695b02bc1f925f"}
         * osn_username : df29cdc4e54019871128
         * token : eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJwaG9uZSI6ImNrXzEzNjE2MEAxNjMuY29tIiwiZXhwIjo0ODMxOTQ1MTIyLCJ1c2VySWQiOiIxNTcyNTY0ODg2MTg4NTg0OTYxIn0.HuYyIFsf7IxTAh5RZHnteiyMJIS-UNJNIzoW5HNMUoY
         * refreshToken : eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJwaG9uZSI6ImNrXzEzNjE2MEAxNjMuY29tIiwiZXhwIjo0ODMxOTQ1MTIyLCJ1c2VySWQiOiIxNTcyNTY0ODg2MTg4NTg0OTYxIn0.HuYyIFsf7IxTAh5RZHnteiyMJIS-UNJNIzoW5HNMUoY
         */

        private String osn_password;
        private String osn_id;
        private String osn_node;
        private JsonBean json;
        private String osn_username;
        private String token;
        private String refreshToken;

        public String getOsn_password() {
            return osn_password;
        }

        public void setOsn_password(String osn_password) {
            this.osn_password = osn_password;
        }

        public String getOsn_id() {
            return osn_id;
        }

        public void setOsn_id(String osn_id) {
            this.osn_id = osn_id;
        }

        public String getOsn_node() {
            return osn_node;
        }

        public void setOsn_node(String osn_node) {
            this.osn_node = osn_node;
        }

        public JsonBean getJson() {
            return json;
        }

        public void setJson(JsonBean json) {
            this.json = json;
        }

        public String getOsn_username() {
            return osn_username;
        }

        public void setOsn_username(String osn_username) {
            this.osn_username = osn_username;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public static class JsonBean {
            /**
             * APP_DEVICE : https://luckmoney8888.com/api/user/device
             * AccessKeySecret : j6wqfCVeswhxelFdqBwI0XcXCs2Xbd
             * GROUP_PROFIT_URL : https://luckmoney8888.com/index.html#/profit?group_id=
             * redPack : https://luckmoney8888.com/#/redPack
             * MainDapp1 : eyJwYXJhbSI6IiIsIm5hbWUiOiJldmF0Y29pbiB3ZWIiLCJ1cmwiOiJodHRwczovL2x1Y2ttb25leTg4ODguY29tL3N0YXRpYy9ldmF0Y29pbi9pbmRleC5odG1sIiwidGFyZ2V0IjoiT1NOUzZxSkQxUXI5UllheU1lbnBQZjVDdkZBakxyN2pMZkNaVFJ2RmpRa0dobmdhVmpHIiwiaW5mbyI6eyJzaWduIjoiTUVVQ0lRQy9ZdUZnNU1IY0UzOUJ0SmpOd1Zhd3d0djNWZzFqTCt0S0pLdE0xNGdoWlFJZ09WeFRZak95MTk4QjVLYm92ZDYyb3p4Rnk5U0E3ZnhvSDlHdUhuYlMzVFE9In19
             * BOMB_ROLE_URL : https://luckmoney8888.com/api/transfer/getBombRole
             * RemoteTempFilePath : https://zolo-image1.oss-ap-southeast-1.aliyuncs.com/
             * voiceBaseUrl : webrtc://8.219.219.91:1985/live/android/
             * TRANSFER_URL : https://luckmoney8888.com/api/transfer/transfer
             * GroupList : https://luckmoney8888.com/api/program/list
             * HOST_IP : 8.219.11.57
             * GroupPortraitDirectory : groupPortrait/
             * WALLET_URL : https://bingmingff.top/#/wallet
             * HIDE_ENABLE : 0
             * APP_URL : https://luckmoney8888.com/static/download.html
             * SET_GROUP_URL : https://luckmoney8888.com/api/transfer/upgradeGroup
             * AddGroup : https://luckmoney8888.com/api/groupProgram/add
             * DEL_ACCOUNT_URL : https://luckmoney8888.com/api/user/delAccount
             * ACCOUNT_PREFIX_URL : https://luckmoney8888.com/api/account/getAccount
             * QueryAplets : https://luckmoney8888.com/api/groupProgram/findOne
             * UserPortraitDirectory : userPortrait/
             * GETTRANSFEREST_URL : https://luckmoney8888.com/api/transfer/getTransferResult
             * LOGIN_URL : https://luckmoney8888.com/#/login
             * GROUP_ZERO : https://luckmoney8888.com/api/group/zeroGroupOwner
             * KEFU_LIST : https://luckmoney8888.com/api/config/custormer
             * SET_NAME : https://luckmoney8888.com/api/user/setName
             * TempDirectory : temp/
             * create_group_url : https://luckmoney8888.com/api/im/createGroup
             * BombRole : https://luckmoney8888.com/api/bomb_role/hideEnable
             * PREFIX : https://luckmoney8888.com/api
             * AccessKeyId : LTAI5tLKaJ9GyGCbAwrmUsnu
             * Alias : 25888777
             * ENDPOINT : https://oss-ap-southeast-1.aliyuncs.com
             * voiceHostUrl : http://8.219.219.91:1985
             * BUCKETNAME : zolo-image1
             * GROUP_UPDATE_COUNT : https://luckmoney8888.com/api/group/getUpgroupUserCount
             * JpushAppKey : d66520e25d695b02bc1f925f
             */

            @SerializedName("APP_DEVICE")
            private String app_device;
            private String AccessKeySecret;
            @SerializedName("GROUP_PROFIT_URL")
            private String group_profit_url;
            private String redPack;
            private String MainDapp1;
            @SerializedName("BOMB_ROLE_URL")
            private String bomb_role_url;
            private String RemoteTempFilePath;
            private String voiceBaseUrl;
            @SerializedName("TRANSFER_URL")
            private String transfer_url;
            private String GroupList;
            @SerializedName("HOST_IP")
            private String host_ip;
            private String GroupPortraitDirectory;
            @SerializedName("WALLET_URL")
            private String wallet_url;
            @SerializedName("HIDE_ENABLE")
            private String hide_enable;
            @SerializedName("APP_URL")
            private String app_url;
            @SerializedName("SET_GROUP_URL")
            private String set_group_url;
            private String AddGroup;
            @SerializedName("DEL_ACCOUNT_URL")
            private String del_account_url;
            @SerializedName("ACCOUNT_PREFIX_URL")
            private String account_prefix_url;
            private String QueryAplets;
            private String UserPortraitDirectory;
            @SerializedName("GETTRANSFEREST_URL")
            private String gettransferest_url;
            @SerializedName("LOGIN_URL")
            private String login_url;
            @SerializedName("GROUP_ZERO")
            private String group_zero;
            @SerializedName("KEFU_LIST")
            private String kefu_list;
            @SerializedName("SET_NAME")
            private String set_name;
            private String TempDirectory;
            private String create_group_url;
            private String BombRole;
            @SerializedName("PREFIX")
            private String prefix;
            private String AccessKeyId;
            private String Alias;
            @SerializedName("ENDPOINT")
            private String endpoint;
            private String voiceHostUrl;
            @SerializedName("BUCKETNAME")
            private String bucketname;
            @SerializedName("GROUP_UPDATE_COUNT")
            private String group_update_count;
            private String JpushAppKey;

            public String getApp_device() {
                return app_device;
            }

            public void setApp_device(String app_device) {
                this.app_device = app_device;
            }

            public String getAccessKeySecret() {
                return AccessKeySecret;
            }

            public void setAccessKeySecret(String AccessKeySecret) {
                this.AccessKeySecret = AccessKeySecret;
            }

            public String getGroup_profit_url() {
                return group_profit_url;
            }

            public void setGroup_profit_url(String group_profit_url) {
                this.group_profit_url = group_profit_url;
            }

            public String getRedPack() {
                return redPack;
            }

            public void setRedPack(String redPack) {
                this.redPack = redPack;
            }

            public String getMainDapp1() {
                return MainDapp1;
            }

            public void setMainDapp1(String MainDapp1) {
                this.MainDapp1 = MainDapp1;
            }

            public String getBomb_role_url() {
                return bomb_role_url;
            }

            public void setBomb_role_url(String bomb_role_url) {
                this.bomb_role_url = bomb_role_url;
            }

            public String getRemoteTempFilePath() {
                return RemoteTempFilePath;
            }

            public void setRemoteTempFilePath(String RemoteTempFilePath) {
                this.RemoteTempFilePath = RemoteTempFilePath;
            }

            public String getVoiceBaseUrl() {
                return voiceBaseUrl;
            }

            public void setVoiceBaseUrl(String voiceBaseUrl) {
                this.voiceBaseUrl = voiceBaseUrl;
            }

            public String getTransfer_url() {
                return transfer_url;
            }

            public void setTransfer_url(String transfer_url) {
                this.transfer_url = transfer_url;
            }

            public String getGroupList() {
                return GroupList;
            }

            public void setGroupList(String GroupList) {
                this.GroupList = GroupList;
            }

            public String getHost_ip() {
                return host_ip;
            }

            public void setHost_ip(String host_ip) {
                this.host_ip = host_ip;
            }

            public String getGroupPortraitDirectory() {
                return GroupPortraitDirectory;
            }

            public void setGroupPortraitDirectory(String GroupPortraitDirectory) {
                this.GroupPortraitDirectory = GroupPortraitDirectory;
            }

            public String getWallet_url() {
                return wallet_url;
            }

            public void setWallet_url(String wallet_url) {
                this.wallet_url = wallet_url;
            }

            public String getHide_enable() {
                return hide_enable;
            }

            public void setHide_enable(String hide_enable) {
                this.hide_enable = hide_enable;
            }

            public String getApp_url() {
                return app_url;
            }

            public void setApp_url(String app_url) {
                this.app_url = app_url;
            }

            public String getSet_group_url() {
                return set_group_url;
            }

            public void setSet_group_url(String set_group_url) {
                this.set_group_url = set_group_url;
            }

            public String getAddGroup() {
                return AddGroup;
            }

            public void setAddGroup(String AddGroup) {
                this.AddGroup = AddGroup;
            }

            public String getDel_account_url() {
                return del_account_url;
            }

            public void setDel_account_url(String del_account_url) {
                this.del_account_url = del_account_url;
            }

            public String getAccount_prefix_url() {
                return account_prefix_url;
            }

            public void setAccount_prefix_url(String account_prefix_url) {
                this.account_prefix_url = account_prefix_url;
            }

            public String getQueryAplets() {
                return QueryAplets;
            }

            public void setQueryAplets(String QueryAplets) {
                this.QueryAplets = QueryAplets;
            }

            public String getUserPortraitDirectory() {
                return UserPortraitDirectory;
            }

            public void setUserPortraitDirectory(String UserPortraitDirectory) {
                this.UserPortraitDirectory = UserPortraitDirectory;
            }

            public String getGettransferest_url() {
                return gettransferest_url;
            }

            public void setGettransferest_url(String gettransferest_url) {
                this.gettransferest_url = gettransferest_url;
            }

            public String getLogin_url() {
                return login_url;
            }

            public void setLogin_url(String login_url) {
                this.login_url = login_url;
            }

            public String getGroup_zero() {
                return group_zero;
            }

            public void setGroup_zero(String group_zero) {
                this.group_zero = group_zero;
            }

            public String getKefu_list() {
                return kefu_list;
            }

            public void setKefu_list(String kefu_list) {
                this.kefu_list = kefu_list;
            }

            public String getSet_name() {
                return set_name;
            }

            public void setSet_name(String set_name) {
                this.set_name = set_name;
            }

            public String getTempDirectory() {
                return TempDirectory;
            }

            public void setTempDirectory(String TempDirectory) {
                this.TempDirectory = TempDirectory;
            }

            public String getCreate_group_url() {
                return create_group_url;
            }

            public void setCreate_group_url(String create_group_url) {
                this.create_group_url = create_group_url;
            }

            public String getBombRole() {
                return BombRole;
            }

            public void setBombRole(String BombRole) {
                this.BombRole = BombRole;
            }

            public String getPrefix() {
                return prefix;
            }

            public void setPrefix(String prefix) {
                this.prefix = prefix;
            }

            public String getAccessKeyId() {
                return AccessKeyId;
            }

            public void setAccessKeyId(String AccessKeyId) {
                this.AccessKeyId = AccessKeyId;
            }

            public String getAlias() {
                return Alias;
            }

            public void setAlias(String Alias) {
                this.Alias = Alias;
            }

            public String getEndpoint() {
                return endpoint;
            }

            public void setEndpoint(String endpoint) {
                this.endpoint = endpoint;
            }

            public String getVoiceHostUrl() {
                return voiceHostUrl;
            }

            public void setVoiceHostUrl(String voiceHostUrl) {
                this.voiceHostUrl = voiceHostUrl;
            }

            public String getBucketname() {
                return bucketname;
            }

            public void setBucketname(String bucketname) {
                this.bucketname = bucketname;
            }

            public String getGroup_update_count() {
                return group_update_count;
            }

            public void setGroup_update_count(String group_update_count) {
                this.group_update_count = group_update_count;
            }

            public String getJpushAppKey() {
                return JpushAppKey;
            }

            public void setJpushAppKey(String JpushAppKey) {
                this.JpushAppKey = JpushAppKey;
            }
        }
    }
}
