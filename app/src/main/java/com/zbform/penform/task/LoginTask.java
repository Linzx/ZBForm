package com.zbform.penform.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

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

public class LoginTask implements IZBformNetBeanCallBack {
	private static final String TAG = "LoginTask";
	private String mPwd;
	private String mUserId;
	private Context mContext;
	private OnLoginTaskListener mOnLoginTaskListener;

	public interface OnLoginTaskListener {
		public void onStartLogin();

		public void onLoginSuccess();

		public void onLoginFail();
	}

	public void Login(Context context, String name, String pwd) {
		mContext = context;
		mPwd = pwd;
		mUserId = name;
		String loginUrl = ApiAddress.getLoginUri(name, pwd);
		ZBformNetBean loginTask = new ZBformNetBean(context, loginUrl,
				HttpRequest.HttpMethod.GET);
		loginTask.setNetTaskCallBack(this);
		loginTask.execute();
	}

	public void setOnLoginTaskListener(OnLoginTaskListener listener) {
		mOnLoginTaskListener = listener;
	}

	@Override
	public void onStart() {
		if (mOnLoginTaskListener != null) {
			mOnLoginTaskListener.onStartLogin();
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
					if (mOnLoginTaskListener != null) {
						mOnLoginTaskListener.onLoginSuccess();
					}

				} else {
					if (mOnLoginTaskListener != null) {
						mOnLoginTaskListener.onLoginFail();
					}
					//					Toast.makeText(mContext, user.getmessage(),
//							Toast.LENGTH_SHORT).show();
				}
			} else {
				Log.i("whd", "user null");
				if (mOnLoginTaskListener != null) {
					mOnLoginTaskListener.onLoginFail();
				}
			}
		} catch (Exception e) {
			android.util.Log.i(TAG, "e=" + e.getMessage());
//			Toast.makeText(mContext,
//					mContext.getString(R.string.login_no_user),
//					Toast.LENGTH_SHORT).show();
			if (mOnLoginTaskListener != null) {
				mOnLoginTaskListener.onLoginFail();
			}
		}
	}

	@Override
	public void onFailure(HttpException error, String msg) {
		if (mOnLoginTaskListener != null) {
			mOnLoginTaskListener.onLoginFail();
		}
	}
}
