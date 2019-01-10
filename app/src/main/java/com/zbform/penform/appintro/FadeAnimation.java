package com.zbform.penform.appintro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.zbform.penform.R;
import com.zbform.penform.activity.SignInActivity;
import com.zbform.penform.activity.ZBformMain;
import com.zbform.penform.util.PreferencesUtility;

public class FadeAnimation extends BaseAppIntro {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        addSlide(SampleSlide.newInstance(R.layout.intro));
        addSlide(SampleSlide.newInstance(R.layout.intro2));
        addSlide(SampleSlide.newInstance(R.layout.intro3));
        addSlide(SampleSlide.newInstance(R.layout.intro4));

        setFadeAnimation();
    }

    private void loadSignActivity(){
        PreferencesUtility mPreference = PreferencesUtility.getInstance(this);
        mPreference.setShowIntroduce(true);
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        loadSignActivity();
        Toast.makeText(getApplicationContext(), getString(R.string.skip), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {
        loadSignActivity();
    }

    @Override
    public void onSlideChanged() {

    }

    public void getStarted(View v){
        loadSignActivity();
    }
}