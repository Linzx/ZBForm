package com.zbform.penform.json;

public class RecordInfo extends BaseInfo {

    public Results[] results;

    public static class Results extends BaseFormRecordResults{

        private String recordRecognizeState;

        private RecordDataItem[] items;

        public String getRecordRecognizeState() {
            return recordRecognizeState;
        }

        public void setRecordRecognizeState(String recordRecognizeState) {
            this.recordRecognizeState = recordRecognizeState;
        }

        public RecordDataItem[] getItems() {
            return items;
        }

        public void setItems(RecordDataItem[] items) {
            this.items = items;
        }
    }
}
