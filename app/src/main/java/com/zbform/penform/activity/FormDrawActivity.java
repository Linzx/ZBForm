package com.zbform.penform.activity;


import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.zbform.penform.R;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.blepen.ZBFormBlePenManager;
import com.zbform.penform.dialog.LoadingDialog;
import com.zbform.penform.json.FormInfo;
import com.zbform.penform.json.FormItem;
import com.zbform.penform.json.HwData;
import com.zbform.penform.json.Point;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.services.ZBFormService;
import com.zbform.penform.task.FormTask;
import com.zbform.penform.task.NewFormRecordTask;
import com.zbform.penform.util.CommonUtils;
import com.zbform.penform.util.PreferencesUtility;

import java.util.ArrayList;
import java.util.HashMap;

public class FormDrawActivity extends BaseActivity {
    private static final String TAG = "FormDrawActivity";
    private static final int LOAD_ACTION_PRE_IMG = 1;
    private static final int LOAD_ACTION_NEXT_IMG = 2;
    private static final int LOAD_ACTION_NEW_RECORD = 3;
    private static final int LOAD_ACTION_OPEN_FORM = 4;
    private static final int LOAD_ACTION_PEN_OPEN = 5;

    private static final int STATE_PREVIEW = 100;
    private static final int STATE_DRAW = 200;
    private static final int STATE_ITEM_SHOW = 300;

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
    private ActionBar mActionBar;
    private FrameLayout mRoot;
    private ImageView mImgView;
    private ImageView mCusDrawItemView;
    private ImageView mShowItem;
    private TextView mPageTitle;
    private Toolbar mToolbar;
    private ImageView mToolbarState;
    private LoadingDialog mLoadingDialog;
    private FormTask mFromTask;
    private NewFormRecordTask mNewRecordTask;
    private ZBFormService mService;
    private ZBFormBlePenManager mZBFormBlePenManager;
    private Resources mResources;
    private double mFormHeight = 0;
    private double mFormWidth = 0;
    private int mTargetWidth;
    private int mTargetHeight;
    private ArrayList<HwData> mCacheHwData = new ArrayList<>();
    private HashMap<String, String> mValAddress;
    private boolean mItemShow = false;
    private DrawItemsTask mDrawItemsTask;
    private PreferencesUtility mPreferencesUtility;
    private ImageLoader mImageLoader;
    private FormOpenLoader mFormOpenLoader;
    private SwitchPageLoader mSwitchPageLoader;
    private RecordNewLoader mRecordNewLoader;
    private PenOpenLoader mPenOpenLoader;


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

    ZBFormService.IGetHwDataCallBack mGetHwDataCallBack = new ZBFormService.IGetHwDataCallBack() {
        @Override
        public void onGetHwData(HwData data) {
            mCacheHwData.add(data);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");
        setContentView(R.layout.formimg_activity);
        mZBFormBlePenManager = ZBFormBlePenManager.getInstance(FormDrawActivity.this);
        mPreferencesUtility = PreferencesUtility.getInstance(this);
        mResources = getResources();
        mRoot = findViewById(R.id.ll_root);
        mImgView = (ImageView) findViewById(R.id.form_img);
        mShowItem = findViewById(R.id.show_item);
        mShowItem.setOnClickListener(mOnClickListener);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarState = findViewById(R.id.toolbar_state);

        mPageTitle = findViewById(R.id.page_title);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fb_add_record);
        fab.setOnClickListener(mOnClickListener);

        setToolBar();

        Intent intent = new Intent(this, ZBFormService.class);
        bindService(intent, conn, Service.BIND_AUTO_CREATE);

        initFormData(getIntent(), LOAD_ACTION_OPEN_FORM);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "onNewIntent");
        String newAddress = intent.getStringExtra("pageaddress");
        Log.i(TAG, "onNewIntent newAddress=" + newAddress);
        Log.i(TAG, "onNewIntent mPageAddress=" + mPageAddress);
        if (!TextUtils.isEmpty(newAddress) &&
                newAddress.equals(mPageAddress)) {
            return;
        }
        initFormData(intent, LOAD_ACTION_PEN_OPEN);

