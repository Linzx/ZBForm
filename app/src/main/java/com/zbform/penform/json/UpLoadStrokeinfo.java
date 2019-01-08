package com.zbform.penform.json;

public class UpLoadStrokeinfo extends BaseInfo {

    public Results[] results;

    public static class Results{
        public String count;

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
        }
    }
}
