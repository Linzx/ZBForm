package com.zbform.penform.net;


import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import android.app.ProgressDialog;
import android.content.Context;

public class ZBformNetBean {

    private static final long HTTPEXPIRY = 1000 * 10;
    private static HttpUtils mHttp;

    private Context mContext;
    private boolean mShowWaiting;
    private String mDialogMsg;
    private String mReqUrl;
    private ProgressDialog mDialog = null;
    private HttpRequest.HttpMethod mHttpMethod;
    private IZBformNetBeanCallBack mZBformNetBeanCallBack;

    public ZBformNetBean(Context context, String reqUrl,
                         HttpRequest.HttpMethod method) {
        mContext = context;
        mReqUrl = reqUrl;
        mHttpMethod = method;
        mShowWaiting = false;
    }

    public ZBformNetBean(Context context, String reqUrl,
                         HttpRequest.HttpMethod method, Boolean showWaiting, String dialogMsg) {
        mContext = context;
        mReqUrl = reqUrl;
        mHttpMethod = method;
        mShowWaiting = showWaiting;
        mDialogMsg = dialogMsg;
    }

    public void setReqUrl(String url){
        mReqUrl = url;
    }
    
    public void setNetTaskCallBack(IZBformNetBeanCallBack callBack){
        mZBformNetBeanCallBack = callBack;
    }

    private HttpUtils getHttpObj() {
        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(HTTPEXPIRY);
        return http;
    }

    public void execute() {
        if (mHttp == null) {
            mHttp = getHttpObj();
        }
        mHttp.send(mHttpMethod, mReqUrl, mRequestCallBack);
    }
    
    public void execute(RequestParams params) {
        if (mHttp == null) {
            mHttp = getHttpObj();
        }
        mHttp.send(mHttpMethod, mReqUrl,params, mRequestCallBack);
    }

    RequestCallBack<String> mRequestCallBack = new RequestCallBack<String>() {
        @Override
        public void onStart() {
            if (mShowWaiting) {
                mDialog = new ProgressDialog(mContext);
                mDialog.setMessage(mDialogMsg);
                mDialog.setIndeterminate(true);
                mDialog.setCancelable(false);
                mDialog.show();
            }
            if (mZBformNetBeanCallBack != null) {
                mZBformNetBeanCallBack.onStart();
            }
        }

        @Override
        public void onLoading(long total, long current, boolean isUploading) {
            if (mZBformNetBeanCallBack != null) {
                mZBformNetBeanCallBack.onLoading(total, current, isUploading);
            }
        }

        @Override
        public void onSuccess(ResponseInfo<String> responseInfo) {
            if (mZBformNetBeanCallBack != null) {
                mZBformNetBeanCallBack.onSuccess(responseInfo);
            }
            if (mDialog != null) {
                mDialog.dismiss();
            }
        }

        @Override
        public void onFailure(HttpException error, String msg) {
            if (mZBformNetBeanCallBack != null) {
                mZBformNetBeanCallBack.onFailure(error, msg);
            }
            if (mDialog != null) {
                mDialog.dismiss();
            }
        }

        @Override
        public void onCancelled() {
            super.onCancelled();
            if (mZBformNetBeanCallBack != null) {
                mZBformNetBeanCallBack.onCancelled();
            }
            if (mDialog != null) {
                mDialog.dismiss();
            }
        }
    };
}
