
package com.zbform.penform.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public final class PreferencesUtility {

    public static final String SHARED_FIRST_USE = "init";
    public static final String SHARED_USERID_KEY = "user_id";
    public static final String SHARED_USER_PWD_KEY = "user_pwd";
    public static final String SHARED_LAST_LOGIN_KEY = "last_login";
    public static final String SHARED_LAST_APK_VERSION_KEY = "last_apk_version";
    private static final String PRE_INTRODUCE_SHOWED = "show_introduce";

    private static PreferencesUtility sInstance;

    private static SharedPreferences mPreferences;

    public PreferencesUtility(final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static final PreferencesUtility getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PreferencesUtility(context.getApplicationContext());
        }
        return sInstance;
    }

    public void setOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public long lastExit(){
        return mPreferences.getLong("last_err_exit", 0);
    }

    public void setExitTime(){
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong("last_err_exit", System.currentTimeMillis());
        editor.commit();
    }

    public void setUserID(String id) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(SHARED_USERID_KEY, id);
        editor.apply();
    }

    public void setPassword(String pwd) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(SHARED_USER_PWD_KEY, pwd);
        editor.apply();
    }

    public String getUserID() {
        return mPreferences.getString(SHARED_USERID_KEY,"");
    }

    public String getPassword() {
        return mPreferences.getString(SHARED_USER_PWD_KEY, "");
    }

    public void setShowIntroduce(boolean show) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PRE_INTRODUCE_SHOWED, show);
        editor.apply();
    }

    public boolean getShowIntroduce() {
        return mPreferences.getBoolean(PRE_INTRODUCE_SHOWED,false);
    }
}