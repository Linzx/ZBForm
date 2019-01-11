package com.zbform.penform.activity;


import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.zbform.penform.R;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.blepen.ZBFormBlePenManager;
import com.zbform.penform.dialog.LoadingDialog;
import com.zbform.penform.json.FormInfo;
import com.zbform.penform.json.FormItem;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.services.ZBFormService;
import com.zbform.penform.task.FormTask;
import com.zbform.penform.task.NewFormRecordTask;

import java.util.HashMap;
import java.util.Map;

public class FormDrawActivity extends BaseActivity {
    private static final String TAG = "FormDrawActivity";
    private static final int PRE_IMG = 1;
    private static final int NEXT_IMG = 2;

    private static final int STATE_DRAW = 100;
    private static final int STATE_PREVIEW = 100;

    //    private static final ColorDrawable TRANSPARENT_DRAWABLE = new ColorDrawable(android.R.color.transparent);
    private FormInfo mFormInfo;
    private int mCurrentPage = 1;
    private int mPage;
    private String mPageAddress;
    private String mFormID;
    private String mFormName;
    private int mDrawState = STATE_PREVIEW;
    //保存当前显示的图
    private HashMap<Integer, Bitmap> mCacheImg = new HashMap<Integer, Bitmap>();
    //保存原始空白图
    private HashMap<Integer, Bitmap> mCacheOriginImg = new HashMap<Integer, Bitmap>();
    private ImageView mImgView;
//    private ProgressBar mProgressBar;
    private LoadingDialog mLoadingDialog;
    private FormTask mFromTask;
    private NewFormRecordTask mNewRecordTask;
    private ZBFormService mService;
    private ActionBar mActionBar;
    private ZBFormBlePenManager mZBFormBlePenManager;
    private Resources mResources;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG,"onCreate");
        setContentView(R.layout.formimg_activity);
        mZBFormBlePenManager = ZBFormBlePenManager.getInstance(FormDrawActivity.this);
        mResources = getResources();
        mImgView = (ImageView) findViewById(R.id.form_img);
