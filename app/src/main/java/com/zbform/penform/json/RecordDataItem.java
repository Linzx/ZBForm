package com.zbform.penform.json;

public class RecordDataItem {

    public String code;
    public int form;
    public int record;
    public HwData hwdata;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getForm() {
        return form;
    }

    public void setForm(int form) {
        this.form = form;
    }

    public int getRecord() {
        return record;
    }

    public void setRecord(int record) {
        this.record = record;
    }

    public HwData getHwdata() {
        return hwdata;
    }

    public void setHwdata(HwData hwdata) {
        this.hwdata = hwdata;
    }
}
