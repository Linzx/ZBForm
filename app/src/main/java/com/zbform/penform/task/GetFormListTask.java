package com.zbform.penform.task;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zbform.penform.json.UserInfo;
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
        public void onStartLogin();

        public void onLoginSuccess();

        public void onLoginFail();
    }

    public void getFormList(Context context, String userId, String userKeyStr) {
        mContext = context;
        mUserId = userId;
        mUserKeyStr = userKeyStr;
        String getFormListUrl = ApiAddress.getFormListUri(userId, userKeyStr);
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
            mOnGetFormListListener.onStartLogin();
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
            Log.i("whd", "resultGson!!=" + resultGson);
            Gson gson = new Gson();
            UserInfo user = gson.fromJson(resultGson, new TypeToken<UserInfo>() {
            }.getType());

            if (user != null && user.header != null
                    && user.results != null && user.results.length > 0) {

                Log.i("whd", "errorcode!!=" + user.header.getErrorCode());
                Log.i("whd", "usercode!!=" + user.results[0].getUserCode());
                if (user.header.getErrorCode().equals(ErrorCode.RESULT_OK)
                        && user.results[0].getUserCode().equals(mUserId)) {
                    if (mOnGetFormListListener != null) {
                        mOnGetFormListListener.onLoginSuccess();
                    }

                } else {
                    if (mOnGetFormListListener != null) {
                        mOnGetFormListListener.onLoginFail();
                    }
                    //                    Toast.makeText(mContext, user.getmessage(),
//                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.i("whd", "user null");
                if (mOnGetFormListListener != null) {
                    mOnGetFormListListener.onLoginFail();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "e=" + e.getMessage());
//            Toast.makeText(mContext,
//                    mContext.getString(R.string.login_no_user),
//                    Toast.LENGTH_SHORT).show();
            if (mOnGetFormListListener != null) {
                mOnGetFormListListener.onLoginFail();
            }
        }
    }

    @Override
    public void onFailure(HttpException error, String msg) {
        if (mOnGetFormListListener != null) {
            mOnGetFormListListener.onLoginFail();
        }
    }
}
