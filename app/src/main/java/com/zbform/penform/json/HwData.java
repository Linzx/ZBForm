package com.zbform.penform.json;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Arrays;

public class HwData {
    @Expose(serialize = true, deserialize = true)
    String t;  // 时间戳
    @Expose(serialize = true, deserialize = true)
    int c;     // 用时
    @Expose(serialize = true, deserialize = true)
    String p;  // 点阵地址
    //String s;  // 笔序列号
    @Expose(serialize = true, deserialize = true)
    Point[] d; // 笔画点阵集合

    @Expose(serialize = false, deserialize = false)
    public ArrayList<Point> dList = new ArrayList<Point>();

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

    public Point[] getD() {
        return d;
    }

    public void setD(Point[] d) {
        this.d = d;
    }

    @Override
    public String toString() {
        return "HwData{" +
                "t='" + t + '\'' +
                ", c=" + c +
                ", p='" + p + '\'' +
                ", d=" + Arrays.toString(d) +
                '}';
    }
}
