package com.zbform.penform.blepen;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

public class StreamingController {

    final String TAG = "StreamingController";

    boolean m_penStatus = false;
    boolean m_firstFlag = false;
    boolean m_bPenDown = false;

    float m_scaleX;
    float m_scaleY;

    float m_x, m_y;
    int m_force;
    String m_page;

    int m_event = -1;
    String m_pageaddress = "";
    String m_battery = "";
    String m_memory = "";

    long m_CoordinateCnt = 0;
    long m_DrawCoordinateCnt = 0;
    float StrokeWidth = 1f;

    int lastLogX = 0;
    int lastLogY = 0;

    Paint m_paint;
    Path m_path = new Path();

    // 屏幕的宽高像素/纸张的宽高像素
    public StreamingController(int width, int height) {
        if (width < 1000) {
            StrokeWidth = 0.5f;
        }
        if (width > height) {
            m_scaleX = ((float) (width) / 7920f);
            m_scaleY = ((float) (height) / 5600f);
        } else {
            m_scaleX = ((float) (width) / 5600f);
            m_scaleY = ((float) (height) / 7920f);
        }
//        m_paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
//        m_paint.setAntiAlias(true);
//        m_paint.setStyle(Paint.Style.STROKE);
//        m_paint.setStrokeJoin(Paint.Join.ROUND);
//        m_paint.setStrokeCap(Paint.Cap.ROUND);
//        m_paint.setStrokeWidth(2);
//        m_paint.setPathEffect(new CornerPathEffect(90));
//        m_paint.setDither(true);
//        m_paint.setLinearText(true);
//        m_paint.setSubpixelText(true);
//        m_paint.setColor(Color.BLACK);
//        m_paint.setFilterBitmap(true);

        //初始化 画笔
        m_paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        m_paint.setStrokeWidth(StrokeWidth);
        m_paint.setColor(Color.BLACK);
        m_paint.setStyle(Paint.Style.STROKE);
        m_paint.setStrokeJoin(Paint.Join.ROUND);
        m_paint.setStrokeCap(Paint.Cap.ROUND);
        m_paint.setDither(true);
        m_paint.setAntiAlias(true);
        m_paint.setFilterBitmap(true);

    }


    public void penEvent(int event) {
        m_event = event;
    }

    //抬笔事件
    public void penUp() {

        m_bPenDown = false;
        m_firstFlag = false;
        m_path.reset();


    }

    public boolean getPenStatus() {
        return m_penStatus;
    }

    //落笔事件
    public void penDown() {
//        m_path.rewind();

        m_bPenDown = true;
        m_firstFlag = true;
        m_CoordinateCnt = 0;

    }

    //坐标事件 （坐标x/y，压力值，点阵地址）
    public void addCoordinate(int x, int y, int force, String pageAddress) {

        // Pen down
        if (!m_bPenDown) {
            return;
        }
        if (m_CoordinateCnt > 500) {
            m_CoordinateCnt = 0;
            penUp();
            penDown();
            addCoordinate(lastLogX, lastLogY, m_force, pageAddress);
            Log.d(TAG, "addCoordinate: 500");
        }
        if (m_firstFlag) {
            m_firstFlag = false;
            m_path.moveTo((x * m_scaleX), (y * m_scaleY));
            m_x = x;
            m_y = y;
            lastLogX = x;
            lastLogY = y;
            m_force = force;
        } else {
            m_path.cubicTo(m_x * m_scaleX, m_y * m_scaleY, (x + m_x) / 2 * m_scaleX, (y + m_y) / 2 * m_scaleY, x * m_scaleX, y * m_scaleY);

            m_x = x;
            m_y = y;

            m_paint.setStrokeWidth(StrokeWidth + (m_force + force) * 0.002f);
            m_force = force;

            lastLogX = x;
            lastLogY = y;

            m_CoordinateCnt++;

        }

    }

    String m_PenSerial = "";

    String m_PageInfo = "";


    public void setBattery(String battery) {
        m_battery = battery;
    }


    String m_sSoundStatus = "All Sound : , Sleep Sound : ";

    public void setSoundStatus(byte allSnd, byte sleepSnd) {
        String all = "";
        String sleep = "";

        if (allSnd == 0) {
            all = "Off";
        } else {
            all = "On";
        }
        if (sleepSnd == 0) {
            sleep = "Off";
        } else {
            sleep = "On";
        }

        m_sSoundStatus = "All Sound : " + all + ", Sleep Sound : " + sleep;
    }


    String m_Vid;
    String m_Pid;


    String m_swVer;


    public Paint getPaint() {
        return m_paint;
    }

    public Path getPath() {
        return m_path;
    }

    String m_PenInfo = null;

    public void setPenInfo(String penInfo) {
        m_PenInfo = penInfo;
    }

    public void clearCoordinateInfo() {
        m_CoordinateCnt = 0;
    }


    public String getStreamingInfo() {
        String m_str;

        m_str = "Pen ID : " + m_PenSerial + ", Pen S/W Version : " + m_swVer + "\n";
        m_str += "Pen Mode : " + ", Vid : " + m_Vid + ", Pid : " + m_Pid + "\n";

        m_str += "Sound Status :" + m_sSoundStatus + "\n";

        m_str += "Page Info : " + m_PageInfo + "\n";

        m_str += "Coordinate X : " + m_x + ", Y : " + m_y + ", Force : " + m_force + "\n";

        if (m_penStatus == true) {
            m_str += "Pen Status : Pen Down" + "\n";
        } else {
            m_str += "Pen Status : Pen Up" + "\n";
        }


        switch (m_event) {
            case 0:
                m_str += "EVENT : STATUS_NO_POSTION_DECODE_FAILED" + "\n";
                break;
            case 1:
                m_str += "EVENT : STATUS_NO_POSTION_LOCKED_SEGMENT" + "\n";
                break;
            case 2:
                m_str += "EVENT : STATUS_NO_POSTION_NON_ANOTO_PAPER" + "\n";
                break;
            case 3:
                m_str += "EVENT : STATUS_NO_POSTION_FRAME_SKIPPED" + "\n";
                break;
            case 4:
                m_str += "EVENT : STATUS_NO_POSTION_CAMERA_RESTARTED" + "\n";
                break;

            default:
                break;
        }
        m_event = -1;

        m_str += "Remained Battery : " + m_battery + ", Memory Fill Level : " + m_memory;

        return m_str;
    }

    public String getPageAddressInfo() {
        String m_str;

        m_str = "Page Address : " + m_pageaddress;

        return m_str;
    }

}



