package com.zbform.penform.json;

public class FormInfo extends BaseInfo {

    public Results[] results;

    public static class Results extends BaseFormRecordResults{
        private FormItem[] items;

        public FormItem[] getItems() {
            return items;
        }

        public void setItems(FormItem[] items) {
            this.items = items;
        }
    }
}
