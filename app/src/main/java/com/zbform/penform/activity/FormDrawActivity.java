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
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.zbform.penform.db.FormSettingEntity;
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
import com.zbform.penform.view.CustViewPager;
import com.zbform.penform.view.PageItemView;

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

    private FormInfo mFormInfo;
    private int mPage;
    private String mPageAddress;
    private String mFormID;
    private String mFormName;
    private int mDrawState = STATE_PREVIEW;
    private ActionBar mActionBar;
    private FrameLayout mRoot;
    private ImageView mCusDrawItemView;
    private ImageView mShowItem;
    private TextView mPageTitle;
    private Toolbar mToolbar;
    private ImageView mToolbarState;
    private CustViewPager mFormViewPager;
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
//    private PreferencesUtility mPreferencesUtility;
    private FormOpenLoader mFormOpenLoader;
    private RecordNewImageLoader mRecordNewLoader;
    private FormPagerAdapter mPageAdapter;
    private boolean mForceRefresh = false;
    private ArrayList<FormPageHolder> mFormPageHolderList = new ArrayList<>();

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
//        mPreferencesUtility = PreferencesUtility.getInstance(this);
        mResources = getResources();
        mRoot = findViewById(R.id.ll_root);
//        mImgView = (ImageView) findViewById(R.id.form_img);
        mShowItem = findViewById(R.id.show_item);
        mShowItem.setOnClickListener(mOnClickListener);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarState = findViewById(R.id.toolbar_state);

        mPageTitle = findViewById(R.id.page_title);
        mFormViewPager = findViewById(R.id.form_pager);

        mFormViewPager.addOnPageChangeListener(mOnPageChangeListener);

        mPageAdapter = new FormPagerAdapter(this);

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
//
//        if (mToolbar != null) {
//            Menu menu = mToolbar.getMenu();
//            setUpMenu(menu);
//        }
    }

    private void initFormData(Intent intent, int action) {
        if (intent != null) {
            mPage = intent.getIntExtra("page", 1);
            mPageAddress = intent.getStringExtra("pageaddress");
            mFormName = intent.getStringExtra("formname");
//            mCurrentPage = intent.getIntExtra("currentpage", 1);
            setUpPageTitle(1);

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
                initViewPagerData();
                mPageAdapter.setData(mFormPageHolderList);
                mFormViewPager.setAdapter(mPageAdapter);

                if (mFormOpenLoader == null) {
                    mFormOpenLoader = new FormOpenLoader(action);
                } else {
                    mFormOpenLoader.setAction(action);
                }
                mFormOpenLoader.startLoader();
            } else {
                indexViewPager();
            }
        }
    }

    private void indexViewPager(){
        for (FormPageHolder holder : mFormPageHolderList){
            if (holder.mPageAddress.equals(mPageAddress)){

                mFormViewPager.setCurrentItem(holder.mHolderPosition, false);
                break;
            }
        }
    }

    private void initViewPagerData() {
        mFormPageHolderList.clear();
        for (int i = 1; i <= mPage; i++) {
            FormPageHolder holder = new FormPageHolder();
            holder.mImgUrl =
                    ApiAddress.getFormImgUri(ZBformApplication.getmLoginUserId(),
                            ZBformApplication.getmLoginUserId(), mFormID, i);

            holder.mHolderPosition = i - 1;
            holder.mPageAddress = mValAddress.get(String.valueOf(i));
            mFormPageHolderList.add(holder);
        }
    }

    private void drawForm(FormPageHolder formPageHolder) {
        if (formPageHolder.formBitmap != null &&
                formPageHolder.contentView != null) {
            mTargetWidth = formPageHolder.formBitmap.getWidth();
            mTargetHeight = formPageHolder.formBitmap.getHeight();
            Log.i(TAG, "onResourceReady W=" + mTargetWidth);
            Log.i(TAG, "onResourceReady H=" + mTargetHeight);
            mZBFormBlePenManager.setDrawView(formPageHolder.contentView.getImageView(),
                    formPageHolder.formBitmap);
            if (mFormHeight > 0 && mFormWidth > 0) {
                mZBFormBlePenManager.setPaperSize((float) mFormWidth, (float) mFormHeight);
            }
            if (mValAddress != null) {
                String index = String.valueOf(formPageHolder.mHolderPosition + 1);
                mPageAddress = mValAddress.get(index);
                Log.i(TAG, "switch page=" + mPageAddress);
            }
            if (mDrawState == STATE_DRAW && mCacheHwData.size() > 0) {
                new DrawHwDataTask(formPageHolder.formBitmap).execute();
            }

            if (mService != null) {
                mService.setCurrentPageAddress(mPageAddress);
            }
            if (mItemShow) {
                showItemsReF(formPageHolder);
            }
        } else {
            Log.i(TAG, "holder resource null");
        }
    }

    private class FormPageHolder {
        PageItemView contentView;
        Bitmap formBitmap;

        int mHolderPosition;
        String mImgUrl;
        String mPageAddress;
    }

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Log.i(TAG, "onPageSelected=" + position);
            setUpPageTitle(position + 1);
            FormPageHolder pageHolder = mFormPageHolderList.get(position);
            if (pageHolder != null) {
                drawForm(pageHolder);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            Log.i(TAG,"onPageScrollStateChanged");
        }
    };

    private class FormPagerAdapter extends PagerAdapter {
        ArrayList<FormPageHolder> mList;
        Context mContext;
        LayoutInflater mInflater;

        FormPagerAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
        }

        public void setData(ArrayList<FormPageHolder> list) {
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            Log.i(TAG, "instantiateItem=" + position);
            PageItemView contentView = new PageItemView(container.getContext());

            FormPageHolder pageHolder = mList.get(position);
            pageHolder.contentView = contentView;
            new ImageLoader(pageHolder).startLoader();

            container.addView(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return contentView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            PageItemView rootView = (PageItemView) object;
            if (rootView == null)
                return;

            Log.i(TAG, "destroyItem=" + position);
            if (position < mList.size()) {
                FormPageHolder pageHolder = mList.get(position);
                if (pageHolder != null) {
                    pageHolder.contentView = null;
                    pageHolder.formBitmap = null;
                }
            }
            Glide.clear(rootView.getImageView());
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            if (mForceRefresh) {
                return POSITION_NONE;
            } else {
                return super.getItemPosition(object);
            }
        }
    }

    public interface IImageLoaderCallBack {
        void onImgLoadStart();

        void onImgLoadSuccess();

        void onImgLoadFail();
    }

    public class ImageLoader {
        private class DrawImgRequestListener implements RequestListener<String, Bitmap> {
            @Override
            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                Log.i(TAG, "DrawImgRequestListener onException=" + e.getMessage());
                if (mCallback != null) {
                    mCallback.onImgLoadFail();
                }
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, String model,
                                           Target<Bitmap> target, boolean isFromMemoryCache,
                                           boolean isFirstResource) {
                Log.i(TAG, "onResourceReady=" + mFormViewPager.getCurrentItem());
                if (resource != null) {
                    mFormPageHolder.formBitmap = resource;
                    if (mFormPageHolder.mHolderPosition == mFormViewPager.getCurrentItem()) {
                        drawForm(mFormPageHolder);
                    }
                } else {
                    mFormPageHolder.formBitmap = null;
                }
                mItemView.dismissLoading();
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

        int mAction;

        IImageLoaderCallBack mCallback;

        PageItemView mItemView;
        FormPageHolder mFormPageHolder;

        public ImageLoader(FormPageHolder page) {
            mItemView = page.contentView;
            mFormPageHolder = page;
        }

        public ImageLoader(int action, IImageLoaderCallBack callback) {
            mAction = action;
            mCallback = callback;
        }


        private void setAction(int action) {
            mAction = action;
        }

        private void setCallback(IImageLoaderCallBack callback) {
            mCallback = callback;
        }

        public void startLoader() {
            try {
                Glide.with(FormDrawActivity.this)
                        .load(mFormPageHolder.mImgUrl)
                        .asBitmap()
                        .skipMemoryCache(true)
                        .listener(new DrawImgRequestListener())
                        .transform(new FormDrawImgTransformation(FormDrawActivity.this))
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mItemView.getImageView());
            } catch (Exception e) {
                Log.i(TAG, "bitmap ex=" + e.getMessage());
                e.printStackTrace();
            } finally {

            }
        }
    }

    public class FormOpenLoader implements FormTask.OnFormTaskListener{

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
            showLoading(mResources.getString(R.string.loading));
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
                        FormDrawActivity.this.getResources().getString(R.string.toast_img_error),
                        Toast.LENGTH_SHORT).show();
                dismissLoading();
                return;
            }

            dismissLoading();

            FormSettingEntity entity = CommonUtils.getFormSetting(mFormID);
            //自动创建表单记录
            if (entity != null && entity.getOpentype() == 1) {
                if (mRecordNewLoader == null) {
                    mRecordNewLoader = new RecordNewImageLoader(LOAD_ACTION_NEW_RECORD);
                } else {
                    mRecordNewLoader.setAction(LOAD_ACTION_NEW_RECORD);
                }
                mRecordNewLoader.startLoader();
            }
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
    }

    public class RecordNewImageLoader implements NewFormRecordTask.OnNewRecordTaskListener{
        int mAction;
        String mUUID;

        public RecordNewImageLoader(int action) {
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
            showLoading(mResources.getString(R.string.loading_new_record));
        }

        @Override
        public void onNewSuccess(String uuid) {
            mUUID = uuid;

            mForceRefresh = false;
            if (mService != null) {
                mService.setDrawFormInfo(mFormInfo, mUUID);
                mService.setGetHwDataCallBack(mGetHwDataCallBack);
                mService.startDraw();
                setUpToolBarState(true);
                mDrawState = STATE_DRAW;
                Log.i(TAG, "startDraw");
            }
            dismissLoading();

            Toast.makeText(FormDrawActivity.this,
                    FormDrawActivity.this.getResources().getString(R.string.toast_new_record),
                    Toast.LENGTH_SHORT).show();
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
        FormPageHolder mFormPageHolder;
        Bitmap mDrawItemTarget;
        Canvas mCanvas;
        Paint mPaint;
        float mScaleX = 0.1929f;
        float mScaleY = 0.23457f;


        public DrawItemsTask(FormPageHolder holder) {
            mFormPageHolder = holder;
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

                int index = mFormViewPager.getCurrentItem() +1;
                for (FormItem item : items) {
                    if (item != null && index == item.getPage()) {
                        double left = convertPageSize(item.getLocaX()) * mScaleX;
                        double top = convertPageSize(item.getLocaY()) * mScaleY;
                        double right = convertPageSize(item.getLocaX() + item.getLocaW()) * mScaleX;
                        double bottom = convertPageSize(item.getLocaY() + item.getLocaH()) * mScaleY;

//                        Log.i(TAG, "DrawItemsTask left=" + left);
//                        Log.i(TAG, "DrawItemsTask top=" + top);
//                        Log.i(TAG, "DrawItemsTask right=" + right);
//                        Log.i(TAG, "DrawItemsTask bottom=" + bottom);
                        mCanvas.drawRect((float) left, (float) top, (float) right, (float) bottom, mPaint);
                    }
                }

                if (mCusDrawItemView == null && mFormPageHolder.contentView != null) {
                    mCusDrawItemView = new ImageView(FormDrawActivity.this);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            mFormPageHolder.contentView.getImageView().getWidth(),
                            mFormPageHolder.contentView.getImageView().getHeight());
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
                FormPageHolder holder = getCurrentHolder();
                if (holder != null && holder.formBitmap != null) {
                    if (mDrawState == STATE_DRAW) {
                        if (mCacheHwData.size() >0) {
                            mForceRefresh = true;
                            mCacheHwData.clear();
                        }
                        mPageAdapter.notifyDataSetChanged();
                        mFormViewPager.setCurrentItem(0);
                    }
                    if (mRecordNewLoader == null) {
                        mRecordNewLoader = new RecordNewImageLoader(LOAD_ACTION_NEW_RECORD);
                    } else {
                        mRecordNewLoader.setAction(LOAD_ACTION_NEW_RECORD);
                    }
                    mRecordNewLoader.startLoader();
                } else {
                    Toast.makeText(FormDrawActivity.this,
                            FormDrawActivity.this.getResources().getString(
                                    R.string.toast_img_loading),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.i(TAG, "item click");
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
                    FormPageHolder holder = getCurrentHolder();
                    if (holder != null) {
                        showItemsReF(holder);
                    }
                    mItemShow = true;
                }
            }
        }
    };

    private FormPageHolder getCurrentHolder(){
        return mFormPageHolderList.get(mFormViewPager.getCurrentItem());
    }

    private void showItemsReF(FormPageHolder holder) {
        if (mDrawItemsTask == null) {
            mDrawItemsTask = new DrawItemsTask(holder);
        }
        mDrawItemsTask.drawItems();
    }

    public double convertPageSize(double x) {
        return x * 10 * 8 / 0.3;
    }

    private void setUpPageTitle(int pos) {
        if (mPage > 1) {
            mPageTitle.setVisibility(View.VISIBLE);
            mPageTitle.setText(getResources().getString(R.string.page_title, pos));
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

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_draw, menu);

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
                intent.putExtra("title", mFormName);
                startActivity(intent);
                FormDrawActivity.this.finish();
                return true;
            case R.id.form_setting:
                Intent intent1 = new Intent(this, FormSettingActivity.class);
                intent1.putExtra("formid",mFormID);
                startActivity(intent1);
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

}
