package com.zbform.penform;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.util.Log;


import com.google.gson.Gson;
import com.tstudy.blepenlib.BlePenManager;
import com.zbform.penform.blepen.MyLicense;
import com.zbform.penform.blepen.ZBFormBlePenManager;
import com.zbform.penform.handler.UnceHandler;
import com.zbform.penform.json.UserInfo;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.permissions.Nammu;
//import com.zbform.penform.provider.PlaylistInfo;
import com.zbform.penform.util.IConstants;
import com.zbform.penform.util.PreferencesUtility;
import com.zbform.penform.util.ThemeHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 */
public class ZBformApplication extends Application {

    /**
     * mUser: 记录登录用户账号信息
     * */
    public static UserInfo.Results mUser;
    private static String mLoginUserId;
    private static String mLoginUserKey;
    public static Context context;

    //捕获全局Exception 重启界面
    public void initCatchException() {
        //设置该CrashHandler为程序的默认处理器
        UnceHandler catchExcep = new UnceHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(catchExcep);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;

        BlePenManager.getInstance().init(this,MyLicense.getBytes());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Nammu.init(this);
        }
        // refWatcher = LeakCanary.install(this);
        //       LeakCanary.install(this);
        initCatchException();
    }

    public static String getmLoginUserId() {
        return mLoginUserId;
    }

    public static void setmLoginUserId(String id) {
        mLoginUserId = id;
    }

    public static String getmLoginUserKey() {
        return mLoginUserKey;
    }

    public static void setmLoginUserKey(String key) {
        mLoginUserKey = key;
    }

    public static void setUser(UserInfo.Results mUser) {
        ZBformApplication.mUser = mUser;
    }
}