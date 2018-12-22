package com.zbform.penform.json;

public class FormListInfo extends BaseInfo {

    public Results[] results;

    public static class Results {
        private String code;
        private String uuid;
        private String group;
        private String name;
        private double size;
        private int page;
        private double heigh;
        private double width;
        private String conf;
        private String dataTable;
        private String state;
        private String rinit;
        private int rcount;
        private String version;
        private String createDate;
        private String modifyDate;

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

        public double getSize() {
            return size;
        }

        public void setSize(double size) {
            this.size = size;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public double getHeigh() {
            return heigh;
        }

        public void setHeigh(double heigh) {
            this.heigh = heigh;
        }

        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public String getConf() {
            return conf;
        }

        public void setConf(String conf) {
            this.conf = conf;
        }

        public String getDataTable() {
            return dataTable;
        }

        public void setDataTable(String dataTable) {
            this.dataTable = dataTable;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getRinit() {
            return rinit;
        }

        public void setRinit(String rinit) {
            this.rinit = rinit;
        }

        public int getRcount() {
            return rcount;
        }

        public void setRcount(int rcount) {
            this.rcount = rcount;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getCreateDate() {
            return createDate;
        }

        public void setCreateDate(String createDate) {
            this.createDate = createDate;
        }

        public String getModifyDate() {
            return modifyDate;
        }

        public void setModifyDate(String modifyDate) {
            this.modifyDate = modifyDate;
        }
    }
}
