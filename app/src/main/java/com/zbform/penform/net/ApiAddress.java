package com.zbform.penform.net;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.zbform.penform.ZBformApplication;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ApiAddress {
    public final static String TAG = "apiaddress";

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
    /// 获取表单的图片
    /// </summary>
    public static String Form_Get_Img = DownLoadPdfUrlBase;

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

        StringBuilder sb = new StringBuilder();
        sb.append(ApiAddress.UserLogin_Get);
        sb.append("?signcode=");
        sb.append(Uri.encode(signcode));
        sb.append("&timestamp=");
        sb.append(Uri.encode(getTimeStamp()));
        sb.append("&userid=");
        sb.append(Uri.encode(userid));
        sb.append("&password=");
        sb.append(Uri.encode(password));

        Log.i("whd", "get login url=" + sb.toString());
        return sb.toString();
    }

    public static String getTimeStamp(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");// HH:mm:ss
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    public static String getNewRecordUri(String userid, String userkey, String formid) {
        String signCode = getSignCode(userid+userkey+SYSTEM_KEY);

        StringBuilder sb = new StringBuilder();
        sb.append(ApiAddress.Hw_Create);
        sb.append("?signcode=");
        sb.append(Uri.encode(signCode));
        sb.append("&timestamp=");
        sb.append(Uri.encode(getTimeStamp()));
        sb.append("&userid=");
        sb.append(Uri.encode(userid));
        sb.append("&formid=");
        sb.append(Uri.encode(formid));

        Log.i(TAG, "get form img url=" + sb.toString());
        return sb.toString();
    }

    public static String getFormImgUri(String userid, String userkey, String uuid, int page) {
        String signCode = getSignCode(userid+userkey+SYSTEM_KEY);

        StringBuilder sb = new StringBuilder();
        sb.append(ApiAddress.Form_Get_Img);
        sb.append("?signcode=");
        sb.append(Uri.encode(signCode));
        sb.append("&timestamp=");
        sb.append(Uri.encode(getTimeStamp()));
        sb.append("&uuid=");
        sb.append(Uri.encode(uuid));
        sb.append("&page=");
        sb.append(Uri.encode(String.valueOf(page)));

        Log.i(TAG, "get form img url=" + sb.toString());
        return sb.toString();
    }

    public static String getFormListUri(String userid, String userkey) {
        String signCode = getSignCode(userid+userkey+SYSTEM_KEY);

        StringBuilder sb = new StringBuilder();
        sb.append(ApiAddress.Form_List);
        sb.append("?signcode=");
        sb.append(Uri.encode(signCode));
        sb.append("&timestamp=");
        sb.append(Uri.encode(getTimeStamp()));
        sb.append("&userid=");
        sb.append(Uri.encode(userid));

        Log.i(TAG, "get form list url=" + sb.toString());
        return sb.toString();
    }

    public static String getFormUri(String userid, String userkey, String formId) {
        String signCode = getSignCode(userid+userkey+SYSTEM_KEY);

        StringBuilder sb = new StringBuilder();
        sb.append(ApiAddress.Form_Get);
        sb.append("?signcode=");
        sb.append(Uri.encode(signCode));
        sb.append("&timestamp=");
        sb.append(Uri.encode(getTimeStamp()));
        sb.append("&userid=");
        sb.append(Uri.encode(userid));
        sb.append("&id=");
        sb.append(Uri.encode(formId));

        Log.i(TAG, "get form url=" + sb.toString());
        return sb.toString();
    }

    public static String getRecordListUri(String userid, String userkey, String formId) {
        String signCode = getSignCode(userid+userkey+SYSTEM_KEY);

        StringBuilder sb = new StringBuilder();
        sb.append(ApiAddress.Hw_List);
        sb.append("?signcode=");
        sb.append(Uri.encode(signCode));
        sb.append("&timestamp=");
        sb.append(Uri.encode(getTimeStamp()));
        sb.append("&userid=");
        sb.append(Uri.encode(userid));
        sb.append("&formid=");
        sb.append(Uri.encode(formId));

        Log.i(TAG, "get record list url=" + sb.toString());
        return sb.toString();
    }

    public static String getRecordUri(String userid, String userkey, String formId, String hwId)
    {
        String signCode = getSignCode(userid+userkey+SYSTEM_KEY);

        StringBuilder sb = new StringBuilder();
        sb.append(ApiAddress.Hw_Get);
        sb.append("?signcode=");
        sb.append(Uri.encode(signCode));
        sb.append("&timestamp=");
        sb.append(Uri.encode(getTimeStamp()));
        sb.append("&userid=");
        sb.append(Uri.encode(userid));
        sb.append("&formid=");
        sb.append(Uri.encode(formId));
        sb.append("&id=");
        sb.append(Uri.encode(hwId));

        Log.i(TAG, "get record url=" + sb.toString());
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
