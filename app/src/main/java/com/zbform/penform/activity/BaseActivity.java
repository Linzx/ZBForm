package com.zbform.penform.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


/**
 * Created by isaac on 2018/8/2.
 * activity基类
 */
public class BaseActivity extends AppCompatActivity{
    private String TAG = "BaseActivity";

    /**
     * @param outState 取消保存状态
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * @param savedInstanceState 取消保存状态
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //super.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
    }

}
