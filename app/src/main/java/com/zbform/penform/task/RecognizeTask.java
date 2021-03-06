package com.zbform.penform.task;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zbform.penform.json.RecognizeItem;
import com.zbform.penform.json.RecognizeResultInfo;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.net.IZBformNetBeanCallBack;
import com.zbform.penform.net.ZBformNetBean;

public class RecognizeTask implements IZBformNetBeanCallBack {
    private static final String TAG = "RecognizeTask";
    private Context mContext;
    private String mFormId;
    private String mRecordId;
    private RecognizeItem[] mItems;

    private OnRecognizeTaskListener mOnRecognizeTaskListener;

    public interface OnRecognizeTaskListener {
        void onStartGet();

        void onGetSuccess(RecognizeResultInfo info);

        void onGetFail();

        void onCancelled();
    }

    public RecognizeTask(Context context, String formId, String recordId, RecognizeItem[] items) {
        mContext = context;
        mFormId = formId;
        mRecordId = recordId;
        mItems = items;
    }

    public void setItems(RecognizeItem[] mItems) {
        this.mItems = mItems;
    }

    public void execute() {
        String recognizeUri = ApiAddress.HWR_RECOGNIZE;
        ZBformNetBean recognizeTask = new ZBformNetBean(mContext, recognizeUri,
                HttpRequest.HttpMethod.POST);
        recognizeTask.setNetTaskCallBack(this);

        RequestParams params = new RequestParams();
        params.addHeader("Content-Type", "application/json;Charset=UTF-8");

        params.addBodyParameter("formid", mFormId);
        params.addBodyParameter("recordid", mRecordId);

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        String json = gson.toJson(mItems);
        Log.i(TAG, "post items =" + json);
        params.addBodyParameter("items", json);

        recognizeTask.execute(params);
    }

    public void setOnFormTaskListener(OnRecognizeTaskListener listener) {
        mOnRecognizeTaskListener = listener;
    }

    @Override
    public void onStart() {
        Log.i(TAG, "onStart");

        if (mOnRecognizeTaskListener != null) {
            mOnRecognizeTaskListener.onStartGet();
        }
    }

    @Override
    public void onCancelled() {
        Log.i(TAG, "onCancelled");

        if (mOnRecognizeTaskListener != null) {
            mOnRecognizeTaskListener.onCancelled();
        }
    }

    @Override
    public void onLoading(long total, long current, boolean isUploading) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSuccess(ResponseInfo<String> responseInfo) {
        Log.i(TAG, "onSuccess");

        try {
            String resultGson = responseInfo.result;
            Log.i(TAG, "result form Gson = " + resultGson);
            Gson gson = new Gson();
            RecognizeResultInfo result = gson.fromJson(resultGson, new TypeToken<RecognizeResultInfo>() {
            }.getType());


            Log.i(TAG, "result errcode = " + result.getErrcode() + "  msg: "+result.getMsg());
            if (result.getErrcode() == 0) {

                if (mOnRecognizeTaskListener != null) {
                    mOnRecognizeTaskListener.onGetSuccess(result);
                }

            } else {
                if (mOnRecognizeTaskListener != null) {
                    mOnRecognizeTaskListener.onGetFail();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "e=" + e.getMessage());

            if (mOnRecognizeTaskListener != null) {
                mOnRecognizeTaskListener.onGetFail();
            }
        }
    }

    @Override
    public void onFailure(HttpException error, String msg) {
        Log.i(TAG, "onFailure" + "error: "+ error.toString());
        Log.i(TAG, "msg: " + msg);

        if (mOnRecognizeTaskListener != null) {
            mOnRecognizeTaskListener.onGetFail();
        }
    }
}
