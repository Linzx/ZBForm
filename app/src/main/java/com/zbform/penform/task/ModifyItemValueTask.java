package com.zbform.penform.task;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zbform.penform.json.ModifyPostParams;
import com.zbform.penform.json.ModifyResultInfo;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.net.IZBformNetBeanCallBack;
import com.zbform.penform.net.ZBformNetBean;

public class ModifyItemValueTask implements IZBformNetBeanCallBack {
    private static final String TAG = "ModifyItemValueTask";
    private Context mContext;
    private String mFormId;
    private String mRecordId;
    private String mItemCode;
    private String mItemData;

    private OnModifyTaskListener mOnModifyTaskListener;

    public interface OnModifyTaskListener {
        void onStartGet();

        void onGetSuccess(ModifyPostParams info);

        void onGetFail();

        void onCancelled();
    }

    public ModifyItemValueTask(Context context, String formId, String recordId, String itemCode, String itemValue) {
        mContext = context;
        mFormId = formId;
        mRecordId = recordId;
        mItemCode = itemCode;
        mItemData = itemValue;
    }

    public void execute() {
        String modifyUri = ApiAddress.MODIFY_ITEMVALUE_URL;
        ZBformNetBean modifyTask = new ZBformNetBean(mContext, modifyUri,
                HttpRequest.HttpMethod.POST);
        modifyTask.setNetTaskCallBack(this);

        RequestParams params = new RequestParams();
        params.addHeader("Content-Type", "application/json;Charset=UTF-8");

        params.addQueryStringParameter("formUuid", mFormId);
        params.addQueryStringParameter("recordUuid", mRecordId);
        params.addQueryStringParameter("itemData", mItemData);
        params.addQueryStringParameter("itemCode", mItemCode);

        modifyTask.execute(params);
    }

    public void setOnFormTaskListener(OnModifyTaskListener listener) {
        mOnModifyTaskListener = listener;
    }

    @Override
    public void onStart() {
        Log.i(TAG, "onStart");

        if (mOnModifyTaskListener != null) {
            mOnModifyTaskListener.onStartGet();
        }
    }

    @Override
    public void onCancelled() {
        Log.i(TAG, "onCancelled");

        if (mOnModifyTaskListener != null) {
            mOnModifyTaskListener.onCancelled();
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
            ModifyResultInfo result = gson.fromJson(resultGson, new TypeToken<ModifyResultInfo>() {
            }.getType());


            Log.i(TAG, "result errcode = " + result.getResponseCode() + "  msg: " + result.getResponseMsg());
            if (result.getResponseCode() == 0) {

                ModifyPostParams modifyPostParams = new ModifyPostParams(mFormId, mRecordId, mItemCode, mItemData);
                if (mOnModifyTaskListener != null) {
                    mOnModifyTaskListener.onGetSuccess(modifyPostParams);
                }

            } else {
                if (mOnModifyTaskListener != null) {
                    mOnModifyTaskListener.onGetFail();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "e=" + e.getMessage());

            if (mOnModifyTaskListener != null) {
                mOnModifyTaskListener.onGetFail();
            }
        }
    }

    @Override
    public void onFailure(HttpException error, String msg) {
        Log.i(TAG, "onFailure" + "error: " + error.toString());
        Log.i(TAG, "msg: " + msg);

        if (mOnModifyTaskListener != null) {
            mOnModifyTaskListener.onGetFail();
        }
    }
}
