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
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.zbform.penform.R;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.blepen.ZBFormBlePenManager;
import com.zbform.penform.json.FormInfo;
import com.zbform.penform.json.FormItem;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.services.ZBFormService;
import com.zbform.penform.task.FormTask;
import com.zbform.penform.task.NewFormRecordTask;

import java.util.HashMap;

public class FormDrawActivity extends BaseActivity {
    private static final String TAG = "FormDrawActivity";
    private static final int PRE_IMG = 1;
    private static final int NEXT_IMG = 2;

    private static final ColorDrawable TRANSPARENT_DRAWABLE = new ColorDrawable(android.R.color.transparent);
    private FormInfo mFormInfo;
    private int mCurrentPage = 1;
    private int mPage;
    private String mFormID;
    private String mFormName;
    private HashMap<Integer,Bitmap> mCacheImg = new HashMap<Integer,Bitmap>();
    private ImageView mImgView;
    private ProgressBar mProgressBar;
    private FormTask mFromTask;
    private NewFormRecordTask mNewRecordTask;
    private ZBFormService mService;
    private ActionBar mActionBar;
    private ZBFormBlePenManager mZBFormBlePenManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.formimg_activity);
//        url = getIntent().getStringExtra("info");
        mZBFormBlePenManager = ZBFormBlePenManager.getInstance(FormDrawActivity.this);
        mPage = getIntent().getIntExtra("page",1);
        mFormID = getIntent().getStringExtra("formid");
        mFormName = getIntent().getStringExtra("formname");
        mImgView = (ImageView) findViewById(R.id.form_img);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_img);
        setToolBar();

        getFormImg();

        Intent intent = new Intent(this, ZBFormService.class);
        bindService(intent, conn, Service.BIND_AUTO_CREATE);
    }

    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            ZBFormService.LocalBinder binder = (ZBFormService.LocalBinder) service;
            mService = binder.getService();
        }
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private FormTask.OnFormTaskListener mFormTaskListener = new FormTask.OnFormTaskListener() {

        @Override
        public void onStartGet() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onGetSuccess(FormInfo form) {
            mNewRecordTask = new NewFormRecordTask();
            mNewRecordTask.execute(FormDrawActivity.this,mFormID);
            mNewRecordTask.setOnNewFormTaskListener(mNewRecordListener);
            mFormInfo = form;
            for (int i = 0;i < mFormInfo.results[0].items.length; i++) {
                FormItem item = mFormInfo.results[0].items[i];
                Log.i(TAG, "item=" + item.getItem() +",fieldName="+item.getFieldName()+",page="+
                item.getPage());
            }
        }

        @Override
        public void onGetFail() {
            mProgressBar.setVisibility(View.INVISIBLE);

        }
    };

    private NewFormRecordTask.OnNewRecordTaskListener mNewRecordListener = new NewFormRecordTask.OnNewRecordTaskListener() {

        @Override
        public void onStartNew() {

        }

        @Override
        public void onNewSuccess(String uuid) {

            mProgressBar.setVisibility(View.INVISIBLE);
            if (mService != null){
                mService.setDrawFormInfo(mFormInfo, uuid);
                mService.startRecordCoord();
                ZBformApplication.sBlePenManager.startDraw();
            }
        }

        @Override
        public void onNewFail() {
            mProgressBar.setVisibility(View.INVISIBLE);

        }
    };

    private void getFormImg() {
        try {
            mProgressBar.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(getUrl())
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

    private String getUrl() {
        if (mCurrentPage < 0 || mCurrentPage > mPage) {
            mCurrentPage = 1;
        }
        return ApiAddress.getFormImgUri(ZBformApplication.getmLoginUserId(),
                ZBformApplication.getmLoginUserId(), mFormID, mCurrentPage);
    }

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

    private void switchPages(int action) {
        if (action == PRE_IMG && mCurrentPage == 1) {
            Toast.makeText(this, this.getResources().getString(R.string.toast_already_first), Toast.LENGTH_SHORT).show();
            return;
        }
        if (action == NEXT_IMG && mCurrentPage == mPage) {
            Toast.makeText(this, this.getResources().getString(R.string.toast_already_last), Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i(TAG,"mpage="+mPage);
        mCacheImg.put(mCurrentPage, mZBFormBlePenManager.getDrawBitmap());
        Log.i(TAG,"mCurrentPage1="+mCurrentPage);
        if (action == PRE_IMG) {
            if (mCurrentPage > 1) {
                mCurrentPage -= 1;
            }
        } else if (action == NEXT_IMG) {
            if (mCurrentPage < mPage) {
                mCurrentPage += 1;
            }
        }
        Log.i(TAG,"mCurrentPage2="+mCurrentPage);
        if (mCacheImg.containsKey(mCurrentPage)) {
            Log.i(TAG,"mCurrentPage3");
            Bitmap cache = mCacheImg.get(mCurrentPage);
            mImgView.setImageBitmap(cache);

            mZBFormBlePenManager.setDrawView(mImgView, cache, cache.getWidth(), cache.getHeight());
        } else {
            Log.i(TAG,"mCurrentPage4");
            getFormImg();
        }
    }

    class BitmapTask extends AsyncTask<Integer, Void, Bitmap> {
        private int mWidth;
        private int mHeight;

        BitmapTask(Context context, int width, int height) {

            mWidth = width;
            mHeight = height;
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            Bitmap scaleImg = null;
            try {
                scaleImg = Glide.with(FormDrawActivity.this)
                        .load(getUrl())
                        .asBitmap()
                        .into(mWidth, mHeight)
                        .get();

            } catch (Exception e) {
                Log.i(TAG, "e W=" + e.getMessage());
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

                mProgressBar.setVisibility(View.INVISIBLE);
                mZBFormBlePenManager.setDrawView(mImgView, bitmap, bitmap.getWidth(), bitmap.getHeight());
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

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(mFormName == null ? "" : mFormName);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_draw, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.add_record:
                mFromTask = new FormTask();
                mFromTask.setOnFormTaskListener(mFormTaskListener);
                mFromTask.execute(FormDrawActivity.this, mFormID);
                return true;
            case R.id.img_pre:
                switchPages(PRE_IMG);
                return true;
            case R.id.img_next:
                switchPages(NEXT_IMG);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mService != null){
            mService.stopRecordCoord();
            ZBformApplication.sBlePenManager.stopDraw();
        }
        unbindService(conn);
    }
}
