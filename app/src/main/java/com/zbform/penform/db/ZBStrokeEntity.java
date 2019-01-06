package com.zbform.penform.db;


import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

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

    public ZBStrokeEntity parent;
    @Column(column = "isupload")
    public Boolean isupload;

    /// <summary>
    /// 一个笔画开始时间yyyyMMdd HH:mm:ss.SSS
    /// </summary>
    @Column(column = "beginTime")
    public long beginTime;

    /// <summary>
    /// 一个笔画结束时间
    /// </summary>
    @Column(column = "endTime")
    public long endTime;

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
}