        if (mToolbar != null) {
            Menu menu = mToolbar.getMenu();
            setUpMenu(menu);
        }
    }

    private void initFormData(Intent intent, int action) {
        if (intent != null) {
            mPage = intent.getIntExtra("page", 1);
            mPageAddress = intent.getStringExtra("pageaddress");
            mFormName = intent.getStringExtra("formname");
            mCurrentPage = intent.getIntExtra("currentpage", 1);
            setUpPageTitle();

            String initAddress = intent.getStringExtra("initaddress");
            String newFormID = intent.getStringExtra("formid");
            if (!newFormID.equals(mFormID)) {
                //新表单恢复初始状态
                mFormID = newFormID;
                if (mService != null) {
                    Log.i(TAG, "initFormData stopDraw");
                    mService.stopDraw();
                }

                mDrawState = STATE_PREVIEW;
                mCacheHwData.clear();
                setUpToolBarState(false);

                Log.i(TAG, "initFormData initAddress=" + initAddress);
                mValAddress = CommonUtils.findValidateAddress(
                        false, initAddress, mPage);
                Log.i(TAG, "initFormData mValAddress size=" + mValAddress.size());


                if (mFormOpenLoader == null) {
                    mFormOpenLoader = new FormOpenLoader(action);
                } else {
                    mFormOpenLoader.setAction(action);
                }
                mFormOpenLoader.startLoader();
            } else {
                if (mPenOpenLoader == null){
                    mPenOpenLoader = new PenOpenLoader(LOAD_ACTION_PEN_OPEN);
                }
                mPenOpenLoader.startLoader();
            }
        }
    }

    public interface ILoaderCallBack {
        void onLoadStart();

        void onLoadSuccess();

        void onLoadFail();
    }

    public class ImageLoader {
        int mAction;

        ILoaderCallBack mCallback;

        private class DrawImgRequestListener implements RequestListener<String, Bitmap> {
            @Override
            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                Log.i(TAG, "DrawImgRequestListener onException=" + e.getMessage());
                if (mCallback != null) {
                    mCallback.onLoadFail();
                }
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, String model,
                                           Target<Bitmap> target, boolean isFromMemoryCache,
                                           boolean isFirstResource) {
                if (resource != null) {
                    mTargetWidth = resource.getWidth();
                    mTargetHeight = resource.getHeight();
                    Log.i(TAG, "onResourceReady W=" + mTargetWidth);
                    Log.i(TAG, "onResourceReady H=" + mTargetHeight);
                    mZBFormBlePenManager.setDrawView(mImgView, resource);
                    if (mFormHeight > 0 && mFormWidth > 0) {
                        mZBFormBlePenManager.setPaperSize((float) mFormWidth, (float) mFormHeight);
                    }
                    if (mAction == LOAD_ACTION_PRE_IMG
                            || mAction == LOAD_ACTION_NEXT_IMG
                            || mAction == LOAD_ACTION_PEN_OPEN) {
                        if (mValAddress != null && mAction != LOAD_ACTION_PEN_OPEN) {
                            mPageAddress = mValAddress.get(String.valueOf(mCurrentPage));
                            Log.i(TAG, "switch page=" + mPageAddress);
                        }
                        if (mDrawState == STATE_DRAW && mCacheHwData.size() > 0) {
                            new DrawHwDataTask(resource).execute();
                        }
                    }

                    if (mService != null) {
                        mService.setCurrentPageAddress(mPageAddress);
                    }
                    if (mItemShow) {
                        showItemsReF();
                    }
                } else {
                    Log.i(TAG, "onResourceReady resource null");
                }

                if (mCallback != null) {
                    mCallback.onLoadSuccess();
                }
                return false;
            }
        }

        private class FormDrawImgTransformation extends BitmapTransformation {

            public FormDrawImgTransformation(Context context) {
                super(context);
            }

            @Override
            protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
//                Log.i(TAG, "transform， outWidth = " + outWidth + "   outHeight = " + outHeight);
//                Log.i(TAG, "bitmapp width = " + toTransform.getWidth() + "  height = " + toTransform.getHeight());
                return toTransform;
            }

            @Override
            public String getId() {
                return "com.zbform.penform.FormDrawImgTransformation";
            }
        }

        public ImageLoader(int action, ILoaderCallBack callback) {
            mAction = action;
            mCallback = callback;
        }

        private void setAction(int action) {
            mAction = action;
        }

        private void setCallback(ILoaderCallBack callback) {
            mCallback = callback;
        }

        public void startLoader() {
            try {
                Glide.with(FormDrawActivity.this)
                        .load(getUrl())
                        .asBitmap()
                        .skipMemoryCache(true)
                        .listener(new DrawImgRequestListener())
                        .transform(new FormDrawImgTransformation(FormDrawActivity.this))
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mImgView);
            } catch (Exception e) {
                Log.i(TAG, "bitmap ex=" + e.getMessage());
                e.printStackTrace();
            } finally {

            }
        }
    }

    public class PenOpenLoader implements ILoaderCallBack {
        int mAction;

        public PenOpenLoader(int action) {
            mAction = action;
        }

        private void setAction(int action) {
            mAction = action;
        }

        public void startLoader() {
            onLoadStart();
            if (mImageLoader == null) {
                mImageLoader = new ImageLoader(mAction, this);
            } else {
                mImageLoader.setAction(mAction);
                mImageLoader.setCallback(this);
            }
            mImageLoader.startLoader();
        }

        @Override
        public void onLoadStart() {
            showLoading(mResources.getString(R.string.loading));
        }

        @Override
        public void onLoadSuccess() {
            FormDrawActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissLoading();
                }
            });
        }

        @Override
        public void onLoadFail() {
            FormDrawActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissLoading();
                }
            });
        }
    }

    public class FormOpenLoader implements FormTask.OnFormTaskListener, ILoaderCallBack {

        int mAction;

        public FormOpenLoader(int action) {
            mAction = action;
        }

        private void setAction(int action) {
            mAction = action;
        }

        public void startLoader() {
            mFromTask = new FormTask();
            mFromTask.setOnFormTaskListener(this);
            mFromTask.execute(FormDrawActivity.this, mFormID);
        }

        @Override
        public void onStartGet() {
            onLoadStart();
        }

        @Override
        public void onGetSuccess(FormInfo form) {
            mFormInfo = form;
            Log.i(TAG, "form onGetSuccess dis");
            mValAddress = CommonUtils.findValidateAddress(
                    false, mFormInfo.results[0].getRinit(),
                    mFormInfo.results[0].getPage());

            mFormHeight = convertPageSize(mFormInfo.results[0].getHeigh());
            mFormWidth = convertPageSize(mFormInfo.results[0].getWidth());
            Log.i(TAG, "form onGetSuccess mFormHeigh=" + mFormHeight);
            Log.i(TAG, "form onGetSuccess mFormWidth=" + mFormWidth);
            mZBFormBlePenManager.setPaperSize((float) mFormWidth, (float) mFormHeight);

            if (TextUtils.isEmpty(mFormID) || mFormInfo == null) {
                Toast.makeText(FormDrawActivity.this,
                        FormDrawActivity.this.getResources().getString(R.string.toast_new_record_fail),
                        Toast.LENGTH_SHORT).show();
                dismissLoading();
                return;
            }

            if (mImageLoader == null) {
                mImageLoader = new ImageLoader(mAction, this);
            } else {
                mImageLoader.setAction(mAction);
                mImageLoader.setCallback(this);
            }
            mImageLoader.startLoader();
        }

        @Override
        public void onGetFail() {
            Log.i(TAG, "onGetFail dis");
            dismissLoading();
        }

        @Override
        public void onCancelled() {
            Log.i(TAG, "onCancelled dis");
            dismissLoading();
        }

        @Override
        public void onLoadStart() {
            showLoading(mResources.getString(R.string.loading));
        }

        @Override
        public void onLoadSuccess() {
            FormDrawActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissLoading();
                }
            });
            if (mPreferencesUtility.getPreFormDefNew()) {
                if (mRecordNewLoader == null) {
                    mRecordNewLoader = new RecordNewLoader(LOAD_ACTION_NEW_RECORD);
                } else {
                    mRecordNewLoader.setAction(LOAD_ACTION_NEW_RECORD);
                }
                mRecordNewLoader.startLoader();
            }
        }

        @Override
        public void onLoadFail() {
            FormDrawActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissLoading();
                }
            });
        }
    }

    public class RecordNewLoader implements NewFormRecordTask.OnNewRecordTaskListener,ILoaderCallBack {
        int mAction;
        String mUUID;

        public RecordNewLoader(int action) {
            mAction = action;
        }

        private void setAction(int action) {
            mAction = action;
        }

        public void startLoader() {
            mNewRecordTask = new NewFormRecordTask();
            mNewRecordTask.setOnNewFormTaskListener(this);
            mNewRecordTask.execute(FormDrawActivity.this, mFormID);
        }

        @Override
        public void onStartNew() {
            onLoadStart();
        }

        @Override
        public void onNewSuccess(String uuid) {
            mUUID = uuid;
            if (mDrawState == STATE_DRAW) {
                mCurrentPage = 1;
                mCacheHwData.clear();
                if (mImageLoader == null) {
                    mImageLoader = new ImageLoader(mAction, this);
                } else {
                    mImageLoader.setAction(mAction);
                    mImageLoader.setCallback(this);
                }
                mImageLoader.startLoader();
            } else {
                onLoadSuccess();
            }

        }

        @Override
        public void onNewFail() {
            Toast.makeText(FormDrawActivity.this,
                    FormDrawActivity.this.getResources().getString(R.string.toast_new_record_fail),
                    Toast.LENGTH_SHORT).show();
            dismissLoading();
        }

        @Override
        public void onCancelled() {
            Toast.makeText(FormDrawActivity.this,
                    FormDrawActivity.this.getResources().getString(R.string.toast_new_record_fail),
                    Toast.LENGTH_SHORT).show();
            dismissLoading();
        }

        @Override
        public void onLoadStart() {
            showLoading(mResources.getString(R.string.loading_new_record));
        }

        @Override
        public void onLoadSuccess() {
            if (mService != null) {
                mService.setDrawFormInfo(mFormInfo, mUUID);
                mService.setGetHwDataCallBack(mGetHwDataCallBack);
                mService.startDraw();
                setUpToolBarState(true);
                mDrawState = STATE_DRAW;
                Log.i(TAG, "startDraw");
            }
            FormDrawActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissLoading();
                }
            });

            Toast.makeText(FormDrawActivity.this,
                    FormDrawActivity.this.getResources().getString(R.string.toast_new_record),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoadFail() {
            FormDrawActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissLoading();
                }
            });
            Toast.makeText(FormDrawActivity.this,
                    FormDrawActivity.this.getResources().getString(R.string.toast_new_record_fail),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public class SwitchPageLoader implements ILoaderCallBack{
        int mAction;
        Context mContext;
        public SwitchPageLoader(Context context, int action) {
            mContext = context;
            mAction = action;
        }

        private void setAction(int action) {
            mAction = action;
        }

        public void startLoader() {
            if (mAction == LOAD_ACTION_PRE_IMG && mCurrentPage == 1) {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_already_first), Toast.LENGTH_SHORT).show();
                return;
            }
            if (mAction == LOAD_ACTION_NEXT_IMG && mCurrentPage == mPage) {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_already_last), Toast.LENGTH_SHORT).show();
                return;
            }
            Log.i(TAG, "mPage=" + mPage);
            Log.i(TAG, "mCurrentPage1=" + mCurrentPage);

            if (mAction == LOAD_ACTION_PRE_IMG) {
                if (mCurrentPage > 1) {
                    mCurrentPage -= 1;
                }
            } else if (mAction == LOAD_ACTION_NEXT_IMG) {
                if (mCurrentPage < mPage) {
                    mCurrentPage += 1;
                }
            }

            onLoadStart();

            if (mImageLoader == null){
                mImageLoader = new ImageLoader(mAction,this);
            } else {
                mImageLoader.setCallback(this);
                mImageLoader.setAction(mAction);
            }
            mImageLoader.startLoader();
            if (mService != null) {
                mService.setCurrentPage(mCurrentPage);
            }
            setUpPageTitle();
        }

        @Override
        public void onLoadStart() {
            if (mAction == LOAD_ACTION_PRE_IMG) {
                showLoading(mResources.getString(R.string.loading_pre));
            } else if (mAction == LOAD_ACTION_NEXT_IMG) {
                showLoading(mResources.getString(R.string.loading_next));
            }
        }

        @Override
        public void onLoadSuccess() {
            FormDrawActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissLoading();
                }
            });
        }

        @Override
        public void onLoadFail() {
            FormDrawActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissLoading();
                }
            });
        }
    }

    private class DrawHwDataTask extends AsyncTask<Void, Void, Void> {
        private Bitmap mDrawTarget;
        Path mPath = new Path();
        float mScaleX = 0.1929f;
        float mScaleY = 0.23457f;

        public DrawHwDataTask(Bitmap target) {
            mDrawTarget = target;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Log.i(TAG, "DrawHwDataTask begin");
                if (mCacheHwData.size() == 0) return null;

                int width = mDrawTarget.getWidth();
                int height = mDrawTarget.getHeight();
                //计算scale
                if (width > height) {
                    if (mFormWidth > mFormHeight) {
                        mScaleX = (float) width / (float) mFormWidth;
                        mScaleY = (float) height / (float) mFormHeight;
                    } else {
                        mScaleX = (float) width / (float) mFormHeight;
                        mScaleY = (float) height / (float) mFormWidth;
                    }
                } else {
                    if (mFormWidth > mFormHeight) {
                        mScaleY = (float) height / (float) mFormWidth;
                        mScaleX = (float) width / (float) mFormHeight;
                    } else {
                        mScaleY = (float) height / (float) mFormHeight;
                        mScaleX = (float) width / (float) mFormWidth;
                    }
                }

                boolean found = false;
                for (HwData data : mCacheHwData) {
                    if (data != null && mPageAddress.equals(data.getP())) {
                        addHwData2Path(data);
                        found = true;
                    }
                }

                if (!found) return null;

                Canvas canvas = new Canvas(mDrawTarget);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
                paint.setStrokeWidth(2f);
                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setDither(true);
                paint.setAntiAlias(true);
                paint.setFilterBitmap(true);
                canvas.drawPath(mPath, paint);

            } catch (Exception e) {
                Log.i(TAG, "queue ex=" + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mImgView.setImageBitmap(mDrawTarget);
            mPath.reset();
            dismissLoading();
        }

        public void addHwData2Path(HwData hwData) {
            Point lastP;
            boolean firstP = true;
            for (Point p : hwData.dList) {
                lastP = p;
                if (firstP) {
                    mPath.moveTo(p.getX() * mScaleX, p.getY() * mScaleY);
                    firstP = false;
                } else {
                    mPath.cubicTo(lastP.getX() * mScaleX, lastP.getY() * mScaleY, ((lastP.getX() + p.getX()) / 2) * mScaleX, ((lastP.getY() + p.getY()) / 2) * mScaleY, p.getX() * mScaleX, p.getY() * mScaleY);
                }
            }
        }
    }

    private class DrawItemsTask {
        Bitmap mDrawItemTarget;
        Canvas mCanvas;
        Paint mPaint;
        float mScaleX = 0.1929f;
        float mScaleY = 0.23457f;


        public DrawItemsTask() {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            mPaint.setStrokeWidth(2f);
            mPaint.setColor(Color.BLUE);
            mPaint.setStyle(Paint.Style.STROKE);
        }

        public void drawItems() {
            try {
                Log.i(TAG, "DrawItemsTask begin");
                if (mFormInfo == null ||
                        mFormInfo.results[0].items == null ||
                        mFormInfo.results[0].items.length == 0) {
                    return;
                }

                FormItem[] items = mFormInfo.results[0].items;

                if (mDrawItemTarget == null) {
                    mDrawItemTarget = Bitmap.createBitmap(mTargetWidth, mTargetHeight,
                            Bitmap.Config.ARGB_8888);
                    mCanvas = new Canvas(mDrawItemTarget);
                } else {
                    mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                }

                int width = mDrawItemTarget.getWidth();
                int height = mDrawItemTarget.getHeight();
                //计算scale
                if (width > height) {
                    if (mFormWidth > mFormHeight) {
                        mScaleX = (float) width / (float) mFormWidth;
                        mScaleY = (float) height / (float) mFormHeight;
                    } else {
                        mScaleX = (float) width / (float) mFormHeight;
                        mScaleY = (float) height / (float) mFormWidth;
                    }
                } else {
                    if (mFormWidth > mFormHeight) {
                        mScaleY = (float) height / (float) mFormWidth;
                        mScaleX = (float) width / (float) mFormHeight;
                    } else {
                        mScaleY = (float) height / (float) mFormHeight;
                        mScaleX = (float) width / (float) mFormWidth;
                    }
                }

                for (FormItem item : items) {
                    if (item != null && mCurrentPage == item.getPage()) {
                        Log.i(TAG, "DrawItemsTask item=" + item);
                        double left = convertPageSize(item.getLocaX()) * mScaleX;
                        double top = convertPageSize(item.getLocaY()) * mScaleY;
                        double right = convertPageSize(item.getLocaX() + item.getLocaW()) * mScaleX;
                        double bottom = convertPageSize(item.getLocaY() + item.getLocaH()) * mScaleY;

                        Log.i(TAG, "DrawItemsTask left=" + left);
                        Log.i(TAG, "DrawItemsTask top=" + top);
                        Log.i(TAG, "DrawItemsTask right=" + right);
                        Log.i(TAG, "DrawItemsTask bottom=" + bottom);
                        mCanvas.drawRect((float) left, (float) top, (float) right, (float) bottom, mPaint);
                    }
                }

                if (mCusDrawItemView == null) {
                    mCusDrawItemView = new ImageView(FormDrawActivity.this);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            mImgView.getWidth(), mImgView.getHeight());
                    params.gravity = Gravity.CENTER;
                    mCusDrawItemView.setLayoutParams(params);
                    mCusDrawItemView.setImageBitmap(mDrawItemTarget);
                    mRoot.addView(mCusDrawItemView);
                } else {
                    mCusDrawItemView.setImageBitmap(mDrawItemTarget);
                    mCusDrawItemView.invalidate();
                }
            } catch (Exception e) {
                Log.i(TAG, "queue ex=" + e.getMessage());
                e.printStackTrace();
            }
        }

        public void clear() {
            if (mDrawItemTarget != null && !mDrawItemTarget.isRecycled()) {
                mDrawItemTarget.recycle();
                mDrawItemTarget = null;
            }
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.fb_add_record) {
                if (mRecordNewLoader == null) {
                    mRecordNewLoader = new RecordNewLoader(LOAD_ACTION_NEW_RECORD);
                } else {
                    mRecordNewLoader.setAction(LOAD_ACTION_NEW_RECORD);
                }
                mRecordNewLoader.startLoader();
            } else {
                Log.i("whd", "item click");
                if (mItemShow) {
                    mShowItem.setImageResource(R.drawable.ic_show_item);
                    if (mCusDrawItemView != null) {
                        mCusDrawItemView.setVisibility(View.GONE);
                    }
                    mItemShow = false;
                } else {
                    mShowItem.setImageResource(R.drawable.ic_show_item_selected);
                    if (mCusDrawItemView != null) {
                        mCusDrawItemView.setVisibility(View.VISIBLE);
                    }
                    showItemsReF();
                    mItemShow = true;
                }
            }
        }
    };

    private void showItemsReF() {
        if (mDrawItemsTask == null) {
            mDrawItemsTask = new DrawItemsTask();
        }
        mDrawItemsTask.drawItems();
    }

    public double convertPageSize(double x) {
        return x * 10 * 8 / 0.3;
    }

    private String getUrl() {
        if (mCurrentPage < 0 || mCurrentPage > mPage) {
            mCurrentPage = 1;
        }
        return ApiAddress.getFormImgUri(ZBformApplication.getmLoginUserId(),
                ZBformApplication.getmLoginUserId(), mFormID, mCurrentPage);
    }

    private void switchPages(int action) {
        if (mSwitchPageLoader == null){
            mSwitchPageLoader = new SwitchPageLoader(this, action);
        } else {
            mSwitchPageLoader.setAction(action);
        }
        mSwitchPageLoader.startLoader();
    }

    private void setUpPageTitle() {
        if (mPage > 1) {
            mPageTitle.setVisibility(View.VISIBLE);
            mPageTitle.setText(getResources().getString(R.string.page_title, mCurrentPage));
        } else {
            mPageTitle.setVisibility(View.GONE);
        }
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("");
    }

    private void showLoading(String msg) {
        mLoadingDialog = new LoadingDialog(this, msg);
        mLoadingDialog.show();
    }

    private void dismissLoading() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
    }

    private void setUpToolBarState(boolean visible) {
        if (mToolbarState != null) {
            mToolbarState.setVisibility(visible == true ? View.VISIBLE : View.INVISIBLE);
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
        if (System.currentTimeMillis() - lastClickTime < FAST_CLICK_DELAY_TIME) {
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_view_record:
                Intent intent = new Intent(FormDrawActivity.this,
                        RecordListActivity.class);
                intent.putExtra("formId", mFormID);
                startActivity(intent);
                FormDrawActivity.this.finish();
                return true;
            case R.id.img_pre:
                switchPages(LOAD_ACTION_PRE_IMG);
                return true;
            case R.id.img_next:
                switchPages(LOAD_ACTION_NEXT_IMG);
                return true;
            default:
                break;
        }
        lastClickTime = System.currentTimeMillis();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        // Unbind from the service
        if (mService != null) {
            mService.stopDraw();
        }
        if (mDrawItemsTask != null) {
            mDrawItemsTask.clear();
        }
        dismissLoading();
        unbindService(conn);
        super.onDestroy();
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
