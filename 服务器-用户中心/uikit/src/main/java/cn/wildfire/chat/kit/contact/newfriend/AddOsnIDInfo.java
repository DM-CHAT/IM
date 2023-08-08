package cn.wildfire.chat.kit.contact.newfriend;

public class AddOsnIDInfo {


    /**
     * msg : success
     * code : 200
     * data : {"createDate":null,"updateDate":null,"deleteDate":null,"deleted":null,"id":null,"city":null,"suPhone":null,"suSalt":null,"suPassword":null,"suAvatar":null,"suName":null,"suSex":null,"suBirth":null,"osn_id":"OSNU6ngCirM11sahWScb1bnogmYrDSA4swLm2fH17gYods8MXZD","osn_username":null,"osn_password":null,"osn_node":null,"suEmail":null}
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
         * createDate : null
         * updateDate : null
         * deleteDate : null
         * deleted : null
         * id : null
         * city : null
         * suPhone : null
         * suSalt : null
         * suPassword : null
         * suAvatar : null
         * suName : null
         * suSex : null
         * suBirth : null
         * osn_id : OSNU6ngCirM11sahWScb1bnogmYrDSA4swLm2fH17gYods8MXZD
         * osn_username : null
         * osn_password : null
         * osn_node : null
         * suEmail : null
         */

        private Object createDate;
        private Object updateDate;
        private Object deleteDate;
        private Object deleted;
        private Object id;
        private Object city;
        private Object suPhone;
        private Object suSalt;
        private Object suPassword;
        private Object suAvatar;
        private Object suName;
        private Object suSex;
        private Object suBirth;
        private String osn_id;
        private Object osn_username;
        private Object osn_password;
        private Object osn_node;
        private Object suEmail;

        public Object getCreateDate() {
            return createDate;
        }

        public void setCreateDate(Object createDate) {
            this.createDate = createDate;
        }

        public Object getUpdateDate() {
            return updateDate;
        }

        public void setUpdateDate(Object updateDate) {
            this.updateDate = updateDate;
        }

        public Object getDeleteDate() {
            return deleteDate;
        }

        public void setDeleteDate(Object deleteDate) {
            this.deleteDate = deleteDate;
        }

        public Object getDeleted() {
            return deleted;
        }

        public void setDeleted(Object deleted) {
            this.deleted = deleted;
        }

        public Object getId() {
            return id;
        }

        public void setId(Object id) {
            this.id = id;
        }

        public Object getCity() {
            return city;
        }

        public void setCity(Object city) {
            this.city = city;
        }

        public Object getSuPhone() {
            return suPhone;
        }

        public void setSuPhone(Object suPhone) {
            this.suPhone = suPhone;
        }

        public Object getSuSalt() {
            return suSalt;
        }

        public void setSuSalt(Object suSalt) {
            this.suSalt = suSalt;
        }

        public Object getSuPassword() {
            return suPassword;
        }

        public void setSuPassword(Object suPassword) {
            this.suPassword = suPassword;
        }

        public Object getSuAvatar() {
            return suAvatar;
        }

        public void setSuAvatar(Object suAvatar) {
            this.suAvatar = suAvatar;
        }

        public Object getSuName() {
            return suName;
        }

        public void setSuName(Object suName) {
            this.suName = suName;
        }

        public Object getSuSex() {
            return suSex;
        }

        public void setSuSex(Object suSex) {
            this.suSex = suSex;
        }

        public Object getSuBirth() {
            return suBirth;
        }

        public void setSuBirth(Object suBirth) {
            this.suBirth = suBirth;
        }

        public String getOsn_id() {
            return osn_id;
        }

        public void setOsn_id(String osn_id) {
            this.osn_id = osn_id;
        }

        public Object getOsn_username() {
            return osn_username;
        }

        public void setOsn_username(Object osn_username) {
            this.osn_username = osn_username;
        }

        public Object getOsn_password() {
            return osn_password;
        }

        public void setOsn_password(Object osn_password) {
            this.osn_password = osn_password;
        }

        public Object getOsn_node() {
            return osn_node;
        }

        public void setOsn_node(Object osn_node) {
            this.osn_node = osn_node;
        }

        public Object getSuEmail() {
            return suEmail;
        }

        public void setSuEmail(Object suEmail) {
            this.suEmail = suEmail;
        }
    }
}
