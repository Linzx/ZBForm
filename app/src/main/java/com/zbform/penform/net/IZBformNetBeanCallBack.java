package com.zbform.penform.net;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;

public interface IZBformNetBeanCallBack {
    public void onStart();

    public void onCancelled();

    public void onLoading(long total, long current, boolean isUploading);

    public void onSuccess(ResponseInfo<String> responseInfo);

    public void onFailure(HttpException error, String msg);
}
