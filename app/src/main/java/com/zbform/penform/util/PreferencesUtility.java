
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

    public static final String PRE_FORM_DEF_NEW = "from_def_new";
    public static final String PRE_RECORD_LAST = "record_last_open";

    public static final String BLEPEN_NAME = "ble_pen_name";
    public static final String BLEPEN_MAC = "ble_pen_mac";


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

    public boolean getPreFormDefNew() {
        return mPreferences.getBoolean(PRE_FORM_DEF_NEW,false);
    }

    public void setPreFormDefNew(boolean check) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PRE_FORM_DEF_NEW, check);
        editor.apply();
    }

    public boolean getPreRecordLast() {
        return mPreferences.getBoolean(PRE_RECORD_LAST,false);
    }

    public void setPreRecordLast(boolean check) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PRE_RECORD_LAST, check);
        editor.apply();
    }

    public boolean getShowIntroduce() {
        return mPreferences.getBoolean(PRE_INTRODUCE_SHOWED,false);
    }

    public void setPreferenceValue(String key, String value){
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getPreferenceValue(String key, String defaultValue){
        return mPreferences.getString(key,defaultValue);
    }

}