package com.zbform.penform.task;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zbform.penform.json.ZBFormGetRecognizedDataInfo;
import com.zbform.penform.json.ZBFormRecgonizeResultInfo;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.net.IZBformNetBeanCallBack;
import com.zbform.penform.net.ZBformNetBean;

public class ZBFormGetRecognizedDataTask implements IZBformNetBeanCallBack {
	private static final String TAG = "GetRecognizedDataTask";
	private Context mContext;
	private String mRecordId;

	private OnZBFormGetRecognizedDataTaskListener mOnZBFormGetRecognizedDataTaskListener;

	public interface OnZBFormGetRecognizedDataTaskListener {
		void onStartGet();

		void onGetSuccess(ZBFormGetRecognizedDataInfo info);

		void onGetFail();

		void onCancelled();
	}

	public ZBFormGetRecognizedDataTask(Context context, String recordId) {
		mContext = context;
		mRecordId = recordId;
	}

	public void execute() {
		String recognizeUri = ApiAddress.getZbformRecognizeDataUri(mRecordId);
		ZBformNetBean recognizeTask = new ZBformNetBean(mContext, recognizeUri,
				HttpRequest.HttpMethod.GET);
		recognizeTask.setNetTaskCallBack(this);
		recognizeTask.execute();
	}

	public void setOnZBFormGetRecognizedDataTaskListener(OnZBFormGetRecognizedDataTaskListener listener) {
		mOnZBFormGetRecognizedDataTaskListener = listener;
	}

	@Override
	public void onStart() {
		Log.i(TAG, "onStart");

		if (mOnZBFormGetRecognizedDataTaskListener != null) {
			mOnZBFormGetRecognizedDataTaskListener.onStartGet();
		}
	}

	@Override
	public void onCancelled() {
		Log.i(TAG, "onCancelled");

		if (mOnZBFormGetRecognizedDataTaskListener != null) {
			mOnZBFormGetRecognizedDataTaskListener.onCancelled();
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
			ZBFormGetRecognizedDataInfo result = gson.fromJson(resultGson, new TypeToken<ZBFormGetRecognizedDataInfo>() {
			}.getType());

			if (mOnZBFormGetRecognizedDataTaskListener != null) {
				mOnZBFormGetRecognizedDataTaskListener.onGetSuccess(result);
			}

		} catch (Exception e) {
			Log.i(TAG, "e=" + e.getMessage());
			if (mOnZBFormGetRecognizedDataTaskListener != null) {
				mOnZBFormGetRecognizedDataTaskListener.onGetFail();
			}
		}
	}

	@Override
	public void onFailure(HttpException error, String msg) {
		Log.i(TAG, "onFailure" + "error: " + error.toString());
		Log.i(TAG, "msg: " + msg);

		if (mOnZBFormGetRecognizedDataTaskListener != null) {
			mOnZBFormGetRecognizedDataTaskListener.onGetFail();
		}
	}
}
