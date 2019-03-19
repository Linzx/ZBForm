package com.zbform.penform.db;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

@Table(name = "formsetting")
public class FormSettingEntity extends EntityBase {

    @Column(column = "formid")
    String formid;

    @Column(column = "opentype")
    int opentype;

    @Column(column = "recordcount")
    int recordcount;

    public String getFormid() {
        return formid;
    }

    public void setFormid(String formid) {
        this.formid = formid;
    }

    public int getOpentype() {
        return opentype;
    }

    public void setOpentype(int opentype) {
        this.opentype = opentype;
    }

    public int getRecordcount() {
        return recordcount;
    }

    public void setRecordcount(int recordcount) {
        this.recordcount = recordcount;
    }

}
