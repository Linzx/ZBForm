package com.zbform.penform.activity;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.zbform.penform.R;
import com.zbform.penform.blepen.TouchImageView;
import com.zbform.penform.blepen.ZBFormBlePenManager;

public class FormImgActivity extends BaseActivity {
    private static final ColorDrawable TRANSPARENT_DRAWABLE = new ColorDrawable(android.R.color.transparent);
    String url;
    TouchImageView mImgView;
    ViewGroup parent;
    ProgressBar progressBar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.formimg_activity);
url = getIntent().getStringExtra("info");
//        Bitmap img =  (Bitmap) getIntent().getParcelableExtra("info");
//        parent = findViewById(R.id.content);
        mImgView = (TouchImageView) findViewById(R.id.form_img);
        progressBar = (ProgressBar) findViewById(R.id.progress_img);

//        fadeInDisplay(view,img);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                getImg();
//            }
//        }).start();
        getImg();
    }

    private void getImg(){
//        SimpleTarget bitmap = null;
        try {
//            file = Glide.with(context)
//                    .load(url)
//                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
//                    .get();

            Log.i("whd", "getimg url="+url);
            Glide.with(this)
                    .load(url)
                    .asBitmap()
                    .into(target);
                        //.into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    //.get();
//
        } catch (Exception e) {
            Log.i("whd", "bitmap ex="+e.getMessage());
            e.printStackTrace();
        } finally {

        }
    }
    private SimpleTarget target = new SimpleTarget<Bitmap>() {
        @Override
        public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
            // do something with the bitmap
            // for demonstration purposes, let's just set it to an ImageView
//            imageView1.setImageBitmap( bitmap );
            if (bitmap != null){
                // 在这里执行图片保存方法
                setImageViewMathParent(bitmap,mImgView);
                Log.i("whd", "getimg! h="+bitmap.getHeight());
                Log.i("whd", "getimg! w="+bitmap.getWidth());
            } else {
                Log.i("whd", "bitmap! null");
            }
        }
    };

    private void fadeInDisplay(ImageView imageView, Bitmap bitmap) {
        final TransitionDrawable transitionDrawable =
                new TransitionDrawable(new Drawable[]{
                        TRANSPARENT_DRAWABLE,
                        new BitmapDrawable(imageView.getResources(), bitmap)
                });
        imageView.setImageDrawable(transitionDrawable);
        transitionDrawable.startTransition(500);
    }

    public void setImageViewMathParent(Bitmap bitmap ,ImageView image) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);
        Log.i("whd","displayMetrics W="+displayMetrics.widthPixels);
        Log.i("whd","displayMetrics H="+displayMetrics.heightPixels);
        float scalew = (float) displayMetrics.widthPixels
                / (float) bitmap.getWidth();
        image.setScaleType(ImageView.ScaleType.MATRIX);
        Matrix matrix = new Matrix();
        image.setAdjustViewBounds(true);

        if (displayMetrics.widthPixels < bitmap.getWidth()) {
            matrix.postScale(scalew, scalew);
        } else {
            matrix.postScale(1 / scalew, 1 / scalew);
        }
        image.setImageMatrix(matrix);

//        image.setMaxWidth(displayMetrics.widthPixels);
//        float imageViewHeight = displayMetrics.heightPixels > bitmap.getHeight() ? displayMetrics.heightPixels
//                : bitmap.getHeight();
//        image.setMaxHeight((int) imageViewHeight);
        image.setImageBitmap(bitmap);
//        parent.addView(image);
        progressBar.setVisibility(View.INVISIBLE);
//        if (bitmap != null && bitmap.isRecycled()) {
//            bitmap.recycle();
//        }
        Log.i("whd", "set done W="+image.getWidth());
        Log.i("whd", "set done H="+image.getHeight());
        ZBFormBlePenManager manager = ZBFormBlePenManager.getInstance(FormImgActivity.this);

        manager.setDrawView(mImgView,bitmap,image.getWidth(), image.getHeight());
    }

}
