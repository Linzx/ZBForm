package com.zbform.penform.fragment;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.util.Log;

import com.zbform.penform.R;
import com.zbform.penform.util.PreferencesUtility;

public class FormSettingFragment extends PreferenceFragment {

    private static final String KEY_FORM_DEF_NEW_KEY = "form_def_new_key";
    private static final String KEY_RECORD_LAST_OPEN_KEY = "record_last_open_key";
    private SwitchPreference mFormDefNewPref;
    private SwitchPreference mRecordLastPref;
    private PreferencesUtility mPreferencesUtility;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_form_setting_fragment);
        mPreferencesUtility = PreferencesUtility.getInstance(this.getActivity());
        mFormDefNewPref = (SwitchPreference)findPreference(KEY_FORM_DEF_NEW_KEY);
        mRecordLastPref = (SwitchPreference)findPreference(KEY_RECORD_LAST_OPEN_KEY);

        mFormDefNewPref.setChecked(mPreferencesUtility.getPreFormDefNew());
        mRecordLastPref.setChecked(mPreferencesUtility.getPreRecordLast());
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        Log.i("whd","pre click");

        if(mFormDefNewPref == preference){
            mPreferencesUtility.setPreFormDefNew(mFormDefNewPref.isChecked());
        } else if(mRecordLastPref == preference){
            mPreferencesUtility.setPreRecordLast(mRecordLastPref.isChecked());
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}