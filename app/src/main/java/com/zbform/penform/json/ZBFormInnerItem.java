package com.zbform.penform.json;

import com.zbform.penform.db.ZBStrokeEntity;

/// <summary>
/// 等待上传笔迹数据
/// </summary>
public class ZBFormInnerItem {

    /// <summary>
    /// 用户ID
    /// </summary>
    public String userid;
    /// <summary>
    /// 表单ID
    /// </summary>
    public String formid;
    /// <summary>
    /// 书写记录ID hw.uuid
    /// </summary>
    public String id;
    /// <summary>
    /// 表单内区域项id
    /// </summary>
    public String itemid;

    /// <summary>
    /// 笔迹数据数组
    /// </summary>
    public ZBStrokeEntity[] data;

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
}
