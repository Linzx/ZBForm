package com.zbform.penform.activity;


import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.zbform.penform.R;
import com.zbform.penform.blepen.ZBFormBlePenManager;
import com.zbform.penform.json.FormInfo;
import com.zbform.penform.services.ZBFormService;
import com.zbform.penform.task.FormTask;

public class FormDrawActivity extends BaseActivity {
    private static final String TAG = "FormDrawActivity";
    private static final ColorDrawable TRANSPARENT_DRAWABLE = new ColorDrawable(android.R.color.transparent);
    private String url;
    private String mFormID;
    private ImageView mImgView;
    private ProgressBar progressBar;
    private FormTask mFromTask;
    private ZBFormService mService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.formimg_activity);
        url = getIntent().getStringExtra("info");
        mFormID = getIntent().getStringExtra("formid");
        mImgView = (ImageView) findViewById(R.id.form_img);
        progressBar = (ProgressBar) findViewById(R.id.progress_img);

        Intent intent = new Intent(this, ZBFormService.class);
        bindService(intent, conn, Service.BIND_AUTO_CREATE);

    }

    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            ZBFormService.LocalBinder binder = (ZBFormService.LocalBinder) service;
            mService = binder.getService();
            mFromTask = new FormTask();
            mFromTask.setOnFormTaskListener(mFormTaskListener);
            mFromTask.execute(FormDrawActivity.this, mFormID);
        }
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private FormTask.OnFormTaskListener mFormTaskListener = new FormTask.OnFormTaskListener() {

        @Override
        public void onStartGet() {

        }

        @Override
        public void onGetSuccess(FormInfo form) {
            if (mService != null){
                mService.setDrawFormInfo(form);
            }
            getFormImg();
        }

        @Override
        public void onGetFail() {

        }
    };

    private void getFormImg() {
        try {
            Log.i("whd", "getimg url=" + url);
            Glide.with(this)
                    .load(url)
                    .asBitmap()
                    .into(mOriginTarget);
        } catch (Exception e) {
            Log.i("whd", "bitmap ex=" + e.getMessage());
            e.printStackTrace();
        } finally {

        }
    }

    private SimpleTarget mOriginTarget = new SimpleTarget<Bitmap>() {
        @Override
        public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
            // do something with the bitmap
            // for demonstration purposes, let's just set it to an ImageView
            if (bitmap != null) {
                computeBitmapSize(bitmap, mImgView);
            } else {
                Log.i("whd", "bitmap! null");
            }
        }
    };


    public void computeBitmapSize(Bitmap bitmap, ImageView image) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);

        float scalew = (float) displayMetrics.widthPixels
                / (float) bitmap.getWidth();
        image.setScaleType(ImageView.ScaleType.MATRIX);
        Matrix matrix = new Matrix();
        image.setAdjustViewBounds(true);

        if (displayMetrics.widthPixels < bitmap.getWidth()) {
            matrix.postScale(scalew, scalew);
        } else {
            matrix.postScale(1 / scalew, 1 / scalew);
            scalew = 1 / scalew;
        }
        image.setImageMatrix(matrix);

        Log.i("whd", "displayMetrics W=" + displayMetrics.widthPixels);
        Log.i("whd", "displayMetrics H=" + displayMetrics.heightPixels);
        Log.i("whd", "bitmap W=" + bitmap.getWidth());
        Log.i("whd", "bitmap H=" + bitmap.getHeight());

        float w = scalew * bitmap.getWidth();
        float h = scalew * bitmap.getHeight();

        Log.i("whd", "w=" + w);
        Log.i("whd", "h=" + h);
//        image.setImageBitmap(bitmap);

        Log.i("whd", "image w=" + image.getWidth());
        Log.i("whd", "image h=" + image.getHeight());

        new BitmapTask(this, (int) w, (int) h).execute();


//        image.setMaxWidth(displayMetrics.widthPixels);
//        float imageViewHeight = displayMetrics.heightPixels > bitmap.getHeight() ? displayMetrics.heightPixels
//                : bitmap.getHeight();
//        image.setMaxHeight((int) imageViewHeight);

//        parent.addView(image);

    }

    class BitmapTask extends AsyncTask<Integer, Void, Bitmap> {
        private Context mContext;
        private int mWidth;
        private int mHeight;

        BitmapTask(Context context, int width, int height) {

            mContext = context;
            mWidth = width;
            mHeight = height;
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            Bitmap scaleImg = null;
            try {
                scaleImg = Glide.with(FormDrawActivity.this)
                        .load(url)
                        .asBitmap()
                        .into(mWidth, mHeight)
                        .get();

            } catch (Exception e) {
                Log.i("whd", "e W=" + e.getMessage());
                e.printStackTrace();
            }
            return scaleImg;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                Log.i("whd", "scalbitmap W=" + bitmap.getWidth());
                Log.i("whd", "scalbitmap H=" + bitmap.getHeight());
                mImgView.setImageBitmap(bitmap);

                progressBar.setVisibility(View.INVISIBLE);
//        if (bitmap != null && bitmap.isRecycled()) {
//            bitmap.recycle();
//        }
                ZBFormBlePenManager manager = ZBFormBlePenManager.getInstance(FormDrawActivity.this);

                manager.setDrawView(mImgView, bitmap, bitmap.getWidth(), bitmap.getHeight());
            } else {
                Log.i("whd", "bitmap! null");
            }
        }
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
