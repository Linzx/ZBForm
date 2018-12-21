package com.zbform.penform.json;

public class RecordInfo extends BaseInfo {

    public Results[] results;

    public static class Results extends BaseFormRecordResults{

        private RecordDataItem[] items;

        public RecordDataItem[] getItems() {
            return items;
        }

        public void setItems(RecordDataItem[] items) {
            this.items = items;
        }
    }
}
