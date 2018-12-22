package com.zbform.penform.task;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zbform.penform.json.FormListInfo;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.net.ErrorCode;
import com.zbform.penform.net.IZBformNetBeanCallBack;
import com.zbform.penform.net.ZBformNetBean;

public class GetFormListTask implements IZBformNetBeanCallBack {
    private static final String TAG = "GetFormListTask";

    private String mUserId;
    private String mUserKeyStr;

    private Context mContext;
    private OnGetFormListListener mOnGetFormListListener;

    public interface OnGetFormListListener {
        public void onStart();

        public void onSuccess();

        public void onFail();
    }

    public void getFormList(Context context, String formListId, String userKeyStr) {
        mContext = context;
        mUserId = formListId;
        mUserKeyStr = "aaaaaa";
        String getFormListUrl = ApiAddress.getFormListUri(formListId, userKeyStr);
        ZBformNetBean getFormListTask = new ZBformNetBean(context, getFormListUrl,
                HttpRequest.HttpMethod.GET);
        getFormListTask.setNetTaskCallBack(this);
        getFormListTask.execute();
    }

    public void setOnLoginTaskListener(OnGetFormListListener listener) {
        mOnGetFormListListener = listener;
    }

    @Override
    public void onStart() {
        if (mOnGetFormListListener != null) {
            mOnGetFormListListener.onStart();
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
            FormListInfo formList = gson.fromJson(resultGson, new TypeToken<FormListInfo>(){}.getType());

            if (formList != null && formList.header != null
                    && formList.results != null && formList.results.length > 0) {

                Log.i(TAG, "errorcode =" + formList.header.getErrorCode());
                Log.i(TAG, "results conunt: "+ formList.header.getCount());
                Log.i(TAG, "user code =" + formList.results[0].getCode());
                if (formList.header.getErrorCode().equals(ErrorCode.RESULT_OK)) {
                    for(FormListInfo.Results r: formList.results) {
                        Log.i(TAG, r.toString());
                    }
                    if (mOnGetFormListListener != null) {
                        mOnGetFormListListener.onSuccess();
                    }

                } else {
                    if (mOnGetFormListListener != null) {
                        mOnGetFormListListener.onFail();
                    }
                }
            } else {
                if (mOnGetFormListListener != null) {
                    mOnGetFormListListener.onFail();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "e=" + e.getMessage());
//            Toast.makeText(mContext,
//                    mContext.getString(R.string.login_no_formList),
//                    Toast.LENGTH_SHORT).show();
            if (mOnGetFormListListener != null) {
                mOnGetFormListListener.onFail();
            }
        }
    }

    @Override
    public void onFailure(HttpException error, String msg) {
        if (mOnGetFormListListener != null) {
            mOnGetFormListListener.onFail();
        }
    }
}
