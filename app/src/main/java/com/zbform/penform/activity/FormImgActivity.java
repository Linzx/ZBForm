package com.zbform.penform.activity;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.zbform.penform.R;

public class FormImgActivity extends BaseActivity {
    private static final ColorDrawable TRANSPARENT_DRAWABLE = new ColorDrawable(android.R.color.transparent);
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.formimg_activity);

        Bitmap img =  (Bitmap) getIntent().getParcelableExtra("info");

        ImageView view = (ImageView) findViewById(R.id.form_img);

        fadeInDisplay(view,img);
    }

    private void fadeInDisplay(ImageView imageView, Bitmap bitmap) {
        final TransitionDrawable transitionDrawable =
                new TransitionDrawable(new Drawable[]{
                        TRANSPARENT_DRAWABLE,
                        new BitmapDrawable(imageView.getResources(), bitmap)
                });
        imageView.setImageDrawable(transitionDrawable);
        transitionDrawable.startTransition(500);
    }
}
