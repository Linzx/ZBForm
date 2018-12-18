package com.zbform.penform.banner;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    private static final String TAG = "JsonUtils";

    public static List<String> parseUrl(String urlStrings){
        Log.e(TAG,"urlStrings" + urlStrings);
        List<String> urls = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(urlStrings);
            Log.d(TAG, jsonObject.toString());
            if (jsonObject.get("returnCode").equals("0")){
                JSONArray jsonArray = jsonObject.getJSONArray("returnData");
                for (int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                    String url = (String) jsonObject1.get("imageUrl");
                    Log.d(TAG, "urls" + url);
                    urls.add(url);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return urls;
    }
}

