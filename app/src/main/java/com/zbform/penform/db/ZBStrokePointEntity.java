package com.zbform.penform.db;

import com.google.gson.annotations.Expose;
import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Foreign;
import com.lidroid.xutils.db.annotation.Table;

/// <summary>
/// 点阵坐标点
/// </summary>
@Table(name = "zbstrokepoint")
public class ZBStrokePointEntity extends EntityBase {
    @Expose(serialize = false, deserialize = false)
    @Foreign(column = "parentId", foreign = "id")
    public ZBStrokeEntity parent;

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
}
