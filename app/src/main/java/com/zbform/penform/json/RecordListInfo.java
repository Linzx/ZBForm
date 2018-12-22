package com.zbform.penform.json;

import java.util.Arrays;

public class RecordListInfo extends BaseInfo {
    public Results[] results;

    public static class Results {
        String code;
        String uuid;
        String group;
        String name;
        RecordItem[] items;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public RecordItem[] getItems() {
            return items;
        }

        public void setItems(RecordItem[] items) {
            this.items = items;
        }

        @Override
        public String toString() {
            return "Results{" +
                    "code='" + code + '\'' +
                    ", uuid='" + uuid + '\'' +
                    ", group='" + group + '\'' +
                    ", name='" + name + '\'' +
                    ", items=" + Arrays.toString(items) +
                    '}';
        }
    }
}
