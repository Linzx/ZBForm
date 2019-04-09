package com.zbform.penform.task;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.json.FormInfo;
import com.zbform.penform.json.NewRecordInfo;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.net.ErrorCode;
import com.zbform.penform.net.IZBformNetBeanCallBack;
import com.zbform.penform.net.ZBformNetBean;

public class NewFormRecordTask implements IZBformNetBeanCallBack {
    private static final String TAG = "NewFormRecordTask";
	private Context mContext;
	private OnNewRecordTaskListener mOnNewRecordTaskListener;

	public interface OnNewRecordTaskListener {
		public void onStartNew();

		public void onNewSuccess(String uuid);

		public void onNewFail();

		public void onCancelled();
	}

	public void execute(Context context, String formid) {
		mContext = context;
		String formListUri = ApiAddress.getNewRecordUri(ZBformApplication.getmLoginUserId(),
				ZBformApplication.getmLoginUserKey(),formid,
				ZBformApplication.sBlePenManager.getBleDeviceSyncNum(),
				ZBformApplication.sBlePenManager.getBleDeviceMac());
		ZBformNetBean formListTask = new ZBformNetBean(context, formListUri,
				HttpRequest.HttpMethod.GET);
		formListTask.setNetTaskCallBack(this);
		formListTask.execute();
	}

	public void setOnNewFormTaskListener(OnNewRecordTaskListener listener) {
		mOnNewRecordTaskListener = listener;
	}

	@Override
	public void onStart() {
		if (mOnNewRecordTaskListener != null) {
			mOnNewRecordTaskListener.onStartNew();
		}
	}

	@Override
	public void onCancelled() {
		if (mOnNewRecordTaskListener != null) {
			mOnNewRecordTaskListener.onCancelled();
		}

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
            NewRecordInfo newRecordInfo = gson.fromJson(resultGson, new TypeToken<NewRecordInfo>() {
			}.getType());

			if (newRecordInfo != null && newRecordInfo.header != null
					&& newRecordInfo.results != null && newRecordInfo.results.length > 0) {

				Log.i(TAG, "form errorcode!!=" + newRecordInfo.header.getErrorCode());
				if (newRecordInfo.header.getErrorCode().equals(ErrorCode.RESULT_OK)) {

					String id = newRecordInfo.results[0].uuid;
					if (mOnNewRecordTaskListener != null) {
						mOnNewRecordTaskListener.onNewSuccess(id);
					}

				} else {
					if (mOnNewRecordTaskListener != null) {
						mOnNewRecordTaskListener.onNewFail();
					}
				}
			} else {
				if (mOnNewRecordTaskListener != null) {
					mOnNewRecordTaskListener.onNewFail();
				}
			}
		} catch (Exception e) {
			Log.i(TAG, "e=" + e.getMessage());

			if (mOnNewRecordTaskListener != null) {
				mOnNewRecordTaskListener.onNewFail();
			}
		}
	}

	@Override
	public void onFailure(HttpException error, String msg) {
		if (mOnNewRecordTaskListener != null) {
			mOnNewRecordTaskListener.onNewFail();
		}
	}
}
