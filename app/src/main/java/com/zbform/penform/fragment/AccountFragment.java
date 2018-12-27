package com.zbform.penform.fragment;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ListView;

import com.zbform.penform.R;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.settings.SettingPreferenceCategory;

public class AccountFragment extends PreferenceFragment {

    SettingPreferenceCategory userCodePreference;
    Preference groupPreference;
    Preference namePreference;
    Preference emailPreference;
    Preference phonePreference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_account_fragment);

        if(ZBformApplication.mUser != null) {
            setPreferenceValues();
        }

//        ListView list = getView().findViewById(android.R.id.list);
//        list.setDivider(null);
    }

    private void setPreferenceValues() {
        String userCode = ZBformApplication.mUser.getUserCode();
        String group = ZBformApplication.mUser.getGroup();
        String name = ZBformApplication.mUser.getName();
        String email = ZBformApplication.mUser.getEmail();
        String phone = ZBformApplication.mUser.getPhone();

        userCodePreference = (SettingPreferenceCategory)findPreference(getString(R.string.setting_pref_category_key));
        groupPreference = findPreference(getString(R.string.setting_pref_group_key));
        namePreference = findPreference(getString(R.string.setting_pref_username_key));
        phonePreference = findPreference(getString(R.string.setting_pref_phone_key));
        emailPreference = findPreference(getString(R.string.setting_pref_email_key));

        userCodePreference.setTitle(userCode);
        if(!TextUtils.isEmpty(group)) {
            groupPreference.setSummary(group);
        } else {
            getPreferenceScreen().removePreference(groupPreference);
        }
        if(!TextUtils.isEmpty(name)) {
            namePreference.setSummary(name);
        } else {
            getPreferenceScreen().removePreference(namePreference);
        }
        if(!TextUtils.isEmpty(email)) {
            emailPreference.setSummary(email);
        } else {
            getPreferenceScreen().removePreference(emailPreference);

        }
        if(!TextUtils.isEmpty(phone)) {
            phonePreference.setSummary(phone);
        } else {
            getPreferenceScreen().removePreference(phonePreference);
        }

    }
}