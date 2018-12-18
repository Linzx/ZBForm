package com.zbform.penform.net;

import android.net.Uri;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ApiAddress {
    private static boolean DEBUG = true;
    public static final String SYSTEM_KEY = "7A1285_788a0f";

    public static final String DownLoadPdfUrlBase = DEBUG == true ?
            "http://demo.zbform.com/zbform/get/getFormPDF/" :
            "http://www.zbform.com/zbform/get/getFormPDF/";
    private static final String BaseHttpUrl = DEBUG == true ?
            "http://demo.zbform.com:8080/zbform/api/" :
            "http://www.zbform.com:8080/zbform/api/";

    public static final String Hwitem_Websocket = DEBUG == true ?
            "ws://demo.zbform.com:8080/zbform/ws/hwitem.ws" :
            "ws://www.zbform.com:8080/zbform/ws/hwitem.ws";

    /// <summary>
    /// 笔迹上传websocket url
    /// </summary>
    //public static final String Hwitem_Websocket = "ws://www.zbform.com:8080/zbform/ws/hwitem.ws";
    //private static final String BaseHttpUrl = "http://www.zbform.com:8080/zbform/api/";
    //public static final String DownLoadPdfUrlBase = "http://www.zbform.com/zbform/get/getFormPDF/";
    /// <summary>
    /// 用户登录
    /// </summary>
    public static String UserLogin_Get = BaseHttpUrl + "userlogin/get";
    /// <summary>
    /// 表单列表
    /// </summary>
    public static String Form_List = BaseHttpUrl + "form/list";
    /// <summary>
    /// 获取单个表单
    /// </summary>
    public static String Form_Get = BaseHttpUrl + "form/get";
    /// <summary>
    /// 获取书写记录列表
    /// </summary>
    public static String Hw_List = BaseHttpUrl + "hw/list";
    /// <summary>
    /// 获取单个书写记录
    /// </summary>
    public static String Hw_Get = BaseHttpUrl + "hw/get";
    /// <summary>
    /// 创建书写记录
    /// </summary>
    public static String Hw_Create = BaseHttpUrl + "hw/create";
    /// <summary>
    /// 上传书写内容
    /// </summary>
    public static String Hwitem_put = BaseHttpUrl + "hwitem/put";

    public static String Hwitem_delete = BaseHttpUrl + "hwitem/delete";

    public static String getLoginUri(String userid, String password) {
        String signcode = getSignCode(userid + SYSTEM_KEY);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");// HH:mm:ss
        Date date = new Date(System.currentTimeMillis());
        String timestamp = simpleDateFormat.format(date);

        StringBuilder sb = new StringBuilder();
        sb.append(ApiAddress.UserLogin_Get);
        sb.append("?signcode=");
        sb.append(Uri.encode(signcode));
        sb.append("&timestamp=");
        sb.append(Uri.encode(timestamp));
        sb.append("&userid=");
        sb.append(Uri.encode(userid));
        sb.append("&password=");
        sb.append(Uri.encode(password));

        Log.i("whd", "url=" + sb.toString());
        return sb.toString();
    }

    public static String getSignCode(String keyword){
        try {
            return getSHA(getSHA(keyword));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getSHA(String val) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("SHA-1");
        md5.update(val.getBytes());
        byte[] m = md5.digest();//加密
        return byte2hex(m);
    }

    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs;
    }
}
