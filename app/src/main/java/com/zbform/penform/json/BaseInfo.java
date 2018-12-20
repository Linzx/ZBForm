package com.zbform.penform.json;

public class BaseInfo {
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
}
