package com.zbform.penform.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.zbform.penform.view.TransitionView;
import com.zbform.penform.R;

public class SignInActivity extends AppCompatActivity
{

    private TransitionView mAnimView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_login);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.sign_up_activity);

        mAnimView = (TransitionView) findViewById(R.id.ani_view);

        mAnimView.setOnAnimationEndListener(new TransitionView.OnAnimationEndListener()
        {
            @Override
            public void onEnd()
            {
                //跳转到主页面
                gotoHomeActivity();
            }
        });
    }

    private void gotoHomeActivity()
    {
        //startActivity(new Intent(this, ZBformMain.class));
        finish();
    }

    public void singUp(View view)
    {
        mAnimView.startAnimation();
    }
}
