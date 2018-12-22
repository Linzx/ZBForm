package com.zbform.penform.json;

public class RecordItem {

    String hwcode;
    String hwuuid;
    String hwgroup;
    String hwname;
    double hwsize;
    int hwpage;
    double hwheigh;
    double hwwidth;
    String hwcreatedate;
    String hwmodifydate;

    public String getHwcode() {
        return hwcode;
    }

    public void setHwcode(String hwcode) {
        this.hwcode = hwcode;
    }

    public String getHwuuid() {
        return hwuuid;
    }

    public void setHwuuid(String hwuuid) {
        this.hwuuid = hwuuid;
    }

    public String getHwgroup() {
        return hwgroup;
    }

    public void setHwgroup(String hwgroup) {
        this.hwgroup = hwgroup;
    }

    public String getHwname() {
        return hwname;
    }

    public void setHwname(String hwname) {
        this.hwname = hwname;
    }

    public double getHwsize() {
        return hwsize;
    }

    public void setHwsize(double hwsize) {
        this.hwsize = hwsize;
    }

    public int getHwpage() {
        return hwpage;
    }

    public void setHwpage(int hwpage) {
        this.hwpage = hwpage;
    }

    public double getHwheigh() {
        return hwheigh;
    }

    public void setHwheigh(double hwheigh) {
        this.hwheigh = hwheigh;
    }

    public double getHwwidth() {
        return hwwidth;
    }

    public void setHwwidth(double hwwidth) {
        this.hwwidth = hwwidth;
    }

    public String getHwcreatedate() {
        return hwcreatedate;
    }

    public void setHwcreatedate(String hwcreatedate) {
        this.hwcreatedate = hwcreatedate;
    }

    public String getHwmodifydate() {
        return hwmodifydate;
    }

    public void setHwmodifydate(String hwmodifydate) {
        this.hwmodifydate = hwmodifydate;
    }

    @Override
    public String toString() {
        return "RecordItem{" +
                "hwcode='" + hwcode + '\'' +
                ", hwuuid='" + hwuuid + '\'' +
                ", hwgroup='" + hwgroup + '\'' +
                ", hwname='" + hwname + '\'' +
                ", hwsize=" + hwsize +
                ", hwpage=" + hwpage +
                ", hwheigh=" + hwheigh +
                ", hwwidth=" + hwwidth +
                ", hwcreatedate='" + hwcreatedate + '\'' +
                ", hwmodifydate='" + hwmodifydate + '\'' +
                '}';
    }
}
