package com.zbform.penform.activity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.view.TransitionView;
import com.zbform.penform.R;

public class SignInActivity extends AppCompatActivity{

    private TransitionView mAnimView;
    private Toolbar mToolbar;
    private View mBarContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_login);
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mBarContainer = findViewById(R.id.app_bar);
        mAnimView = (TransitionView) findViewById(R.id.ani_view);
        mAnimView.setParent(findViewById(R.id.sign_content));
        mAnimView.setOnAnimationEndListener(new TransitionView.OnAnimationEndListener(){
            @Override
            public void onEnd() {
                gotoHomeActivity();
            }
        });
    }

    private void gotoHomeActivity(){
        //startActivity(new Intent(this, ZBformMain.class));
        finish();
    }

    public void singUp(View view){
        testGet(view);
        hideInput(view);
        float curY = mBarContainer.getTranslationY();//获取当前的y轴位置
        float height = mBarContainer.getHeight();
        ObjectAnimator oa = ObjectAnimator.ofFloat(mBarContainer,"translationY",curY,-height);
        oa.setDuration(800);
        oa.start();
        mAnimView.startAnimation();


    }

    public void hideInput(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void testGet(View view) {
//        String keyOrigin = "ZB002"+ApiAddress.SYSTEM_KEY;
//        String signcode="";
//        try {
//            String key1 = ZBformApplication.getSHA(keyOrigin);
//            Log.i("whd","key1="+key1);
//
//             signcode =  ZBformApplication.getSHA(key1);
//            Log.i("whd","key2="+signcode);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//
//        long time = System.currentTimeMillis();


        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000 * 10);
        http.send(HttpRequest.HttpMethod.GET,
                ApiAddress.getLoginUri("ZB002","888888"),
                //params,
                new RequestCallBack<String>() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        Log.i("whd","onLoading");
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {

                        Log.i("whd","onSuccess="+responseInfo.result);
                    }


                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Log.i("whd","onFailure="+msg);
                    }
                });
    }
}
