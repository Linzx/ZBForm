package com.zbform.penform.settings;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class SettingPreferenceCategory extends PreferenceCategory {

    public SettingPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SettingPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SettingPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingPreferenceCategory(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        if(view instanceof TextView){
            TextView tv = (TextView) view;
            tv.setTextSize(18);
            tv.setAllCaps(false);
        }
    }
}
