package com.zbform.penform.task;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.json.RecordListInfo;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.net.ErrorCode;
import com.zbform.penform.net.IZBformNetBeanCallBack;
import com.zbform.penform.net.ZBformNetBean;

import java.util.Arrays;
import java.util.List;

public class RecordListTask implements IZBformNetBeanCallBack {
    private static final String TAG = RecordListTask.class.getSimpleName();

    private String mUserId;
    private String mUserKeyStr;
    private String mFormId;

    private Context mContext;
    private OnTaskListener mOnTaskListener;


    public interface OnTaskListener {
        public void onTaskStart();

        public void onTaskSuccess(List<RecordListInfo.Results> results);

        public void onTaskFail();
    }

    public RecordListTask(Context context, String formId){
        mUserId = ZBformApplication.getmLoginUserId();
        mUserKeyStr = ZBformApplication.getmLoginUserKey();
        mContext = context;
        mFormId = "8341b2a9-7b72-4059-9822-ae77e6580c46";
    }

    public void getRecordList() {
        Log.i(TAG, "[getRecordList] begin");
        String getFormListUrl = ApiAddress.getRecordListUri(mUserId, mUserKeyStr, mFormId);
        ZBformNetBean getFormListTask = new ZBformNetBean(mContext, getFormListUrl,
                HttpRequest.HttpMethod.GET);
        getFormListTask.setNetTaskCallBack(this);
        getFormListTask.execute();
        Log.i(TAG, "[getRecordList] end");

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
            Log.i(TAG, "resultGson!!=" + resultGson);
            Gson gson = new Gson();
            RecordListInfo recordList = gson.fromJson(resultGson, new TypeToken<RecordListInfo>(){}.getType());

            if (recordList != null && recordList.header != null
                    && recordList.results != null && recordList.results.length > 0) {

                Log.i(TAG, "errorcode =" + recordList.header.getErrorCode());
                Log.i(TAG, "results conunt: "+ recordList.header.getCount());
                if (recordList.header.getErrorCode().equals(ErrorCode.RESULT_OK)) {
                    for(RecordListInfo.Results r: recordList.results) {
                        Log.i(TAG, r.toString());
                    }
                    if (mOnTaskListener != null) {
                        mOnTaskListener.onTaskSuccess(Arrays.asList(recordList.results));
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
