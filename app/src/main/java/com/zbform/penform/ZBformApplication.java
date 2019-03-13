package com.zbform.penform;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;


import com.lidroid.xutils.DbUtils;
import com.tstudy.blepenlib.BlePenManager;
import com.tstudy.blepenlib.BlePenStreamManager;
import com.zbform.penform.blepen.MyLicense;
import com.zbform.penform.blepen.MyLicenseDemo;
import com.zbform.penform.blepen.ZBFormBlePenManager;
import com.zbform.penform.handler.UnceHandler;
import com.zbform.penform.json.UserInfo;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.permissions.Nammu;
import com.zbform.penform.services.BleConnectService;

/**
 */
public class ZBformApplication extends Application {

    public static final String TAG = "ZBform";

    /**
     * mUser: 记录登录用户账号信息
     */
    public static UserInfo.Results mUser;
    private static String mLoginUserName;
    private static String mLoginUserId;
    private static String mLoginUserKey;
    public static Context context;
    public static DbUtils mDB;
    /**
     * mZBFormBlePenManager 全局唯一
     */
    public static ZBFormBlePenManager sBlePenManager;

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

        initZBFormBlePenManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Nammu.init(this);
        }
        // refWatcher = LeakCanary.install(this);
        //       LeakCanary.install(this);
//        initCatchException();

        mDB = DbUtils.create(this, "zbform.db");
        mDB.configAllowTransaction(true);
        mDB.configDebug(true);
    }

    private void initZBFormBlePenManager() {
        sBlePenManager = ZBFormBlePenManager.getInstance(context);
        boolean initSuccess;
        if (ApiAddress.DEBUG){
            initSuccess = BlePenManager.getInstance().init(this, MyLicenseDemo.getBytes());
        } else {
            initSuccess = BlePenManager.getInstance().init(this, MyLicense.getBytes());
        }
        Log.i(TAG, "ble init success = " + initSuccess);
        sBlePenManager.setBleInitSuccess(initSuccess);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.i(TAG, "onTerminate, disconnect all device.");
        BlePenManager.getInstance().disconnectAllDevice();
    }

    public static String getmLoginUserId() {
        return mLoginUserId;
    }

    public static String getLoginUserName() {
        return mLoginUserName;
    }

    public static String setLoginUserName(String name) {
        return mLoginUserName = name;
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