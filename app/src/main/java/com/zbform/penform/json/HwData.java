package com.zbform.penform.json;

import java.util.Arrays;

public class HwData {
    String t;  // 时间戳
    int c;     // 用时
    String p;  // 点阵地址
    //String s;  // 笔序列号
    Point[] d; // 笔画点阵集合

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