//        mProgressBar = (ProgressBar) findViewById(R.id.progress_img);
        setToolBar();

        initFormData(getIntent());
        getFormImg(0);

        Intent intent = new Intent(this, ZBFormService.class);
        bindService(intent, conn, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG,"onNewIntent");
        initFormData(intent);
        getFormImg(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (mDrawState == STATE_PREVIEW){
//            if (mService != null) {
//                mService.startDraw();
//            }
//        }
    }

    private void initFormData(Intent intent){
        if (intent != null) {
            mDrawState = STATE_PREVIEW;
            mCurrentPage = 1;
            mCacheImg.clear();
            mPage = intent.getIntExtra("page", 1);
            mPageAddress = intent.getStringExtra("pageaddress");
            mFormID = intent.getStringExtra("formid");
            mFormName = intent.getStringExtra("formname");

            TextView title = findViewById(R.id.toolbar_title);
            title.setText(mFormName);
            clearOriginBitmap();
        }
    }

    private void clearOriginBitmap(){
        try {
            for (Map.Entry<Integer, Bitmap> entry : mCacheOriginImg.entrySet()) {
                if (entry != null && entry.getValue() != null) {
                    entry.getValue().recycle();
                }
            }
            Log.i(TAG,"clearOriginBitmap done");
            mCacheOriginImg.clear();
        }catch (Exception e){

        }
    }

    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            ZBFormService.LocalBinder binder = (ZBFormService.LocalBinder) service;
            mService = binder.getService();
            mService.setCurrentPageAddress(mPageAddress);
        }

        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private FormTask.OnFormTaskListener mFormTaskListener = new FormTask.OnFormTaskListener() {

        @Override
        public void onStartGet() {
//            mProgressBar.setVisibility(View.VISIBLE);
            showLoading(mResources.getString(R.string.loading_new_record));
        }

        @Override
        public void onGetSuccess(FormInfo form) {
            mNewRecordTask = new NewFormRecordTask();
            mNewRecordTask.execute(FormDrawActivity.this, mFormID);
            mNewRecordTask.setOnNewFormTaskListener(mNewRecordListener);
            mFormInfo = form;
//            for (int i = 0; i < mFormInfo.results[0].items.length; i++) {
//                FormItem item = mFormInfo.results[0].items[i];
//                Log.i(TAG, "item=" + item.getItem() + ",fieldName=" + item.getFieldName() + ",page=" +
//                        item.getPage());
//            }
        }

        @Override
        public void onGetFail() {
//            mProgressBar.setVisibility(View.INVISIBLE);
            dismissLoading();
        }
    };

    private NewFormRecordTask.OnNewRecordTaskListener mNewRecordListener = new NewFormRecordTask.OnNewRecordTaskListener() {

        @Override
        public void onStartNew() {}

        @Override
        public void onNewSuccess(String uuid) {

//            mProgressBar.setVisibility(View.INVISIBLE);
            dismissLoading();
            if (mService != null) {
                mService.setDrawFormInfo(mFormInfo, uuid);
                mService.startDraw();
                mDrawState = STATE_DRAW;

                if (mCacheOriginImg.containsKey(mCurrentPage) &&
                        mCacheOriginImg.get(mCurrentPage) != null) {
                    Bitmap origin = mCacheOriginImg.get(mCurrentPage);
                    mImgView.setImageBitmap(origin);
                    mZBFormBlePenManager.setDrawView(mImgView, origin, origin.getWidth(), origin.getHeight());
                } else {
                    Log.i(TAG,"set origin fail");
                }
                Log.i(TAG,"startDraw");
            }
            Toast.makeText(FormDrawActivity.this,FormDrawActivity.this.getResources().getString(R.string.toast_new_record),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNewFail() {
//            mProgressBar.setVisibility(View.INVISIBLE);
            dismissLoading();
        }
    };

    private void getFormImg(int action) {
        try {
//            mProgressBar.setVisibility(View.VISIBLE);
            if (action == PRE_IMG) {
                showLoading(mResources.getString(R.string.loading_pre));
            } else if (action == NEXT_IMG) {
                showLoading(mResources.getString(R.string.loading_next));
            } else {
                showLoading(mResources.getString(R.string.loading));
            }
            Glide.with(this)
                    .load(getUrl())
                    .asBitmap()
                    .transform(new FormDrawImgTransformation(this))
                    .into(mImgView);
//                    .into(mOriginTarget);
        } catch (Exception e) {
            Log.i("whd", "bitmap ex=" + e.getMessage());
            e.printStackTrace();
        } finally {

        }
    }

    private class FormDrawImgTransformation extends BitmapTransformation {

        public FormDrawImgTransformation(Context context) {
            super(context);
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            Log.i(TAG, "transform， outWidth = " + outWidth + "   outHeight = " + outHeight);
            Log.i(TAG, "bitmapp width = " + toTransform.getWidth() + "  height = " + toTransform.getHeight());
            if (toTransform != null) {
                Log.i(TAG, "scalbitmap W=" + toTransform.getWidth());
                Log.i(TAG, "scalbitmap H=" + toTransform.getHeight());
//                mProgressBar.setVisibility(View.INVISIBLE);
                FormDrawActivity.this.
                        runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            dismissLoading();
                                                        }
                                                    }

                );

                if (!mCacheOriginImg.containsKey(mCurrentPage)) {
                    Bitmap origin = toTransform.copy(toTransform.getConfig(), true);
                    mCacheOriginImg.put(mCurrentPage, origin);
                }
                mZBFormBlePenManager.setDrawView(mImgView, toTransform, toTransform.getWidth(), toTransform.getHeight());
            } else {
                Log.i(TAG, "bitmap! null");
            }
            return toTransform;
        }

        @Override
        public String getId() {
            return "com.zbform.penform.FormDrawImgTransformation";
        }
    }

    private String getUrl() {
        if (mCurrentPage < 0 || mCurrentPage > mPage) {
            mCurrentPage = 1;
        }
        return ApiAddress.getFormImgUri(ZBformApplication.getmLoginUserId(),
                ZBformApplication.getmLoginUserId(), mFormID, mCurrentPage);
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
        Log.i(TAG, "mpage=" + mPage);
        mCacheImg.put(mCurrentPage, mZBFormBlePenManager.getDrawBitmap());
        Log.i(TAG, "mCurrentPage1=" + mCurrentPage);
        if (action == PRE_IMG) {
            if (mCurrentPage > 1) {
                mCurrentPage -= 1;
            }
        } else if (action == NEXT_IMG) {
            if (mCurrentPage < mPage) {
                mCurrentPage += 1;
            }
        }
        Log.i(TAG, "mCurrentPage2=" + mCurrentPage);
        if (mCacheImg.containsKey(mCurrentPage)) {
            Log.i(TAG, "mCurrentPage3");
            Bitmap cache = mCacheImg.get(mCurrentPage);
            mImgView.setImageBitmap(cache);

            mZBFormBlePenManager.setDrawView(mImgView, cache, cache.getWidth(), cache.getHeight());
        } else {
            Log.i(TAG, "mCurrentPage4");
            getFormImg(action);
        }
        if (mService != null) {
            mService.setCurrentPage(mCurrentPage);
        }
    }


    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("");
    }

    private void showLoading(String msg){
        mLoadingDialog = new LoadingDialog(this,msg);
        mLoadingDialog.show();
    }

    private void dismissLoading(){
        if (mLoadingDialog != null){
            mLoadingDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_draw, menu);

        MenuItem pre = menu.findItem(R.id.img_pre);
        MenuItem next = menu.findItem(R.id.img_next);

        if (pre != null && next != null) {
            if (mPage > 1) {
                pre.setVisible(true);
                next.setVisible(true);
            } else {
                pre.setVisible(false);
                next.setVisible(false);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.add_record:
                mCurrentPage = 1;
                mCacheImg.clear();
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
        // Unbind from the service
        if (mService != null) {
            mService.stopDraw();
        }
        dismissLoading();
        unbindService(conn);
        clearOriginBitmap();
        super.onDestroy();
    }


    ////////////////////////////////////////////////////////////////////

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
                Log.i("whd", "BitmapTask doInBackground");
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
            Log.i("whd", "BitmapTask onPostExecute");
            if (bitmap != null) {
                Log.i("whd", "scalbitmap W=" + bitmap.getWidth());
                Log.i("whd", "scalbitmap H=" + bitmap.getHeight());
                mImgView.setImageBitmap(bitmap);

//                mProgressBar.setVisibility(View.INVISIBLE);
                dismissLoading();
                mZBFormBlePenManager.setDrawView(mImgView, bitmap, bitmap.getWidth(), bitmap.getHeight());
            } else {
                Log.i("whd", "bitmap! null");
            }
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
//    private void fadeInDisplay(ImageView imageView, Bitmap bitmap) {
//        final TransitionDrawable transitionDrawable =
//                new TransitionDrawable(new Drawable[]{
//                        TRANSPARENT_DRAWABLE,
//                        new BitmapDrawable(imageView.getResources(), bitmap)
//                });
//        imageView.setImageDrawable(transitionDrawable);
//        transitionDrawable.startTransition(500);
//    }

}
