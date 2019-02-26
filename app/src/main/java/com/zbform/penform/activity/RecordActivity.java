package com.zbform.penform.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.zbform.penform.json.ModifyPostParams;
import com.zbform.penform.json.Point;
import com.zbform.penform.json.RecognizeItem;
import com.zbform.penform.json.RecognizeResultInfo;
import com.zbform.penform.json.RecordDataItem;
import com.zbform.penform.json.RecordInfo;
import com.zbform.penform.json.Result;
import com.zbform.penform.json.ResultData;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.services.ZBFormService;
import com.zbform.penform.task.FormTask;
import com.zbform.penform.task.ModifyItemValueTask;
import com.zbform.penform.task.RecognizeTask;
import com.zbform.penform.task.RecordTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class RecordActivity extends BaseActivity implements RecordTask.OnTaskListener,
        ZBFormBlePenManager.IBlePenDrawCallBack, View.OnClickListener {

    public static final String TAG = RecordActivity.class.getSimpleName();

    private static final int PRE_IMG = 1;
    private static final int NEXT_IMG = 2;
    private static final int AUTO_IMG = 3;

    private static final int MODIFY_VALUE_MSG = 1000;

    private int mCurrentPage = 1;

    private List<RecordInfo.Results> recordResults = new ArrayList<>();
    private List<RecordDataItem> mCurrentItems = new ArrayList<>();
    private ResultData mResultData;
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
    ImageView mItemRecognize;
    DrawerLayout mDrawerLayout;
    private ListView mListView;
    private MenuItemAdapter mAdapter;


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case MODIFY_VALUE_MSG:
                    ModifyPostParams postParams = (ModifyPostParams)msg.obj;
                    modifyValue(postParams);
                    break;
            }
        }
    };

    Path mPath = new Path();
    float mScaleX = 0.1929f;
    float mScaleY = 0.23457f;

    private Hashtable<Integer, List<HwData>> mCachedDataMap = new Hashtable<>();
    private Hashtable<Integer, List<Result>> mCachedRecognizedResultMap = new Hashtable<>();
    private boolean mUserDraw = false;
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
        mListView = findViewById(R.id.id_lv_right_menu);
        mItemRecognize = findViewById(R.id.item_recognize);
        mItemRecognize.setOnClickListener(this);
        mDrawerLayout = findViewById(R.id.fd_record);
        mAdapter = new MenuItemAdapter(mContext);
        LayoutInflater inflater = LayoutInflater.from(this);
        View header = inflater.inflate(R.layout.listitem_recorditem_header_layout, mListView, false);
        mListView.addHeaderView(header);

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

    private void toggleRightSliding() {
        if (mDrawerLayout.isDrawerOpen(Gravity.END)) {
            mDrawerLayout.closeDrawer(Gravity.END);
        } else {
            mDrawerLayout.openDrawer(Gravity.END);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_page, menu);

        MenuItem data = menu.findItem(R.id.img_form_data);
        MenuItem pre = menu.findItem(R.id.img_pre);
        MenuItem next = menu.findItem(R.id.img_next);

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
            case R.id.img_form_data:
                toggleRightSliding();
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
        if (mAdapter != null && mFormInfo != null && mFormInfo.results[0].items.length > 0) {
            mAdapter.setItem(Arrays.asList(mFormInfo.results[0].items));
            mAdapter.notifyDataSetChanged();
        }
        mUserDraw = false;
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
            mUserDraw = true;
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.item_recognize) {
            recognizeStrokes();
        }
    }

    private void recognizeStrokes() {
        if (mCachedRecognizedResultMap.containsKey(mCurrentPage) && !mUserDraw) {
            Log.i(TAG, "Open drawer display directly.");
            // 已经缓存识别后的数据，并且用户没有重新书写新的笔迹，直接展示数据
            mDrawerLayout.openDrawer(Gravity.END);
            return;
        }

        // 1. 下载的record 里原有的笔迹数据识别
        //    服务器下载的笔迹数据格式是多个RecordDataItem，但可能属于相同的item，这些笔迹需要放在一起识别
        // 2. 用户用笔实时写的笔迹数据识别
        //    用户实时写的笔迹数据是存放在mCachedDataMap, 是一个pageNumber到 HwData List的map
        //    这些 HwData是对应于一个笔画，需要将他们归类到相同的item里一起提交给服务器进行识别
        // 最终提交的数据是按照item作为笔迹集合进行识别

        //这个过程可能比较长，需要一个loading的对话框
        showLoading();

        // 1. 先处理服务器下载的笔迹数据
        // 所有的items 集合
        List<RecognizeItem> itemList = new ArrayList<>();

        // 以 item code 为key， 将不同的 recognize item 集中在一起
        Hashtable<String, RecognizeItem> recognizeItemsMap = new Hashtable<>();

        Log.i(TAG, "====Start collect stroke download from server.====");

        // 先查询当前 page 的从服务器下载所有items
        for (RecordDataItem item : mCurrentItems) {

            String itemCode = item.getItemcode();
            String type = null;
            HwData hwData = new Gson().fromJson(item.getHwdata(), new TypeToken<HwData>() {
            }.getType());

            // 从 form 里 items 列表中找到对应item的 type
            for (FormItem formItem : mFormInfo.results[0].getItems()) {
                if (formItem.getItem().equals(item.itemcode)) {
                    type = formItem.getType();
                    break;
                }
            }

            Log.i(TAG, "Recognize: itemcode = " + itemCode + "  type = " + type);
            putData2ItemsMap(itemCode, type, hwData, recognizeItemsMap);
        }

        // 2. 处理用户实时写的笔迹
        Log.i(TAG, "====Start collect new stroke draw by user.====");
        if (mCachedDataMap.containsKey(mCurrentPage)) {
            List<HwData> hwDataList = mCachedDataMap.get(mCurrentPage);

            // 先要确定hwdata 笔迹属于哪个item
            for (HwData d : hwDataList) {

                String typeNew = "";

                Point[] points = d.getD();
                // 有可能存在笔迹中的部分坐标在其他item里，所以需要寻找某个item 中包含该笔迹最多坐标数目
                HashMap<String, Integer> itemCodeMap = new HashMap<>();
                int maxNum = 0;
                String maxNumCode = "";
                for (Point p : points) {
                    int x = p.getX();
                    int y = p.getY();
                    String id = "";
                    String typeInForm = "";
                    for (int i = 0; i < mFormInfo.results[0].items.length; i++) {
                        FormItem item = mFormInfo.results[0].items[i];

                        double xoff = (double) x * 0.3 / 8d / 10d;
                        double yoff = (double) y * 0.3 / 8d / 10d;

                        if (xoff >= item.getLocaX() &&
                                yoff >= item.getLocaY() &&
                                xoff <= (item.getLocaX() + item.getLocaW()) &&
                                yoff <= (item.getLocaY() + item.getLocaH())) {
                            id = item.getItem();
                            typeInForm = item.getType();
                            break;
                        }
                    }
                    int num = 0;
                    if (itemCodeMap.containsKey(id)) {
                        num = itemCodeMap.get(id) + 1;
                    } else {
                        num = 1;
                    }
                    itemCodeMap.put(id, num);

                    if (maxNum < num) {
                        maxNum = num;
                        maxNumCode = id;
                        typeNew = typeInForm;
                    }
                }

                // 获取了hwdata 所属item后，放入待识别items map里
                putData2ItemsMap(maxNumCode, typeNew, d, recognizeItemsMap);
            }
        }

        Enumeration<RecognizeItem> itemEnumeration = recognizeItemsMap.elements();
        while (itemEnumeration.hasMoreElements()) {
            itemList.add(itemEnumeration.nextElement());
        }

        if (itemList.size() > 0) {
            RecognizeTask recognizeTask = new RecognizeTask(mContext, mFormId, mRecordId, itemList.toArray(new RecognizeItem[itemList.size()]));
            recognizeTask.setOnFormTaskListener(mRecognizeTaskListener);
            recognizeTask.execute();
        } else {
            dismissLoading();
            Toast.makeText(mContext, R.string.no_recognize_stroke, Toast.LENGTH_LONG).show();
        }

    }


    private void putData2ItemsMap(String itemCode, String type, HwData hwData, Hashtable<String, RecognizeItem> recognizeItemsMap) {
        if (recognizeItemsMap.containsKey(itemCode)) {
            Log.i(TAG, "recognizeItemsMap contains item " + itemCode);
            RecognizeItem inItem = recognizeItemsMap.get(itemCode);

            if (type != null && type.equals(inItem.getType())) {
                inItem.strokeList.add(hwData);
                inItem.setStroke(inItem.strokeList.toArray(new HwData[inItem.strokeList.size()]));
            }
        } else {
            Log.i(TAG, "recognizeItemsMap not contains item " + itemCode);
            RecognizeItem it = new RecognizeItem();
            if (type != null) {
                it.setType(type);
            }
            it.setId(itemCode);
            it.strokeList.add(hwData);
            it.setStroke(it.strokeList.toArray(new HwData[it.strokeList.size()]));
            recognizeItemsMap.put(itemCode, it);
        }
    }

    private String findFormItemId(Point[] points) {
        // 有可能存在笔迹中的部分坐标在其他item里，所以需要寻找某个item 中包含该笔迹最多坐标数目
        HashMap<String, Integer> itemCodeMap = new HashMap<>();
        int maxNum = 0;
        String maxNumCode = "";
        for (Point p : points) {
            int x = p.getX();
            int y = p.getY();
            String id = "";
            for (int i = 0; i < mFormInfo.results[0].items.length; i++) {
                FormItem item = mFormInfo.results[0].items[i];

                double xoff = (double) x * 0.3 / 8d / 10d;
                double yoff = (double) y * 0.3 / 8d / 10d;

                if (xoff >= item.getLocaX() &&
                        yoff >= item.getLocaY() &&
                        xoff <= (item.getLocaX() + item.getLocaW()) &&
                        yoff <= (item.getLocaY() + item.getLocaH())) {
                    id = item.getItem();
                    break;
                }
            }
            int num = 0;
            if (itemCodeMap.containsKey(id)) {
                num = itemCodeMap.get(id) + 1;
            } else {
                num = 1;
            }
            itemCodeMap.put(id, num);

            if (maxNum < num) {
                maxNum = num;
                maxNumCode = id;
            }
        }

        return maxNumCode;
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

    }

    public List<RecordDataItem> getCurrentItems() {
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

            if (mFormInfo.results[0].items != null && mFormInfo.results[0].items.length > 0) {
                mAdapter.setItem(Arrays.asList(mFormInfo.results[0].items));
                mListView.setAdapter(mAdapter);
            }
        }

        @Override
        public void onGetFail() {
            dismissLoading();
        }

        @Override
        public void onCancelled() {

        }
    };

    private RecognizeTask.OnRecognizeTaskListener mRecognizeTaskListener = new RecognizeTask.OnRecognizeTaskListener() {
        @Override
        public void onStartGet() {

        }

        @Override
        public void onGetSuccess(RecognizeResultInfo info) {
            mResultData = info.getData();

            // 设置并展示 item listview 中的各个item值
            if (mResultData.getResult().length > 0) {
                mAdapter.setResults(Arrays.asList(mResultData.getResult()));
                mAdapter.notifyDataSetChanged();
            }

            // 用作缓存
            mCachedRecognizedResultMap.put(mCurrentPage, Arrays.asList(mResultData.getResult()));
            // 每次识别动作后，重置用户是否 draw 的标志
            mUserDraw = false;

            dismissLoading();
            // 展示数据
            mDrawerLayout.openDrawer(Gravity.END);
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

    public class MenuItemAdapter extends ArrayAdapter<FormItem> {
        private LayoutInflater mInflater;
        private Context mContext;
        private List<FormItem> mItems = new ArrayList<>();
        private List<Result> mResults = new ArrayList<>();

        public MenuItemAdapter(Context context) {
            super(context, R.layout.listitem_recorditem_layout);
            mInflater = LayoutInflater.from(context);
            mContext = context;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public FormItem getItem(int position) {
            return mItems.get(position);
        }

        public void setItem(List<FormItem> items) {
            mItems.clear();
            for (FormItem item : items) {
                if (item != null && item.getPage() == mCurrentPage) {
                    mItems.add(item);
                }
            }
        }

        public void setResults(List<Result> results) {
            mResults = results;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FormItem item = mItems.get(position);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listitem_recorditem_layout, parent,
                        false);
            }

            TextView itemName = convertView.findViewById(R.id.item_name);
            itemName.setText(item.getFieldName());

            TextView itemValue = convertView.findViewById(R.id.item_content);
            itemValue.setText("");
            final String itemCode = item.getItem();
            String itemValueString = "";
            for (Result r : mResults) {
                if (r.getId().equals(itemCode)) {
                    itemValueString = r.getValue();
                    itemValue.setText(itemValueString);
                    break;
                }
            }
            final String contentString = itemValueString;

            TextView modify = convertView.findViewById(R.id.modify);
            modify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("whd", "modify click");
                    final View dialogView = mInflater.inflate(R.layout.dialog_item_update, null);
                    AlertDialog dialog = new AlertDialog.Builder(mContext)
                            .setView(dialogView)
                            .setNegativeButton(R.string.dialog_cancle, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton(R.string.lv_menu_modify, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    EditText contentView = dialogView.findViewById(R.id.dialog_content);
                                    Message msg = mHandler.obtainMessage(MODIFY_VALUE_MSG, new ModifyPostParams(mFormId,mRecordId,itemCode,contentView.getText().toString()));

                                    mHandler.sendMessage(msg);
                                    dialog.dismiss();
                                }
                            })
                            .setCancelable(true)
                            .create();

                    TextView name = dialogView.findViewById(R.id.dialog_item_name);
                    EditText content = dialogView.findViewById(R.id.dialog_content);

                    FormItem item = (FormItem) v.getTag();
                    name.setText(item.getFieldName());

                    content.setText(contentString);
                    dialog.show();
                }
            });
            modify.setTag(item);

            return convertView;
        }
    }

    private void modifyValue(ModifyPostParams postParams) {
        Log.i(TAG,"formid = "+mFormId+"   record id = "+mRecordId);
        Log.i(TAG,"itemCode = "+postParams.getItemCode()+"   itemData = "+postParams.getItemData());

        ModifyItemValueTask modifyItemValueTask = new ModifyItemValueTask(mContext, mFormId, mRecordId, postParams.getItemCode(), postParams.getItemData());
        modifyItemValueTask.setOnFormTaskListener(mOnModifyTaskListener);
        modifyItemValueTask.execute();
    }

    ModifyItemValueTask.OnModifyTaskListener mOnModifyTaskListener = new ModifyItemValueTask.OnModifyTaskListener() {
        @Override
        public void onStartGet() {

        }

        @Override
        public void onGetSuccess(ModifyPostParams info) {
            boolean isFound = false;
            for(int i=0; i< mResultData.getResult().length; i++){
                Result r= mResultData.getResult()[i];
                if(r.getId().equals(info.getItemCode())){
                    r.setValue(info.getItemData());
                    mResultData.getResult()[i] = r;
                    isFound =true;
                    break;
                }
            }
            ArrayList<Result> resultList = new ArrayList<>(Arrays.asList(mResultData.getResult()));

            // 如果修改的数据之前没有笔迹， 需要从新添加到识别结果中，更新界面。
            // 注意： 这里没有更新 mResultData
            if(!isFound){
                Result r= new Result();
                r.setId(info.getItemCode());
                r.setValue(info.getItemData());
                resultList.add(r);
            }
            mAdapter.setResults(resultList);
            mAdapter.notifyDataSetChanged();
            Toast.makeText(mContext, R.string.modify_item_value_success, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onGetFail() {
            Toast.makeText(mContext, R.string.modify_item_value_fail, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancelled() {

        }
    };


}
