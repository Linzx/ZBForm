package com.zbform.penform.task;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.db.ZBStrokeEntity;
import com.zbform.penform.db.ZBStrokePointEntity;
import com.zbform.penform.json.FormInfo;
import com.zbform.penform.json.FormItem;
import com.zbform.penform.json.FormListInfo;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.net.ErrorCode;
import com.zbform.penform.net.IZBformNetBeanCallBack;
import com.zbform.penform.net.ZBformNetBean;

public class FormTask implements IZBformNetBeanCallBack {
    private static final String TAG = "FormTask";
	private Context mContext;
	private OnFormTaskListener mOnFormTaskListener;

	public interface OnFormTaskListener {
		public void onStartGet();

		public void onGetSuccess(FormInfo info);

		public void onGetFail();
	}

	public void execute(Context context, String formid) {
		mContext = context;
		String formListUri = ApiAddress.getFormUri(ZBformApplication.getmLoginUserId(),
				ZBformApplication.getmLoginUserKey(),formid);
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
			Log.i(TAG, "result form Gson!!=" + resultGson);
			Gson gson = new Gson();
			FormInfo form = gson.fromJson(resultGson, new TypeToken<FormInfo>() {
			}.getType());

			if (form != null && form.header != null
					&& form.results != null && form.results.length > 0) {

				Log.i(TAG, "form errorcode!!=" + form.header.getErrorCode());
				if (form.header.getErrorCode().equals(ErrorCode.RESULT_OK)) {

//					FormItem[] item = form.results[0].getItems();
//
					if (mOnFormTaskListener != null) {
						mOnFormTaskListener.onGetSuccess(form);
					}

				} else {
					if (mOnFormTaskListener != null) {
						mOnFormTaskListener.onGetFail();
					}
				}
			} else {
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
