package com.zbform.penform.activity;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.zbform.penform.R;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.db.FormSettingEntity;
import com.zbform.penform.settings.AppCompatPreferenceActivity;
import com.zbform.penform.util.CommonUtils;

public class FormSettingActivity extends AppCompatPreferenceActivity implements Preference.OnPreferenceChangeListener{

    private static final String TAG = "FormSettingActivity";

    private static final String KEY_PEN_DEFAULT = "pen_default_list";
    private static final String KEY_RECORD_COUNT = "record_count_list";

    private ListPreference mPenPreference = null;
    private ListPreference mRecordPreference = null;
    private String mFormID;
    private FormSettingEntity mSettingEntity;
    private CharSequence[] mEntityPen;
    private CharSequence[] mEntityRecord;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.xml.form_setting_activity);

        mFormID = getIntent().getStringExtra("formid");

        mPenPreference = (ListPreference)findPreference(KEY_PEN_DEFAULT);
        mRecordPreference = (ListPreference)findPreference(KEY_RECORD_COUNT);

        mPenPreference.setOnPreferenceChangeListener(this);
        mRecordPreference.setOnPreferenceChangeListener(this);

        mSettingEntity = CommonUtils.getFormSetting(mFormID);
        mEntityPen = mPenPreference.getEntries();
        mEntityRecord = mRecordPreference.getEntries();
        if (mSettingEntity == null) {
            mSettingEntity = new FormSettingEntity();
            Log.i(TAG, "new entity");
            mSettingEntity.setFormid(mFormID);
            mPenPreference.setValue("0");
            mPenPreference.setSummary(mEntityPen[0]);

            mRecordPreference.setValue("10");
            mRecordPreference.setSummary(getResources().getString(
                    R.string.form_setting_record_summary,
                    mEntityRecord[2]));
        } else {
            Log.i(TAG,"got entity");
            String type = String.valueOf(mSettingEntity.getOpentype());
            String recordCount = String.valueOf(mSettingEntity.getRecordcount());
            Log.i(TAG,"got entity type="+type);
            Log.i(TAG,"got entity recordCount="+recordCount);

            int indexType = 0;
            int indexRecord = 0;

            if (!TextUtils.isEmpty(type)){
                indexType = mPenPreference.findIndexOfValue(type);
            }
            if (!TextUtils.isEmpty(recordCount)){
                indexRecord = mRecordPreference.findIndexOfValue(recordCount);
            }

            mPenPreference.setValue(type);
            mRecordPreference.setValue(recordCount);

            mPenPreference.setSummary(mEntityPen[indexType]);

            mRecordPreference.setSummary(getResources().getString(
                    R.string.form_setting_record_summary,
                    mEntityRecord[indexRecord]));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPenPreference) {
            mPenPreference.setValue(String.valueOf(newValue));
            mSettingEntity.setOpentype(Integer.valueOf((String)newValue));
            mPenPreference.setSummary(mEntityPen[mPenPreference.
                    findIndexOfValue(String.valueOf(newValue))]);

        } else if (preference == mRecordPreference) {
            mRecordPreference.setValue(String.valueOf(newValue));

            mSettingEntity.setRecordcount(Integer.valueOf((String)newValue));

            mRecordPreference.setSummary(getResources().getString(
                    R.string.form_setting_record_summary,
                    mEntityRecord[mRecordPreference.findIndexOfValue(
                            String.valueOf(newValue))]));
        }

        FormSettingEntity entity = CommonUtils.getFormSetting(mFormID);
        try {
            if (entity == null) {
                ZBformApplication.mDB.saveBindingId(mSettingEntity);
            } else {
                WhereBuilder whereBuilder = WhereBuilder.b();
                whereBuilder.and("formid", "=", mSettingEntity.getFormid());
                try {
                    ZBformApplication.mDB.update(mSettingEntity, whereBuilder);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG, "newValue=" + newValue);
        } catch (DbException e) {
            e.printStackTrace();
        }
        return false;
    }
}
