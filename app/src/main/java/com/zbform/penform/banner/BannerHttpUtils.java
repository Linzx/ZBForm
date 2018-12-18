package com.zbform.penform.banner;

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class BannerHttpUtils {

    private static final String TAG = "BannerHttpUtils";
    /**
     * @param path    请求的服务器URL地址
     * @param encode    编码格式
     * @return    将服务器端返回的数据转换成String
     */
    public static String sendPostMessage(String path, String encode)
    {
        String result = "";
        HttpClient httpClient = new DefaultHttpClient();
        try
        {
            HttpPost httpPost = new HttpPost(path);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                HttpEntity httpEntity = httpResponse.getEntity();
                if(httpEntity != null)
                {
                    result = EntityUtils.toString(httpEntity, encode);
                    Log.d(TAG, "result" + result);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            httpClient.getConnectionManager().shutdown();
        }

        return result;
    }

    public static String sendGetMessage(String path, String encode) {
        String result = "";
        HttpClient httpClient = new DefaultHttpClient();
        try
        {
            HttpGet httpGet = new HttpGet(path);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            Log.d(TAG,"getStatusCode:" + httpResponse.getStatusLine().getStatusCode());
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                HttpEntity httpEntity = httpResponse.getEntity();
                if(httpEntity != null)
                {
                    result = EntityUtils.toString(httpEntity, encode);
                    Log.d(TAG, "result" + result);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            httpClient.getConnectionManager().shutdown();
        }

        return result;
    }
}
