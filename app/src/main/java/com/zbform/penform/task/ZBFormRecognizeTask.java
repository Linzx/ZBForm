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
import com.zbform.penform.json.ZBFormRecgonizeResultInfo;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.net.IZBformNetBeanCallBack;
import com.zbform.penform.net.ZBformNetBean;

public class ZBFormRecognizeTask implements IZBformNetBeanCallBack {
	private static final String TAG = "ZBFormRecognizeTask";
	private Context mContext;
	private String mRecordId;

	private OnZBFormRecognizeTaskListener mOnZBFormRecognizeTaskListener;

	public interface OnZBFormRecognizeTaskListener {
		void onStartGet();

		void onGetSuccess(ZBFormRecgonizeResultInfo info);

		void onGetFail();

		void onCancelled();
	}

	public ZBFormRecognizeTask(Context context, String recordId) {
		mContext = context;
		mRecordId = recordId;
	}

	public void execute() {
		String recognizeUri = ApiAddress.getZbformRecognizeUri(mRecordId);
		ZBformNetBean recognizeTask = new ZBformNetBean(mContext, recognizeUri,
				HttpRequest.HttpMethod.GET);
		recognizeTask.setNetTaskCallBack(this);
		recognizeTask.execute();
	}

	public void setOnRecognizeTaskListener(OnZBFormRecognizeTaskListener listener) {
		mOnZBFormRecognizeTaskListener = listener;
	}

	@Override
	public void onStart() {
		Log.i(TAG, "onStart");

		if (mOnZBFormRecognizeTaskListener != null) {
			mOnZBFormRecognizeTaskListener.onStartGet();
		}
	}

	@Override
	public void onCancelled() {
		Log.i(TAG, "onCancelled");

		if (mOnZBFormRecognizeTaskListener != null) {
			mOnZBFormRecognizeTaskListener.onCancelled();
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
			ZBFormRecgonizeResultInfo result = gson.fromJson(resultGson, new TypeToken<ZBFormRecgonizeResultInfo>() {
			}.getType());

			if (mOnZBFormRecognizeTaskListener != null) {
				mOnZBFormRecognizeTaskListener.onGetSuccess(result);
			}

		} catch (Exception e) {
			Log.i(TAG, "e=" + e.getMessage());
			if (mOnZBFormRecognizeTaskListener != null) {
				mOnZBFormRecognizeTaskListener.onGetFail();
			}
		}
	}

	@Override
	public void onFailure(HttpException error, String msg) {
		Log.i(TAG, "onFailure" + "error: " + error.toString());
		Log.i(TAG, "msg: " + msg);

		if (mOnZBFormRecognizeTaskListener != null) {
			mOnZBFormRecognizeTaskListener.onGetFail();
		}
	}
}
