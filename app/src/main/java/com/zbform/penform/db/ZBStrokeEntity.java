package com.zbform.penform.db;


import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;
import com.lidroid.xutils.db.annotation.Transient;
import com.zbform.penform.json.Point;

import java.util.ArrayList;

@Table(name = "zbstroke")
public class ZBStrokeEntity extends EntityBase{
    /// <summary>
    /// 用户ID
    /// </summary>
    @Column(column = "userid") // 建议加上注解， 混淆后列名不受影响
    public String userid;

    /// <summary>
    /// 表单ID
    /// </summary>
    @Column(column = "formid")
    public String formid;

    /// <summary>
    /// 书写记录ID
    /// </summary>
    @Column(column = "recordid")
    public String recordid;

    /// <summary>
    /// 表单内区域项id
    /// </summary>
    @Column(column = "itemid")
    public String itemid;

    @Column(column = "isupload")
    public Boolean isupload;

    @Column(column = "pageaddress")
    public String pageAddress;

    @Column(column = "tagtime")
    public String tagtime;

    /// <summary>
    /// 一个笔画时间
    /// </summary>
    @Column(column = "stroketime")
    public int strokeTime;

    /// <summary>
    /// x坐标
    /// </summary>
    @Column(column = "x")
    public int x;
    /// <summary>
    /// y坐标
    /// </summary>
    @Column(column = "y")
    public int y;

    @Transient
    public ArrayList<Point> dList = new ArrayList<Point>();


    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

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

    public String getItemid() {
        return itemid;
    }

    public void setItemid(String itemid) {
        this.itemid = itemid;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Boolean getIsupload() {
        return isupload;
    }

    public void setIsupload(Boolean isupload) {
        this.isupload = isupload;
    }

    public String getPageAddress() {
        return pageAddress;
    }

    public void setPageAddress(String pageAddress) {
        this.pageAddress = pageAddress;
    }

    public String getTagtime() {
        return tagtime;
    }

    public void setTagtime(String tagtime) {
        this.tagtime = tagtime;
    }

    public int getStrokeTime() {
        return strokeTime;
    }

    public void setStrokeTime(int strokeTime) {
        this.strokeTime = strokeTime;
    }
}
