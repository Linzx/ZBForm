package com.zbform.penform.json;

public class UserInfo {

    public Header header;
    public Results[] results;
    public static class Header {

        private String errorCode;
        private String errorMsg;
        private String reqTimestamp;
        private String respTimestamp;
        private int count;

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        public String getReqTimestamp() {
            return reqTimestamp;
        }

        public void setReqTimestamp(String reqTimestamp) {
            this.reqTimestamp = reqTimestamp;
        }

        public String getRespTimestamp() {
            return respTimestamp;
        }

        public void setRespTimestamp(String respTimestamp) {
            this.respTimestamp = respTimestamp;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

    }

    public static class Results {
        private String userCode;
        private String group;
        private String groupname;
        private String groupshortName;
        private String type;
        private String name;
        private String email;
        private String phone;
        private String permission;
        private String keystr;
        private String modifyDate;

        public String getUserCode() {
            return userCode;
        }

        public void setUserCode(String userCode) {
            this.userCode = userCode;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getGroupname() {
            return groupname;
        }

        public void setGroupname(String groupname) {
            this.groupname = groupname;
        }

        public String getGroupshortName() {
            return groupshortName;
        }

        public void setGroupshortName(String groupshortName) {
            this.groupshortName = groupshortName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPermission() {
            return permission;
        }

        public void setPermission(String permission) {
            this.permission = permission;
        }

        public String getKeystr() {
            return keystr;
        }

        public void setKeystr(String keystr) {
            this.keystr = keystr;
        }

        public String getModifyDate() {
            return modifyDate;
        }

        public void setModifyDate(String modifyDate) {
            this.modifyDate = modifyDate;
        }
    }

}
