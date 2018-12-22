package com.zbform.penform.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.zbform.penform.task.LoginTask;
import com.zbform.penform.view.TransitionView;
import com.zbform.penform.R;


public class SignInActivity extends AppCompatActivity implements View.OnClickListener, LoginTask.OnLoginTaskListener {

    private TransitionView mAnimView;
    private Toolbar mToolbar;
    private View mBarContainer;
    private AutoCompleteTextView mNameText;
    private EditText mPwdText;
    private LoginTask mLoginTask;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

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

        mAnimView.setOnLoginFailEndListener(new TransitionView.OnAnimationEndListener() {
            @Override
            public void onEnd() {
                showToolBar();
                Toast.makeText(SignInActivity.this,
                        SignInActivity.this.getString(R.string.login_fail),
                        Toast.LENGTH_SHORT).show();
            }
        });

        mNameText = findViewById(R.id.actv_username);
        mPwdText = findViewById(R.id.edit_password);
        View signUp = findViewById(R.id.tv_sign_up);
        signUp.setOnClickListener(this);
        mLoginTask = new LoginTask();
        mLoginTask.setOnLoginTaskListener(this);
    }

    private void gotoHomeActivity(){
        ZBformMain.launch(this);
        finish();
    }

    public void startLoginView(){
        hideToolBar();

        mAnimView.startLoginAni();
    }

    private void hideToolBar(){
        float curY = mBarContainer.getTranslationY();//获取当前的y轴位置
        float height = mBarContainer.getHeight();
        ObjectAnimator oa = ObjectAnimator.ofFloat(mBarContainer,"translationY",curY,-height);
        oa.setDuration(800);
        oa.start();
    }

    private void showToolBar(){
        float curY = mBarContainer.getTranslationY();//获取当前的y轴位置
        ObjectAnimator oa = ObjectAnimator.ofFloat(mBarContainer,"translationY",curY,0);
        oa.setDuration(800);
        oa.start();
    }


    public void hideInput(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        String id = mNameText.getText().toString();
        String pwd = mPwdText.getText().toString();
        Log.i("whd","id="+id);
        Log.i("whd","pwd="+pwd);
        if (TextUtils.isEmpty(id) || pwd.isEmpty()){
            //return;
        }

        hideInput(v);
        mLoginTask.Login(this, "ZB002", "888888");
    }

    @Override
    public void onStartLogin() {
        Log.i("whd","onStartLogin");
        startLoginView();
    }

    @Override
    public void onLoginSuccess() {
        mAnimView.mSuccess = true;
        if (mAnimView.mSignEnd) {
            mAnimView.startSuccessAni();
        }
    }

    @Override
    public void onLoginFail() {
        mAnimView.mSuccess = false;
        Log.i("whd","onLoginFail");
        if (mAnimView.mSignEnd) {
            mAnimView.startLoginFailAni();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
