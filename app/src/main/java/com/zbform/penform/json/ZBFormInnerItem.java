package com.zbform.penform.json;

import com.google.gson.annotations.Expose;
import com.zbform.penform.db.ZBStrokeEntity;

import java.util.ArrayList;

/// <summary>
/// 等待上传笔迹数据
/// </summary>
public class ZBFormInnerItem {

    /// <summary>
    /// 用户ID
    /// </summary>
    @Expose(serialize = true, deserialize = true)
    public String userid;
    /// <summary>
    /// 表单ID
    /// </summary>
    @Expose(serialize = true, deserialize = true)
    public String formid;
    /// <summary>
    /// 书写记录ID hw.uuid
    /// </summary>
    @Expose(serialize = true, deserialize = true)
    public String id;
    /// <summary>
    /// 表单内区域项id
    /// </summary>
    @Expose(serialize = true, deserialize = true)
    public String itemid;

    @Expose(serialize = true, deserialize = true)
    public String penSid;

    @Expose(serialize = true, deserialize = true)
    public String penMac;

    /// <summary>
    /// 笔迹数据数组
    /// </summary>
    @Expose(serialize = true, deserialize = true)
    public HwData[] data;

    @Expose(serialize = false, deserialize = false)
    public ArrayList<HwData> dataList = new ArrayList<HwData>();

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemid() {
        return itemid;
    }

    public void setItemid(String itemid) {
        this.itemid = itemid;
    }

    public String getPenSid() {
        return penSid;
    }

    public void setPenSid(String penSid) {
        this.penSid = penSid;
    }

    public String getPenMac() {
        return penMac;
    }

    public void setPenMac(String penMac) {
        this.penMac = penMac;
    }

    public HwData[] getData() {
        return data;
    }

    public void setData(HwData[] data) {
        this.data = data;
    }
}
