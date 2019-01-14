package com.zbform.penform.activity;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zbform.penform.R;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.dialog.LoadingDialog;
import com.zbform.penform.json.FormInfo;
import com.zbform.penform.json.FormItem;
import com.zbform.penform.json.HwData;
import com.zbform.penform.json.Point;
import com.zbform.penform.json.RecordDataItem;
import com.zbform.penform.json.RecordInfo;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.services.ZBFormService;
import com.zbform.penform.task.FormTask;
import com.zbform.penform.task.RecordTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecordActivity extends BaseActivity implements RecordTask.OnTaskListener {

    public static final String TAG = RecordActivity.class.getSimpleName();

    private static final int PRE_IMG = 1;
    private static final int NEXT_IMG = 2;

    private int mCurrentPage = 1;
//    private HashMap<Integer, Bitmap> mCacheImg = new HashMap<Integer, Bitmap>();

    private static List<RecordInfo.Results> recordResults = new ArrayList<>();
    private List<RecordDataItem> mCurrentItems = new ArrayList<>();
    private RecordTask mTask;
    private String mFormId;
    private String mRecordId;
    private String mRecordCode;
    private String mPageAddress;
    private int mPage;

    private LoadingDialog mLoadingDialog;
    ActionBar mActionBar;
    ImageView mRecordImg;
    Bitmap mRecordBitmapImg;

    Path mPath = new Path();
    float mScaleX = 0.1929f;
    float mScaleY = 0.23457f;

    private FormTask mFormTask;
    private FormInfo mFormInfo = null;

    private Context mContext;
    private ZBFormService mService;


    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            ZBFormService.LocalBinder binder = (ZBFormService.LocalBinder) service;
            mService = binder.getService();
//            mService.setCurrentPageAddress(mPageAddress);
        }

        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_record);

        mContext = this;
        mRecordImg = findViewById(R.id.record_img);
        mFormId = getIntent().getStringExtra("formId");
        mRecordId = getIntent().getStringExtra("recordId");
        mPage = getIntent().getIntExtra("page", 0);
        mRecordCode = getIntent().getStringExtra("recordCode");
        Log.i(TAG, "form id = " + mFormId + "  record id = " + mRecordId + "  page = " + mPage + "  record code = " + mRecordCode);

        setToolBar();

        initData();

        Log.i(TAG, "bind service: zbform service");
        Intent intent = new Intent(this, ZBFormService.class);
        bindService(intent, conn, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            mService.setIsRecordDraw(false);
            mService.stopDraw();
        }
        dismissLoading();
        unbindService(conn);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mService != null) {
            if(mPageAddress != null && mFormInfo != null && mRecordId!= null) {
                mService.setCurrentPageAddress(mPageAddress);
                mService.setIsRecordDraw(true);
                mService.setDrawFormInfo(mFormInfo, mRecordId);
            }
            Log.i(TAG, "startDraw");
            mService.startDraw();
        }
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(mRecordCode);
    }


    private void initData() {
        mTask = new RecordTask(mContext, mFormId, mRecordId);
        mTask.setTaskListener(this);

        // 获取表单，为了查询form item
        mFormTask = new FormTask();
        mFormTask.setOnFormTaskListener(mFormTaskListener);
        mFormTask.execute(mContext, mFormId);

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_page, menu);

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
            case R.id.img_pre:
                switchPages(PRE_IMG);
                return true;
            case R.id.img_next:
                if (mFormInfo == null) {
                    mFormTask = new FormTask();
                    mFormTask.setOnFormTaskListener(mFormTaskListener);
                    mFormTask.execute(mContext, mFormId);
                }
                switchPages(NEXT_IMG);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskStart() {

    }

    @Override
    public void onTaskSuccess(List<RecordInfo.Results> results) {
        recordResults = results;
        Log.i(TAG, "onTaskSuccess()");

        // 获取Form 表单的图片，准备合成
        getFormImg(getUrl());
    }

    private List<RecordDataItem> getCurrentItems() {
        if (mPage == 1 && recordResults != null && recordResults.size() > 0) {
            return Arrays.asList(recordResults.get(0).getItems());
        }
        List<RecordDataItem> itemList = new ArrayList<>();
        if (recordResults.size() > 0) {
            RecordDataItem[] items = recordResults.get(0).getItems();
            for (RecordDataItem item : items) {

                String itemId = item.getItemcode();
                for (FormItem formItem : mFormInfo.results[0].getItems()) {
                    int p = formItem.getPage();
                    if (mCurrentPage == p && itemId.equals(formItem.getItem())) {
                        itemList.add(item);
                    }
                }
            }
        }
        return itemList;
    }

    private String getUrl() {
        if (mCurrentPage < 0 || mCurrentPage > mPage) {
            mCurrentPage = 1;
        }
        return ApiAddress.getFormImgUri(ZBformApplication.getmLoginUserId(),
                ZBformApplication.getmLoginUserId(), mFormId, mCurrentPage);
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
        Log.i(TAG, "page size = " + mPage);
//        if (!mCacheImg.containsKey(mCurrentPage)) {
//            Log.i(TAG, "put img to cache， page = "+mCurrentPage+"   hascode = " +mRecordBitmapImg.hashCode());
//            mCacheImg.put(mCurrentPage, mRecordBitmapImg);
//        }
        Log.i(TAG, "Current Page = " + mCurrentPage);
        if (action == PRE_IMG) {
            if (mCurrentPage > 1) {
                mCurrentPage -= 1;
            }
        } else if (action == NEXT_IMG) {
            if (mCurrentPage < mPage) {
                mCurrentPage += 1;
            }
        }
        getFormImg(getUrl());
//        if (mCacheImg.containsKey(mCurrentPage)) {
//            Log.i(TAG, "use cache img");
//            Bitmap cache = mCacheImg.get(mCurrentPage);
//            Log.i(TAG , "cache hash code = "+cache.hashCode());
//            mRecordImg.setImageBitmap(cache);
//        } else {
//            Log.i(TAG, "get new page img");
//
//            getFormImg(getUrl());
//        }

        if (mService != null) {
            mService.setCurrentPage(mCurrentPage);
        }
    }

    private void showLoading() {
        Log.i(TAG, "showLoading");
        if (mLoadingDialog != null && mLoadingDialog.isShowing())
            return;
        mLoadingDialog = new LoadingDialog(this, getString(R.string.loading));
        mLoadingDialog.show();
    }

    private void dismissLoading() {
        if (mLoadingDialog != null) {
            Log.i(TAG, "dismissLoading");
            mLoadingDialog.dismiss();
        }
    }

    @Override
    public void onTaskFail() {
        dismissLoading();
    }


    private void getFormImg(String url) {
        try {
            showLoading();
            Glide.with(RecordActivity.this)
                    .load(url)
                    .asBitmap()
                    .transform(new RecordImgTransformation(mContext))
                    .into(mRecordImg);

        } catch (Exception e) {
            Log.i(TAG, "load bitmap ex=" + e.getMessage());
            e.printStackTrace();
        } finally {

        }
    }

    private class RecordImgTransformation extends BitmapTransformation {

        public RecordImgTransformation(Context context) {
            super(context);
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            Log.i(TAG, "transform， outWidth = " + outWidth + "   outHeight = " + outHeight);
            Log.i(TAG, "bitmapp width = " + toTransform.getWidth() + "  height = " + toTransform.getHeight());
            int width = toTransform.getWidth();
            int height = toTransform.getHeight();
            Canvas canvas = new Canvas(toTransform);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            paint.setStrokeWidth(2f);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setDither(true);
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);

            //计算scale
            if (width > height) {
                mScaleX = (float) width / 7920f;
                mScaleY = (float) height / 5600f;
            } else {
                mScaleY = (float) height / 7920f;
                mScaleX = (float) width / 5600f;
            }

            mCurrentItems = getCurrentItems();

            if (mCurrentItems.size() > 0) {
                for (RecordDataItem item : mCurrentItems) {

                    HwData hwData = new Gson().fromJson(item.getHwdata(), new TypeToken<HwData>() {
                    }.getType());

                    //开始将笔迹数据添加到path中，最终在Form img中画出
                    addHwData2Path(hwData);
                }
            }


            canvas.drawPath(mPath, paint);

            mRecordBitmapImg = toTransform;
            ZBformApplication.sBlePenManager.setDrawView(mRecordImg, mRecordBitmapImg, mRecordBitmapImg.getWidth(), mRecordBitmapImg.getHeight());
            mService.startDraw();

            mPath.reset();
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissLoading();
                }
            });
            return toTransform;

        }

        public void addHwData2Path(HwData hwData) {
            Point lastP;
            boolean firstP = true;
            for (Point p : hwData.getD()) {
                lastP = p;
                if (firstP) {
                    mPath.moveTo(p.getX() * mScaleX, p.getY() * mScaleY);
                    firstP = false;
                } else {
                    mPath.cubicTo(lastP.getX() * mScaleX, lastP.getY() * mScaleY, ((lastP.getX() + p.getX()) / 2) * mScaleX, ((lastP.getY() + p.getY()) / 2) * mScaleY, p.getX() * mScaleX, p.getY() * mScaleY);
                }
            }
        }

        @Override
        public String getId() {
            return "com.zbform.penform.RecordImgTransformation";
        }
    }

    private FormTask.OnFormTaskListener mFormTaskListener = new FormTask.OnFormTaskListener() {

        @Override
        public void onStartGet() {
            showLoading();
        }

        @Override
        public void onGetSuccess(FormInfo form) {
            mFormInfo = form;
            mPageAddress = mFormInfo.results[0].getRinit();

            Log.i(TAG, "set page address to service: " + mPageAddress);
            mService.setCurrentPageAddress(mPageAddress);
            mService.setDrawFormInfo(mFormInfo, mRecordId);
            mTask.getRecord();
        }

        @Override
        public void onGetFail() {
            dismissLoading();
        }

        @Override
        public void onCancelled() {

        }
    };
}
