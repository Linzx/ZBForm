package com.zbform.penform.task;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.json.RecordInfo;
import com.zbform.penform.json.RecordListInfo;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.net.ErrorCode;
import com.zbform.penform.net.IZBformNetBeanCallBack;
import com.zbform.penform.net.ZBformNetBean;

import java.util.Arrays;
import java.util.List;

public class RecordTask implements IZBformNetBeanCallBack {
    private static final String TAG = RecordTask.class.getSimpleName();

    private String mUserId;
    private String mUserKeyStr;
    private String mFormId;
    private String mRecordId;

    private Context mContext;
    private OnTaskListener mOnTaskListener;


    public interface OnTaskListener {
        void onTaskStart();

        void onTaskSuccess(List<RecordInfo.Results> results);

        void onTaskFail();
    }

    public RecordTask(Context context, String formId, String recordId){
        mUserId = ZBformApplication.getmLoginUserId();
        mUserKeyStr = ZBformApplication.getmLoginUserKey();
        mContext = context;
        mFormId = "74fef293-e8c1-4c8a-ba3f-25321d1eabaf";
        mRecordId = recordId;
    }

    public void getRecord() {
        Log.i(TAG, "[getRecord] begin");
        String getRecordUrl = ApiAddress.getRecordUri(mUserId, mUserKeyStr, mFormId, mRecordId);
        ZBformNetBean getRecordTask = new ZBformNetBean(mContext, getRecordUrl,
                HttpRequest.HttpMethod.GET);
        getRecordTask.setNetTaskCallBack(this);
        getRecordTask.execute();
        Log.i(TAG, "[getRecord] end");

    }


    public void setTaskListener(OnTaskListener listener) {
        mOnTaskListener = listener;
    }

    @Override
    public void onStart() {
        if (mOnTaskListener != null) {
            mOnTaskListener.onTaskStart();
        }
    }

    @Override
    public void onCancelled() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLoading(long total, long current, boolean isUploading) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSuccess(ResponseInfo<String> responseInfo) {
        try {
            String resultGson = responseInfo.result;
            Log.i(TAG, "result json =" + resultGson);
            Gson gson = new Gson();
            RecordInfo record = gson.fromJson(resultGson, new TypeToken<RecordInfo>(){}.getType());

            if (record != null && record.header != null
                    && record.results != null && record.results.length > 0) {

                Log.i(TAG, "error code =" + record.header.getErrorCode());
                Log.i(TAG, "results count: "+ record.header.getCount());
                if (record.header.getErrorCode().equals(ErrorCode.RESULT_OK)) {
                    if (mOnTaskListener != null) {
                        mOnTaskListener.onTaskSuccess(Arrays.asList(record.results));
                    }

                } else {
                    if (mOnTaskListener != null) {
                        mOnTaskListener.onTaskFail();
                    }
                }
            } else {
                if (mOnTaskListener != null) {
                    mOnTaskListener.onTaskFail();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "e=" + e.getMessage());
            if (mOnTaskListener != null) {
                mOnTaskListener.onTaskFail();
            }
        }
    }

    @Override
    public void onFailure(HttpException error, String msg) {
        if (mOnTaskListener != null) {
            mOnTaskListener.onTaskFail();
        }
    }
}
