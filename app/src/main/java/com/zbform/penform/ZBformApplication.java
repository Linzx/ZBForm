package com.zbform.penform;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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


    private static String mLoginUserGroupName;
    public static Context context;
    public static DbUtils mDB;
    /**
     * mZBFormBlePenManager 全局唯一
     */
    public static ZBFormBlePenManager sBlePenManager;

    public static int NOTIFICATION_ID = 1024;

    private int mActivityCount;

    public static boolean isForeground = true;

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
        isForeground = true;

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

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                mActivityCount++;
                if (mActivityCount > 0) {
                    isForeground = true;
                }
                Log.i("whd8", "mActivityCount="+mActivityCount);
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                mActivityCount--;
                Log.i("whd8", "mActivityCount="+mActivityCount);
                if (0 == mActivityCount) {
                    isForeground = false;
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
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

    public static String getLoginUserGroupName() {
        return mLoginUserGroupName;
    }

    public static void setLoginUserGroupName(String groupName) {
        ZBformApplication.mLoginUserGroupName = groupName;
    }
}