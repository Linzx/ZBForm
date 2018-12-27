package com.zbform.penform.activity;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.zbform.penform.R;
import com.zbform.penform.fragment.AccountFragment;
import com.zbform.penform.fragment.PenFragment;
import com.zbform.penform.settings.AppCompatPreferenceActivity;

import java.util.List;

public class SettingActivity extends AppCompatPreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || AccountFragment.class.getName().equals(fragmentName)
                || PenFragment.class.getName().equals(fragmentName);
    }
}
