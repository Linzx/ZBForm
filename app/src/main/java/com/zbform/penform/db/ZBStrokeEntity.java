package com.zbform.penform.db;


import com.google.gson.annotations.Expose;
import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Finder;
import com.lidroid.xutils.db.annotation.Table;
import com.lidroid.xutils.db.annotation.Transient;
import com.lidroid.xutils.db.sqlite.FinderLazyLoader;

@Table(name = "zbstroke")
public class ZBStrokeEntity extends EntityBase{
    /// <summary>
    /// 用户ID
    /// </summary>
    @Expose(serialize = false, deserialize = false)
    @Column(column = "userid") // 建议加上注解， 混淆后列名不受影响
    public String userid;

    /// <summary>
    /// 表单ID
    /// </summary>
    @Expose(serialize = false, deserialize = false)
    @Column(column = "formid")
    public String formid;

    /// <summary>
    /// 书写记录ID
    /// </summary>
    @Expose(serialize = false, deserialize = false)
    @Column(column = "recordid")
    public String recordid;

    /// <summary>
    /// 表单内区域项id
    /// </summary>
    @Expose(serialize = false, deserialize = false)
    @Column(column = "itemid")
    public String itemid;

    @Expose(serialize = false, deserialize = false)
    @Column(column = "isupload")
    public Boolean isupload;

    /// <summary>
    /// 一个笔画开始时间yyyyMMdd HH:mm:ss.SSS
    /// </summary>
    @Expose(serialize = false, deserialize = false)
    @Column(column = "beginTime")
    public long beginTime;

    /// <summary>
    /// 一个笔画结束时间
    /// </summary>
    @Expose(serialize = false, deserialize = false)
    @Column(column = "endTime")
    public long endTime;

    @Expose(serialize = false, deserialize = false)
    @Finder(valueColumn = "id", targetColumn = "parentId")
    public FinderLazyLoader<ZBStrokePointEntity> children; // 关联对象多时建议使用这种方式，延迟加载效率较高。

    /// <summary>
    /// 时间戳 yyyyMMdd HH:mm:ss.fff
    /// </summary>
    @Transient
    public String t;
    /// <summary>
    /// 笔画耗时
    /// </summary>
    @Column(column = "timedraw")
    public int c;
    /// <summary>
    /// 点阵地址
    /// </summary>
    @Column(column = "pageaddress")
    public String p;

    /// <summary>
    /// 笔画点集合
    /// </summary>
    @Transient
    public ZBStrokePointEntity[] d;


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

    public Boolean getIsupload() {
        return isupload;
    }

    public void setIsupload(Boolean isupload) {
        this.isupload = isupload;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }
}
