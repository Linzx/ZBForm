package com.zbform.penform.task;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.json.FormListInfo;
import com.zbform.penform.json.UserInfo;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.net.ErrorCode;
import com.zbform.penform.net.IZBformNetBeanCallBack;
import com.zbform.penform.net.ZBformNetBean;

import java.util.Arrays;
import java.util.List;

public class FormListTask implements IZBformNetBeanCallBack {
	private static final String TAG = "FormListTask";
	private Context mContext;
	private OnFormTaskListener mOnFormTaskListener;

	public interface OnFormTaskListener {
		public void onStartGet();

		public void onGetSuccess(List<FormListInfo.Results> results);

		public void onGetFail();
	}

	public void execute(Context context) {
		mContext = context;
		String formListUri = ApiAddress.getFormListUri(ZBformApplication.getmLoginUserId(),ZBformApplication.getmLoginUserKey());
		ZBformNetBean formListTask = new ZBformNetBean(context, formListUri,
				HttpRequest.HttpMethod.GET);
		formListTask.setNetTaskCallBack(this);
		formListTask.execute();
	}

	public void setOnFormTaskListener(OnFormTaskListener listener) {
		mOnFormTaskListener = listener;
	}

	@Override
	public void onStart() {
		if (mOnFormTaskListener != null) {
			mOnFormTaskListener.onStartGet();
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
			FormListInfo forms = gson.fromJson(resultGson, new TypeToken<FormListInfo>() {
			}.getType());

			if (forms != null && forms.header != null
					&& forms.results != null && forms.results.length > 0) {

				Log.i("whd", "errorcode!!=" + forms.header.getErrorCode());
				if (forms.header.getErrorCode().equals(ErrorCode.RESULT_OK)) {

					if (mOnFormTaskListener != null) {
						List<FormListInfo.Results> data = Arrays.asList(forms.results);
						mOnFormTaskListener.onGetSuccess(data);
					}

				} else {
					if (mOnFormTaskListener != null) {
						mOnFormTaskListener.onGetFail();
					}
				}
			} else {
				Log.i("whd", "user null");
				if (mOnFormTaskListener != null) {
					mOnFormTaskListener.onGetFail();
				}
			}
		} catch (Exception e) {
			Log.i(TAG, "e=" + e.getMessage());

			if (mOnFormTaskListener != null) {
				mOnFormTaskListener.onGetFail();
			}
		}
	}

	@Override
	public void onFailure(HttpException error, String msg) {
		if (mOnFormTaskListener != null) {
			mOnFormTaskListener.onGetFail();
		}
	}
}
