package com.zbform.penform.json;

public class ResultData {
    String formid;
    String recordid;
    Result[] result;

    public String getFormid() {
        return formid;
    }

    public void setFormid(String formid) {
        this.formid = formid;
    }

    public String getRecordid() {
        return recordid;
    }

    public void setRecordid(String recordid) {
        this.recordid = recordid;
    }

    public Result[] getResult() {
        return result;
    }

    public void setResult(Result[] result) {
        this.result = result;
    }
}
