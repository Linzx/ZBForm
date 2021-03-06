package com.zbform.penform.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import com.zbform.penform.R;
import com.zbform.penform.appintro.FadeAnimation;
import com.zbform.penform.util.PreferencesUtility;


public class WelcomeActivity extends BaseActivity {
    private PreferencesUtility mPreference;
    private ImageView mImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mPreference = PreferencesUtility.getInstance(this);
        if (!mPreference.getShowIntroduce()) {
            Intent intent = new Intent(this, FadeAnimation.class); // Call the AppIntro java class
            startActivity(intent);
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mImageView = (ImageView) findViewById(R.id.img_welcome);
    }
    

    @Override
    protected void onResume() {
        super.onResume();
        Animator animation = AnimatorInflater.loadAnimator(this, R.animator.welcome_animator);
        animation.setTarget(mImageView);
        animation.addListener(new Animator.AnimatorListener(){

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                SignInActivity.launch(WelcomeActivity.this);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animation.start();
    }

}
