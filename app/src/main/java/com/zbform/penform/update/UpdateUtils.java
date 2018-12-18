package com.zbform.penform.update;

import android.util.Log;

import org.json.JSONObject;

public class UpdateUtils {

    private static final String TAG = "UpdateUtils";

    public static String parseUrl(String urlStrings){
        Log.d(TAG,"urlStrings" + urlStrings);
        String downloadUrls = null;
        try {
            JSONObject jsonObject = new JSONObject(urlStrings);
            Log.d(TAG, "jsonObject:" + jsonObject.toString());
            int reCode = (int) jsonObject.get("returnCode");
            if (jsonObject.get("returnCode").equals("0") || reCode == 0){
                JSONObject returnData = jsonObject.getJSONObject("returnData");
                downloadUrls = (String) returnData.get("downloadUrl");
                Log.d(TAG,"downloadUrls:" + downloadUrls);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return downloadUrls;
    }
}
