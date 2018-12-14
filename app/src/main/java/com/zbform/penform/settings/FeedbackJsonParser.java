package com.zbform.penform.settings;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;


public class FeedbackJsonParser {

    private JSONObject jsonObject;
    String json;

    private static final String TAG = "FeedbackJsonParser";

    public JSONObject makeHttpRequest(String url, String method, JSONObject params){

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            String charSet = "UTF-8";
            Log.d(TAG,"request jsonString:"+params.toString());
            StringEntity entity = new StringEntity(params.toString(), charSet);
            httpPost.setEntity(entity);
            HttpResponse response = null;
            response = httpClient.execute(httpPost);
            StatusLine status = response.getStatusLine();
            int state = status.getStatusCode();
            Log.d(TAG,"status:" + state);
            if (state == HttpStatus.SC_OK) {
                HttpEntity responseEntity = response.getEntity();
                json = EntityUtils.toString(responseEntity);
                Log.d(TAG, "jsonString:" + json);
            }
//            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
//            HttpResponse httpResponse = httpClient.execute(httpPost);
//            HttpEntity httpEntity = httpResponse.getEntity();
//            inputStream = httpEntity.getContent();
        }catch (Exception e){
            e.printStackTrace();
        }

//        try {
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
//            StringBuilder stringBuilder = new StringBuilder();
//            String readLine = null;
//            while ((readLine = bufferedReader.readLine()) != null){
//                stringBuilder.append(readLine + "\n");
//            }
//            inputStream.close();
//            json = stringBuilder.toString();

//        }catch (Exception e){
//            e.printStackTrace();
//        }
        try {
            jsonObject = new JSONObject(json);
        }catch (Exception e){
            e.printStackTrace();
        }

        return jsonObject;
    }
}
