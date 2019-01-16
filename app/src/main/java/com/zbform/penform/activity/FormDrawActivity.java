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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.zbform.penform.R;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.blepen.ZBFormBlePenManager;
import com.zbform.penform.dialog.LoadingDialog;
import com.zbform.penform.json.FormInfo;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.services.ZBFormService;
import com.zbform.penform.task.FormTask;
import com.zbform.penform.task.NewFormRecordTask;

import java.util.HashMap;

public class FormDrawActivity extends BaseActivity {
    private static final String TAG = "FormDrawActivity";
    private static final int ACTION_PRE_IMG = 1;
    private static final int ACTION_NEXT_IMG = 2;
    private static final int ACTION_NEW_RECORD = 3;
    private static final int ACTION_PREVIEW = 4;

    private static final int STATE_PREVIEW = 100;
    private static final int STATE_DRAW = 200;

    private long lastClickTime = 0L;
    private static final int FAST_CLICK_DELAY_TIME = 1000;

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
    private ImageView mImgView;
    private TextView mPageTitle;
    private Toolbar mToolbar;
    private ImageView mToolbarState;
    private LoadingDialog mLoadingDialog;
    private FormTask mFromTask;
    private NewFormRecordTask mNewRecordTask;
    private ZBFormService mService;
    private ActionBar mActionBar;
    private ZBFormBlePenManager mZBFormBlePenManager;
    private Resources mResources;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG,"onCreate");
        setContentView(R.layout.formimg_activity);
        mZBFormBlePenManager = ZBFormBlePenManager.getInstance(FormDrawActivity.this);
        mResources = getResources();
        mImgView = (ImageView) findViewById(R.id.form_img);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarState = findViewById(R.id.toolbar_state);

        mPageTitle = findViewById(R.id.page_title);
        setToolBar();

        Intent intent = new Intent(this, ZBFormService.class);
        bindService(intent, conn, Service.BIND_AUTO_CREATE);

        initFormData(getIntent());
        startLoadingForm(ACTION_PREVIEW);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "onNewIntent");
        // 自动识别纸后，停止上次的draw
        if (mService != null){
            mService.stopDraw();
        }
        initFormData(intent);

        if (mToolbar != null) {
            Menu menu = mToolbar.getMenu();
            setUpMenu(menu);
        }
        startLoadingForm(ACTION_PREVIEW);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initFormData(Intent intent){
        if (intent != null) {
            mDrawState = STATE_PREVIEW;
            mCurrentPage = 1;
            mCacheImg.clear();
            setUpToolBarState(false);
            mPage = intent.getIntExtra("page", 1);
            mPageAddress = intent.getStringExtra("pageaddress");
            mFormID = intent.getStringExtra("formid");
            mFormName = intent.getStringExtra("formname");
            setUpPageTitle();
        }
    }

    private void setUpPageTitle(){
        if (mPage >1) {
            mPageTitle.setVisibility(View.VISIBLE);
            mPageTitle.setText(getResources().getString(R.string.page_title,mCurrentPage));
        } else {
            mPageTitle.setVisibility(View.GONE);
        }
    }
    private void startLoadingForm(int action) {
        try {
            if (action == ACTION_PRE_IMG) {
                showLoading(mResources.getString(R.string.loading_pre));
            } else if (action == ACTION_NEXT_IMG) {
                showLoading(mResources.getString(R.string.loading_next));
            } else if (action == ACTION_NEW_RECORD){
                showLoading(mResources.getString(R.string.loading_new_record));
            } else {
                showLoading(mResources.getString(R.string.loading));
            }
            Glide.with(this)
                    .load(getUrl())
                    .asBitmap()
                    .transform(new FormDrawImgTransformation(this,action))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mImgView);
//                    .into(mOriginTarget);
        } catch (Exception e) {
            Log.i("whd", "bitmap ex=" + e.getMessage());
            e.printStackTrace();
        } finally {

        }
    }

    private void startNewRecord(){
        mNewRecordTask = new NewFormRecordTask();
        mNewRecordTask.execute(FormDrawActivity.this, mFormID);
        mNewRecordTask.setOnNewFormTaskListener(mNewRecordListener);
    }

    private class FormDrawImgTransformation extends BitmapTransformation {
        private int mAction;

        public FormDrawImgTransformation(Context context, int action) {
            super(context);
            mAction = action;
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            Log.i(TAG, "transform， outWidth = " + outWidth + "   outHeight = " + outHeight);
            Log.i(TAG, "bitmapp width = " + toTransform.getWidth() + "  height = " + toTransform.getHeight());
            if (toTransform != null) {
                Log.i(TAG, "scalbitmap W=" + toTransform.getWidth());
                Log.i(TAG, "scalbitmap H=" + toTransform.getHeight());

                mZBFormBlePenManager.setDrawView(mImgView, toTransform,
                        toTransform.getWidth(), toTransform.getHeight());

                if (mAction == ACTION_NEW_RECORD) {
                    startNewRecord();
                } else if (mAction == ACTION_PREVIEW) {
                    mFromTask = new FormTask();
                    mFromTask.setOnFormTaskListener(mFormTaskListener);
                    mFromTask.execute(FormDrawActivity.this, mFormID);
                } else {
                    FormDrawActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissLoading();
                        }
                    });
                }
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

    private FormTask.OnFormTaskListener mFormTaskListener = new FormTask.OnFormTaskListener() {

        @Override
        public void onStartGet() {
        }

        @Override
        public void onGetSuccess(FormInfo form) {
            mFormInfo = form;
            if (mService != null) {
                mService.setCurrentPageAddress(mPageAddress);
            }
            dismissLoading();
        }

        @Override
        public void onGetFail() {
            dismissLoading();
        }

        @Override
        public void onCancelled() {
            dismissLoading();
        }
    };

    private NewFormRecordTask.OnNewRecordTaskListener mNewRecordListener = new NewFormRecordTask.OnNewRecordTaskListener() {
        @Override
        public void onStartNew() {}

        @Override
        public void onNewSuccess(String uuid) {
            if (mService != null) {
                mService.setDrawFormInfo(mFormInfo, uuid);
                mService.startDraw();
                setUpToolBarState(true);
                mDrawState = STATE_DRAW;
                Log.i(TAG,"startDraw");
            }
            dismissLoading();

            Toast.makeText(FormDrawActivity.this,
                    FormDrawActivity.this.getResources().getString(R.string.toast_new_record),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNewFail() {
            dismissLoading();
        }
    };


    private String getUrl() {
        if (mCurrentPage < 0 || mCurrentPage > mPage) {
            mCurrentPage = 1;
        }
        return ApiAddress.getFormImgUri(ZBformApplication.getmLoginUserId(),
                ZBformApplication.getmLoginUserId(), mFormID, mCurrentPage);
    }

    private void switchPages(int action) {
        if (action == ACTION_PRE_IMG && mCurrentPage == 1) {
            Toast.makeText(this, this.getResources().getString(R.string.toast_already_first), Toast.LENGTH_SHORT).show();
            return;
        }
        if (action == ACTION_NEXT_IMG && mCurrentPage == mPage) {
            Toast.makeText(this, this.getResources().getString(R.string.toast_already_last), Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i(TAG, "mPage=" + mPage);

        Log.i(TAG, "mCurrentPage1=" + mCurrentPage);

        Log.i(TAG, "clone begin=" + mCurrentPage);
        Bitmap current = mZBFormBlePenManager.getDrawBitmap();
        if (current == null){
            Toast.makeText(FormDrawActivity.this,
                    FormDrawActivity.this.getResources().getString(R.string.toast_img_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //clone 缓存当前图片
        Bitmap clone = current.copy(current.getConfig(),true);

        if (mCacheImg.containsKey(mCurrentPage) &&
                current == mCacheImg.get(mCurrentPage)){
            Log.i(TAG, "recycle=" + mCurrentPage);
            recycleBitmap(current);
        }

        mCacheImg.put(mCurrentPage, clone);
        //clone

        Log.i(TAG, "clone after=" + mCurrentPage);
        if (action == ACTION_PRE_IMG) {
            if (mCurrentPage > 1) {
                mCurrentPage -= 1;
            }
        } else if (action == ACTION_NEXT_IMG) {
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
            startLoadingForm(action);
        }
        if (mService != null) {
            mService.setCurrentPage(mCurrentPage);
        }
        setUpPageTitle();
    }

    private void recycleBitmap(Bitmap bitmap){
        if(bitmap != null){
            bitmap.recycle();
            System.gc();
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

    private void setUpToolBarState(boolean visible){
        if (mToolbarState != null) {
            mToolbarState.setVisibility(visible == true ? View.VISIBLE: View.INVISIBLE);
        }
    }

    private void setUpMenu(Menu menu) {
        if (menu == null) return;
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
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_draw, menu);
        setUpMenu(menu);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (System.currentTimeMillis() - lastClickTime < FAST_CLICK_DELAY_TIME){
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.add_record:
                if (mDrawState == STATE_DRAW) {
                    mCurrentPage = 1;
                    mCacheImg.clear();
                    startLoadingForm(ACTION_NEW_RECORD);
                } else {
                    showLoading(mResources.getString(R.string.loading_new_record));
                    startNewRecord();
                }
                return true;
            case R.id.img_pre:
                switchPages(ACTION_PRE_IMG);
                return true;
            case R.id.img_next:
                switchPages(ACTION_NEXT_IMG);
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
        super.onDestroy();
    }


    ////////////////////////////////////////////////////////////////////

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
