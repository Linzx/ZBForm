package com.zbform.penform.json;

public class NewRecordInfo extends BaseInfo {

    public Results[] results;

    public static class Results{
        public String uuid;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }
    }
}
