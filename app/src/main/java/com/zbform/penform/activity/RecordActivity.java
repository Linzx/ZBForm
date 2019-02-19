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
import android.os.AsyncTask;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zbform.penform.R;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.blepen.ZBFormBlePenManager;
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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class RecordActivity extends BaseActivity implements RecordTask.OnTaskListener, ZBFormBlePenManager.IBlePenDrawCallBack {

    public static final String TAG = RecordActivity.class.getSimpleName();

    private static final int PRE_IMG = 1;
    private static final int NEXT_IMG = 2;
    private static final int AUTO_IMG = 3;

    private int mCurrentPage = 1;

    private List<RecordInfo.Results> recordResults = new ArrayList<>();
    private List<RecordDataItem> mCurrentItems = new ArrayList<>();
    private RecordTask mTask;
    private String mFormId;
    private String mRecordId;
    private String mRecordCode;
    private String mPageAddress;
    private String mBasePageAddress = null;
    private int mPage;

    private LoadingDialog mLoadingDialog;
    ActionBar mActionBar;
    ImageView mRecordImg;

    Path mPath = new Path();
    float mScaleX = 0.1929f;
    float mScaleY = 0.23457f;

    private Hashtable<Integer, List<HwData>> mCachedDataMap = new Hashtable<>();
    private HwData mHwData = new HwData();
    private Path mCurrentCachedPath = new Path();

    private FormTask mFormTask;
    private FormInfo mFormInfo = null;
    private double mFormHeight = 0;
    private double mFormWidth = 0;

    private Context mContext;
    private ZBFormService mService;

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

        ZBformApplication.sBlePenManager.setBlePenDrawCallback(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            mService.setIsRecordDraw(false);
            mService.stopDraw();
        }
        unbindService(conn);
        dismissLoading();
        ZBformApplication.sBlePenManager.removeBlePenDrawCallback(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onresume, bind service: zbform service");
        Intent intent = new Intent(this, ZBFormService.class);
        bindService(intent, conn, Service.BIND_AUTO_CREATE);

        if (mService != null) {
            if (mPageAddress != null && mFormInfo != null && mRecordId != null) {
                mService.setCurrentPageAddress(mPageAddress);
                mService.setIsRecordDraw(true);
                mService.setDrawFormInfo(mFormInfo, mRecordId);
                mService.setCurrentPage(1);
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
        mActionBar.setTitle("");
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

        MenuItem recognize = menu.findItem(R.id.img_form_recognize);
        MenuItem data = menu.findItem(R.id.img_form_data);
        MenuItem pre = menu.findItem(R.id.img_pre);
        MenuItem next = menu.findItem(R.id.img_next);

        recognize.setVisible(true);
        data.setVisible(true);

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
            case R.id.img_form_recognize:

                return true;
            case R.id.img_form_data:
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

    @Override
    public void onTaskFail() {
        dismissLoading();
    }

    private String getUrl() {
        if (mCurrentPage < 0 || mCurrentPage > mPage) {
            mCurrentPage = 1;
        }
        return ApiAddress.getFormImgUri(ZBformApplication.getmLoginUserId(),
                ZBformApplication.getmLoginUserId(), mFormId, mCurrentPage);
    }

    private void switchPages(int action) {
        // 默认自动跳转的不会超出页数范围
        if (action == PRE_IMG && mCurrentPage == 1) {
            Toast.makeText(this, this.getResources().getString(R.string.toast_already_first), Toast.LENGTH_SHORT).show();
            return;
        }
        if (action == NEXT_IMG && mCurrentPage == mPage) {
            Toast.makeText(this, this.getResources().getString(R.string.toast_already_last), Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i(TAG, "page size = " + mPage);
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

        mPageAddress = computeCurrentPageAddress(action);
        Log.i(TAG, "new page address =" + mPageAddress);
        if (mService != null) {
            mService.setCurrentPage(mCurrentPage);
            mService.setCurrentPageAddress(mPageAddress);
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

    private void getFormImg(String url) {
        try {
            showLoading();
            Glide.with(RecordActivity.this)
                    .load(url)
                    .asBitmap()
                    .skipMemoryCache(true)
                    .listener(new RecordImgRequestListener())
                    .transform(new RecordImgTransformation(mContext))
                    .into(mRecordImg);
        } catch (Exception e) {
            Log.i(TAG, "load bitmap ex=" + e.getMessage());
            e.printStackTrace();
            dismissLoading();
        } finally {

        }
    }

    @Override
    public void onPenDown() {
        mHwData = new HwData();
    }

    @Override
    public void onPenUp() {
        if (mHwData.dList != null && mHwData.dList.size() > 0) {
            mHwData.setD(mHwData.dList.toArray(new Point[mHwData.dList.size()]));

            List<HwData> dataList = new ArrayList<>();
            if (mCachedDataMap.containsKey(mCurrentPage)) {
                dataList = mCachedDataMap.get(mCurrentPage);
            }

            dataList.add(mHwData);
            mCachedDataMap.put(mCurrentPage, dataList);

        }
    }

    @Override
    public void onCoordDraw(String pageAddress, int nX, int nY) {
        mPageAddress = pageAddress;
        int p = computeCurrentPage(mBasePageAddress, mPageAddress);
        if (p > mPage || p < 1) {
            return;
        }
        if (mCurrentPage != p) {
            mCurrentPage = p;
            Log.i(TAG, "onCoordDraw: mCurrentPage = " + mCurrentPage);

            switchPages(AUTO_IMG);
        }

        mHwData.setP(mPageAddress);
        Point point = new Point();
        point.setX(nX);
        point.setY(nY);
        mHwData.dList.add(point);
    }

    @Override
    public void onOffLineCoordDraw(String pageAddress, int nX, int nY) {

    }

    private class RecordImgRequestListener implements RequestListener<String, Bitmap> {
        public RecordImgRequestListener() {
            super();
        }

        @Override
        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissLoading();
                }
            });
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
            Log.i(TAG, "onResourceReady bitmap width = " + resource.getWidth() + "  height = " + resource.getHeight());

            int width = resource.getWidth();
            int height = resource.getHeight();

            Canvas canvas = new Canvas(resource);
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

            mCurrentItems = getCurrentItems();

            if (mCurrentItems.size() > 0) {
                for (RecordDataItem item : mCurrentItems) {

                    HwData hwData = new Gson().fromJson(item.getHwdata(), new TypeToken<HwData>() {
                    }.getType());

                    //开始将笔迹数据添加到path中，最终在Form img中画出
                    addHwData2Path(hwData, mPath);
                }
            }

            getCurrentCachedPath();

            if (!mPath.isEmpty()) {
                canvas.drawPath(mPath, paint);
            }

            if (!mCurrentCachedPath.isEmpty()) {
                canvas.drawPath(mCurrentCachedPath, paint);
            }

            ZBformApplication.sBlePenManager.setDrawView(mRecordImg, resource, (float) mFormWidth, (float) mFormHeight);
            mService.startDraw();
            mPath.reset();
            mCurrentCachedPath.reset();
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissLoading();
                }
            });
            return false;
        }

        private void getCurrentCachedPath() {
            if (mCachedDataMap.containsKey(mCurrentPage)) {
                List<HwData> dataList = mCachedDataMap.get(mCurrentPage);
                for (HwData hwData : dataList) {
                    addHwData2Path(hwData, mCurrentCachedPath);
                }
            }
        }

        public void addHwData2Path(HwData hwData, Path path) {
            Point lastP;
            boolean firstP = true;
            for (Point p : hwData.getD()) {
                lastP = p;
                if (firstP) {
                    path.moveTo(p.getX() * mScaleX, p.getY() * mScaleY);
                    firstP = false;
                } else {
                    path.cubicTo(lastP.getX() * mScaleX, lastP.getY() * mScaleY, ((lastP.getX() + p.getX()) / 2) * mScaleX, ((lastP.getY() + p.getY()) / 2) * mScaleY, p.getX() * mScaleX, p.getY() * mScaleY);
                }
            }
        }

        private List<RecordDataItem> getCurrentItems() {
            if (mPage == 1 && recordResults != null && recordResults.size() > 0) {
                return Arrays.asList(recordResults.get(0).getItems());
            }
            List<RecordDataItem> itemList = new ArrayList<>();
            if (recordResults.size() > 0) {
                RecordDataItem[] items = recordResults.get(0).getItems();
                for (RecordDataItem item : items) {

                    HwData hwData = new Gson().fromJson(item.getHwdata(), new TypeToken<HwData>() {
                    }.getType());
                    Log.i(TAG, "hwdata pageaddress = " + hwData.getP() + "   mPageAddress=" + mPageAddress);
                    if (hwData.getP().equals(mPageAddress)) {
                        Log.i(TAG, "add item to list");
                        itemList.add(item);
                    }
                }
            }
            return itemList;
        }
    }


    private class RecordImgTransformation extends BitmapTransformation {

        public RecordImgTransformation(Context context) {
            super(context);
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            Log.i(TAG, "bitmap width = " + toTransform.getWidth() + "  height = " + toTransform.getHeight());
            return toTransform;

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
            mBasePageAddress = mPageAddress;
            Log.i(TAG, "base address = " + mBasePageAddress);

            mFormHeight = convertPageSize(mFormInfo.results[0].getHeigh());
            mFormWidth = convertPageSize(mFormInfo.results[0].getWidth());
            Log.i(TAG, "Form height = " + mFormHeight + "   width=" + mFormWidth);

            Log.i(TAG, "set current pageAddress to service: " + mPageAddress);
            mService.setCurrentPageAddress(mPageAddress);
            mService.setCurrentPage(1);
            mService.setDrawFormInfo(mFormInfo, mRecordId);
            mService.setIsRecordDraw(true);
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

    public double convertPageSize(double x) {
        return x * 10 * 8 / 0.3;
    }

    public double convertPageSizeToMM(double x) {
        return x * 0.3 / 8;
    }

    public int computeCurrentPage(String base, String current) {
        Log.i(TAG, "base = " + base + "  current = " + current);
        int baseNum = 0, currentNum = 0;  // 计算ABC总值
        int page = 0;
        if (mFormHeight != 0 && mFormWidth != 0) {
            double length = mFormHeight > mFormWidth ? mFormHeight : mFormWidth;
            length = convertPageSizeToMM(length);
            Log.i(TAG, "compute page num: length = " + length);
            String[] baseArray = base.split("\\.");
            String[] currentArray = current.split("\\.");

            if (baseArray[0].equals("1713") || (length > 418 && length < 422)) {  // A3纸
                // A3纸范围  1713.A.B.C  0<=B<=52, 0<=C<=107
                baseNum = Integer.parseInt(baseArray[3]) + Integer.parseInt(baseArray[2]) * 108 + Integer.parseInt(baseArray[1]) * 108 * 53;
                currentNum = Integer.parseInt(currentArray[3]) + Integer.parseInt(currentArray[2]) * 108 + Integer.parseInt(currentArray[1]) * 108 * 53;
                page = 1 + currentNum - baseNum;

            } else if (baseArray[0].equals("1536") || (length > 295 && length < 299)) {  // A4纸
                // A4纸范围  1536.A.B.C  0<=B<=72, 0<=C<=107
                baseNum = Integer.parseInt(baseArray[3]) + Integer.parseInt(baseArray[2]) * 108 + Integer.parseInt(baseArray[1]) * 108 * 73;
                currentNum = Integer.parseInt(currentArray[3]) + Integer.parseInt(currentArray[2]) * 108 + Integer.parseInt(currentArray[1]) * 108 * 73;
                page = 1 + currentNum - baseNum;
            }
        }

        return page;
    }

    public String computeCurrentPageAddress(int action) {
        String[] addressArray = mPageAddress.split("\\.");
        Log.i(TAG, "current page address = " + mPageAddress);

        int a = Integer.parseInt(addressArray[1]);
        int b = Integer.parseInt(addressArray[2]);
        int c = Integer.parseInt(addressArray[3]);

        int bMax = 0;
        if (addressArray[0].equals("1713")) {
            bMax = 53;
        } else if (addressArray[0].equals("1536")) {
            bMax = 73;
        }

        int all = a * 108 * bMax + b * 108 + c;

        if (action == NEXT_IMG) {
            all += 1;
        } else if (action == PRE_IMG) {
            all -= 1;
        }

        c = all % 108;
        b = (all / 108) % bMax;
        a = all / (108 * bMax);
        return addressArray[0] + "." + String.valueOf(a) + "." + String.valueOf(b) + "." + String.valueOf(c);
    }
}
