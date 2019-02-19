package com.zbform.penform.json;

public class RecognizeResultInfo {
    int errcode;
    String msg;
    ResultData[] data;

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ResultData[] getData() {
        return data;
    }

    public void setData(ResultData[] data) {
        this.data = data;
    }
}
