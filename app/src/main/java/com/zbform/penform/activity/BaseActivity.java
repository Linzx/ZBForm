package com.zbform.penform.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


/**
 */
public class BaseActivity extends AppCompatActivity{

    private String TAG = "BaseActivity";


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
