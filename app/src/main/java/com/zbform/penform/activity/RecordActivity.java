package com.zbform.penform.activity;

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
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import com.zbform.penform.json.RecordDataItem;
import com.zbform.penform.json.RecordInfo;
import com.zbform.penform.json.ZBFormGetRecognizedDataInfo;
import com.zbform.penform.json.ZBFormRecgonizeResultInfo;
import com.zbform.penform.json.ZBFormRecognizedData;
import com.zbform.penform.json.ZBFormRecognizedItem;
import com.zbform.penform.json.ZBFormRecognizedResult;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.services.ZBFormService;
import com.zbform.penform.task.FormTask;
import com.zbform.penform.task.ModifyItemValueTask;
import com.zbform.penform.task.RecordTask;
import com.zbform.penform.task.ZBFormGetRecognizedDataTask;
import com.zbform.penform.task.ZBFormRecognizeTask;
import com.zbform.penform.util.CommonUtils;
import com.zbform.penform.view.CustViewPager;
import com.zbform.penform.view.PageItemView;

import java.util.ArrayList;
import java.util.Arrays;
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
    private static final int MODIFY_UPDATE_ITEM_MSG = 2000;

    private int mCurrentPage = 1;

    private List<RecordInfo.Results> recordResults = new ArrayList<>();
    private List<RecordDataItem> mCurrentItems = new ArrayList<>();
    private List<ZBFormRecognizedResult> mZBFormRecognizedResults = new ArrayList<>();
    private RecordTask mTask;
    private String mFormId;
    private String mRecordId;
    private String mRecordCode;
    private String mPageAddress;
    private String mBasePageAddress = null;
    private int mPage;

    private int mTargetWidth;
    private int mTargetHeight;

    private boolean isRecognized = false;

    private LoadingDialog mLoadingDialog;
    ActionBar mActionBar;
//    ImageView mRecordImg;
    private ImageView mShowItem;
    private ImageView mCusDrawItemView;
    private CustViewPager mRecordViewPager;
    ImageView mItemRecognize;
    DrawerLayout mDrawerLayout;
    private FrameLayout mRoot;
    private ListView mListView;
    private MenuItemAdapter mAdapter;

    private ArrayList<RecordPageHolder> mRecordPageHolderList = new ArrayList<>();
    private HashMap<String, String> mValAddress;
    private DrawItemsTask mDrawItemsTask;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what) {
                case MODIFY_VALUE_MSG:
                    ModifyPostParams postParams = (ModifyPostParams) msg.obj;
                    modifyValue(postParams);
                    break;
                case MODIFY_UPDATE_ITEM_MSG:
                    if (mAdapter != null && mFormInfo != null && mFormInfo.results[0].items.length > 0) {
                        mAdapter.setItem(Arrays.asList(mFormInfo.results[0].items));
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

//    Path mPath = new Path();
//    float mScaleX = 0.1929f;
//    float mScaleY = 0.23457f;

    // 缓存用户新增笔迹数据
    private Hashtable<Integer, List<HwData>> mCachedDataMap = new Hashtable<>();

    // 缓存识别后的表单记录数据
    private Hashtable<Integer, List<ZBFormRecognizedResult>> mCachedZBFormRecognizedResultMap = new Hashtable<>();

    private boolean mUserDraw = false;
    private HwData mHwData = new HwData();
    private Path mCurrentCachedPath = new Path();

    private FormTask mFormTask;
    private FormInfo mFormInfo = null;
    private double mFormHeight = 0;
    private double mFormWidth = 0;

    private Context mContext;
    private ZBFormService mService;
    private RecordPagerAdapter mPageAdapter;
    private boolean mItemShow = false;

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
//        mRecordImg = findViewById(R.id.record_img);
        mListView = findViewById(R.id.id_lv_right_menu);
        mItemRecognize = findViewById(R.id.item_recognize);
        mItemRecognize.setOnClickListener(this);
        mDrawerLayout = findViewById(R.id.fd_record);
        mAdapter = new MenuItemAdapter(mContext);
        LayoutInflater inflater = LayoutInflater.from(this);
        View header = inflater.inflate(R.layout.listitem_recorditem_header_layout, mListView, false);
        mListView.addHeaderView(header);
        mRoot = findViewById(R.id.ll_root);

        mShowItem = findViewById(R.id.show_item);
        mShowItem.setOnClickListener(this);

        mRecordViewPager = findViewById(R.id.record_pager);

        mRecordViewPager.addOnPageChangeListener(mOnPageChangeListener);

        mPageAdapter = new RecordPagerAdapter(this);

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

    private void initViewPagerData() {
        mRecordPageHolderList.clear();
        for (int i = 1; i <= mPage; i++) {
            RecordPageHolder holder = new RecordPageHolder();
            holder.mImgUrl =
                    ApiAddress.getFormImgUri(ZBformApplication.getmLoginUserId(),
                            ZBformApplication.getmLoginUserId(), mFormId, i);

            holder.mHolderPosition = i - 1;
            holder.mPageAddress = mValAddress.get(String.valueOf(i));
            mRecordPageHolderList.add(holder);
        }

        mPageAdapter.setData(mRecordPageHolderList);
        mRecordViewPager.setAdapter(mPageAdapter);
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
//        MenuItem pre = menu.findItem(R.id.img_pre);
//        MenuItem next = menu.findItem(R.id.img_next);

        data.setVisible(true);

//        if (pre != null && next != null) {
//            if (mPage > 1) {
//                pre.setVisible(true);
//                next.setVisible(true);
//            } else {
//                pre.setVisible(false);
//                next.setVisible(false);
//            }
//        }

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
//            case R.id.img_pre:
//                switchPages(PRE_IMG);
//                return true;
//            case R.id.img_next:
//                if (mFormInfo == null) {
//                    mFormTask = new FormTask();
//                    mFormTask.setOnFormTaskListener(mFormTaskListener);
//                    mFormTask.execute(mContext, mFormId);
//                }
//                switchPages(NEXT_IMG);
//                return true;
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
        // 获取Form 表单的图片，准备合成
        initViewPagerData();

        Log.i(TAG, "onTaskSuccess()");
        String state = results.get(0).getRecordRecognizeState();
        if("Y".equals(state)){
            isRecognized = true;
            getZBFormRecognizedData(false);
        }

        dismissLoading();
    }

    @Override
    public void onTaskFail() {
        dismissLoading();
    }

//    private void switchPages(int action) {
//        // 默认自动跳转的不会超出页数范围
//        if (action == PRE_IMG && mCurrentPage == 1) {
//            Toast.makeText(this, this.getResources().getString(R.string.toast_already_first), Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (action == NEXT_IMG && mCurrentPage == mPage) {
//            Toast.makeText(this, this.getResources().getString(R.string.toast_already_last), Toast.LENGTH_SHORT).show();
//            return;
//        }
//        Log.i(TAG, "page size = " + mPage);
//        Log.i(TAG, "Current Page = " + mCurrentPage);
//        if (action == PRE_IMG) {
//            if (mCurrentPage > 1) {
//                mCurrentPage -= 1;
//            }
//        } else if (action == NEXT_IMG) {
//            if (mCurrentPage < mPage) {
//                mCurrentPage += 1;
//            }
//        }
//
//        mPageAddress = computeCurrentPageAddress(action);
//        Log.i(TAG, "new page address =" + mPageAddress);
//        if (mService != null) {
//            mService.setCurrentPageAddress(mPageAddress);
//        }
//        if (mAdapter != null && mFormInfo != null && mFormInfo.results[0].items.length > 0) {
//            mAdapter.setItem(Arrays.asList(mFormInfo.results[0].items));
//            mAdapter.notifyDataSetChanged();
//        }
//        mUserDraw = false;
//    }

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
//        mPageAddress = pageAddress;
//        int p = computeCurrentPage(mBasePageAddress, mPageAddress);
//        if (p > mPage || p < 1) {
//            return;
//        }
//        if (mCurrentPage != p) {
//            mCurrentPage = p;
//            Log.i(TAG, "onCoordDraw: mCurrentPage = " + mCurrentPage);
//
//            switchPages(AUTO_IMG);
//        }

        if (!mPageAddress.equals(pageAddress)){
            mPageAddress = pageAddress;
            indexViewPager();
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
            recognizeZBFormRecordStrokes();
        } else if(v.getId() == R.id.show_item){
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
                RecordPageHolder holder = getCurrentHolder();
                if (holder != null) {
                    showItemsReF(holder);
                }
                mItemShow = true;
            }
        }
    }

    private void indexViewPager(){
        for (RecordPageHolder holder : mRecordPageHolderList){
            if (holder.mPageAddress.equals(mPageAddress)){

                mRecordViewPager.setCurrentItem(holder.mHolderPosition, false);
                break;
            }
        }
    }

    private void recognizeZBFormRecordStrokes(){
        if(isRecognized && !mUserDraw){
            // 已经识别过，并且用户没有新增笔迹，则直接显示已获取数据
            if(mCachedZBFormRecognizedResultMap.containsKey(mCurrentPage)){
                // 已经获取过识别结果，直接使用cached数据
                mAdapter.setZBFormResults(mCachedZBFormRecognizedResultMap.get(mCurrentPage));
                mAdapter.notifyDataSetChanged();
                mDrawerLayout.openDrawer(Gravity.END);
            } else {
                // 还未获取过识别结果，开始获取
                showLoading();
                getZBFormRecognizedData(true);
            }
        } else {
            // 还未识别过，或者有新的数据，从新识别获取
            showLoading();
            recognizeAndGetData();
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
            mValAddress = CommonUtils.findValidateAddress(
                    false, mFormInfo.results[0].getRinit(),
                    mFormInfo.results[0].getPage());

            mBasePageAddress = mFormInfo.results[0].getRinit();
            Log.i(TAG, "base address = " + mBasePageAddress);

            mFormHeight = convertPageSize(mFormInfo.results[0].getHeigh());
            mFormWidth = convertPageSize(mFormInfo.results[0].getWidth());
            Log.i(TAG, "Form height = " + mFormHeight + "   width=" + mFormWidth);

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
            dismissLoading();
        }
    };

    public void getZBFormRecognizedData(boolean open){
        ZBFormGetRecognizedDataTask getRecognizedDataTask = new ZBFormGetRecognizedDataTask(mContext,mRecordId);
        mGetZBFormRecognizedDataListener.setOpenDrawer(open);
        getRecognizedDataTask.setOnZBFormGetRecognizedDataTaskListener(mGetZBFormRecognizedDataListener);
        getRecognizedDataTask.execute();
    }


    private ZBFormGetRecognizedDataTaskListener mGetZBFormRecognizedDataListener = new ZBFormGetRecognizedDataTaskListener();
    private class ZBFormGetRecognizedDataTaskListener implements ZBFormGetRecognizedDataTask.OnZBFormGetRecognizedDataTaskListener {
        public boolean mOpenDrawer = true;

        public void setOpenDrawer(boolean open) {
            mOpenDrawer = open;
        }

        @Override
        public void onStartGet() {

        }

        @Override
        public void onGetSuccess(ZBFormGetRecognizedDataInfo info) {
            mZBFormRecognizedResults.clear();
            mZBFormRecognizedResults.addAll(Arrays.asList(info.getItems()));
            mAdapter.setZBFormResults(mZBFormRecognizedResults);
            mAdapter.notifyDataSetChanged();
            mCachedZBFormRecognizedResultMap.put(mCurrentPage, mZBFormRecognizedResults);
            if (mOpenDrawer) {
                mDrawerLayout.openDrawer(Gravity.END);
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
    }

    public void recognizeAndGetData(){
        ZBFormRecognizeTask zbFormRecognizeTask = new ZBFormRecognizeTask(mContext, mRecordId);
        zbFormRecognizeTask.setOnRecognizeTaskListener(mOnZBFormRecognizeTaskListener);
        zbFormRecognizeTask.execute();
    }

    private ZBFormRecognizeTask.OnZBFormRecognizeTaskListener mOnZBFormRecognizeTaskListener = new ZBFormRecognizeTask.OnZBFormRecognizeTaskListener() {
        @Override
        public void onStartGet() {

        }

        @Override
        public void onGetSuccess(ZBFormRecgonizeResultInfo info) {
            mZBFormRecognizedResults.clear();
            convertZBFormRecognizedData(info, mZBFormRecognizedResults);

            mAdapter.setZBFormResults(mZBFormRecognizedResults);
            mAdapter.notifyDataSetChanged();
            mCachedZBFormRecognizedResultMap.put(mCurrentPage, mZBFormRecognizedResults);
            dismissLoading();
            mDrawerLayout.openDrawer(Gravity.END);
            mUserDraw = false;
            isRecognized = true;
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

    public void convertZBFormRecognizedData(ZBFormRecgonizeResultInfo info, List<ZBFormRecognizedResult> results){
        String resultItemsString = info.getItems();
        Log.i(TAG, "convertZBFormRecognizedData: items: "+resultItemsString);
        Gson gson = new Gson();
        List<ZBFormRecognizedItem> resultList = gson.fromJson(resultItemsString, new TypeToken<List<ZBFormRecognizedItem>>() {}.getType());
        for(ZBFormRecognizedItem item: resultList){
            ZBFormRecognizedResult r = new ZBFormRecognizedResult();
            r.setItemCode(item.getCode());
            Log.i(TAG, "convertZBFormRecognizedData: RecognizedData: "+item.getRecognizedData());

            ZBFormRecognizedData data = gson.fromJson(item.getRecognizedData(), new TypeToken<ZBFormRecognizedData>(){}.getType());

            r.setValue(data.getValue());
            results.add(r);
        }
    }
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
        private List<ZBFormRecognizedResult> mZBFormResults = new ArrayList<>();

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
                if (item != null && item.getPage() == mCurrentPage &&
                        !item.getIdentityFlag().equals("N")) {
                    mItems.add(item);
                }
            }
        }

        public void setZBFormResults(List<ZBFormRecognizedResult> results){
            mZBFormResults = results;
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
            for (ZBFormRecognizedResult r : mZBFormResults) {
                if (r.getItemCode().equals(itemCode)) {
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
                    content.setSelection(contentString.length());
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
            for(int i=0; i< mZBFormRecognizedResults.size(); i++){
                ZBFormRecognizedResult r= mZBFormRecognizedResults.get(i);
                if(r.getItemCode().equals(info.getItemCode())){
                    r.setValue(info.getItemData());
                    mZBFormRecognizedResults.remove(i);
                    mZBFormRecognizedResults.add(r);
                    isFound =true;
                    break;
                }
            }

            // 如果修改的数据之前没有笔迹， 需要从新添加到识别结果中，更新界面。
            if(!isFound){
                ZBFormRecognizedResult r= new ZBFormRecognizedResult();
                r.setItemCode(info.getItemCode());
                r.setValue(info.getItemData());
                mZBFormRecognizedResults.add(r);
            }
            mAdapter.setZBFormResults(mZBFormRecognizedResults);
            mAdapter.notifyDataSetChanged();

            mCachedZBFormRecognizedResultMap.put(mCurrentPage, mZBFormRecognizedResults);
            if(TextUtils.isEmpty(info.getItemData())){
                isRecognized = false;
            }
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

    private RecordPageHolder getCurrentHolder(){
        return mRecordPageHolderList.get(mRecordViewPager.getCurrentItem());
    }

    private void showItemsReF(RecordPageHolder holder) {
        if (mDrawItemsTask == null) {
            mDrawItemsTask = new DrawItemsTask(holder);
        }
        mDrawItemsTask.drawItems();
    }

    private class DrawItemsTask {
        RecordPageHolder mRecordPageHolder;
        Bitmap mDrawItemTarget;
        Canvas mCanvas;
        Paint mPaint;
        float mScaleX = 0.1929f;
        float mScaleY = 0.23457f;


        public DrawItemsTask(RecordPageHolder holder) {
            mRecordPageHolder = holder;
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

                int index = mRecordViewPager.getCurrentItem() +1;
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

                if (mCusDrawItemView == null && mRecordPageHolder.contentView != null) {
                    mCusDrawItemView = new ImageView(RecordActivity.this);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            mRecordPageHolder.contentView.getImageView().getWidth(),
                            mRecordPageHolder.contentView.getImageView().getHeight());
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

    private class RecordPageHolder {
        PageItemView contentView;
        Bitmap formBitmap;

        int mHolderPosition;
        String mImgUrl;
        String mPageAddress;
    }

    public class ImageLoader {
        PageItemView mItemView;
        RecordPageHolder mRecordPageHolder;

        private class RecordImgRequestListener implements RequestListener<String, Bitmap> {
            public RecordImgRequestListener() {
                super();
            }

            @Override
            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                Log.i(TAG, "onResourceReady bitmap width = " + resource.getWidth() + "  height = " + resource.getHeight());
                if (resource != null) {
                    mRecordPageHolder.formBitmap = resource;
                    if (mRecordPageHolder.mHolderPosition == mRecordViewPager.getCurrentItem()) {
                        drawForm(mRecordPageHolder);
                    }
                } else {
                    mRecordPageHolder.formBitmap = null;
                }

                mItemView.dismissLoading();
                return false;
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

        public ImageLoader(RecordPageHolder page) {
            mItemView = page.contentView;
            mRecordPageHolder = page;
        }

        public void startLoader() {
            try {
                Glide.with(RecordActivity.this)
                        .load(mRecordPageHolder.mImgUrl)
                        .asBitmap()
                        .skipMemoryCache(true)
                        .listener(new RecordImgRequestListener())
                        .transform(new RecordImgTransformation(mContext))
                        .into(mItemView.getImageView());
            } catch (Exception e) {
                Log.i(TAG, "bitmap ex=" + e.getMessage());
                e.printStackTrace();
            } finally {

            }
        }
    }

    private void drawForm(RecordPageHolder recordPageHolder) {
        if (recordPageHolder.formBitmap != null &&
                recordPageHolder.contentView != null) {
            mTargetWidth = recordPageHolder.formBitmap.getWidth();
            mTargetHeight = recordPageHolder.formBitmap.getHeight();
//            Log.i(TAG, "onResourceReady W=" + mTargetWidth);
//            Log.i(TAG, "onResourceReady H=" + mTargetHeight);

            ZBformApplication.sBlePenManager.setDrawView(recordPageHolder.contentView.getImageView(),
                    recordPageHolder.formBitmap);
            if (mFormHeight > 0 && mFormWidth > 0) {
                ZBformApplication.sBlePenManager.setPaperSize((float) mFormWidth, (float) mFormHeight);
            }
            if (mValAddress != null) {
                String index = String.valueOf(recordPageHolder.mHolderPosition + 1);
                mPageAddress = mValAddress.get(index);
                Log.i(TAG, "switch page=" + mPageAddress);
            }

            if (mService != null) {
                mService.setCurrentPageAddress(mPageAddress);
            }
            mService.startDraw();

            new DrawHwDataTask(recordPageHolder).execute();
            if (mItemShow) {
                showItemsReF(recordPageHolder);
            }
        } else {
            Log.i(TAG, "holder resource null");
        }
    }

    private class DrawHwDataTask extends AsyncTask<Void, Void, Void> {

        private Bitmap mDrawTarget;
        ImageView imageView;
        Path mPath = new Path();
        float mScaleX = 0.1929f;
        float mScaleY = 0.23457f;

        public DrawHwDataTask(Bitmap target) {
            mDrawTarget = target;
        }

        public DrawHwDataTask(RecordPageHolder holder) {
            mDrawTarget = holder.formBitmap;
            imageView = holder.contentView.getImageView();
        }
        @Override
        protected Void doInBackground(Void... params) {
//        public void execute(){
            Canvas canvas = new Canvas(mDrawTarget);
            int width = mDrawTarget.getWidth();
            int height = mDrawTarget.getHeight();

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
            mPath.reset();
            mCurrentCachedPath.reset();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            imageView.setImageBitmap(mDrawTarget);
            mPath.reset();
            mCurrentCachedPath.reset();
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

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            Log.i(TAG,"scrolled");
        }

        @Override
        public void onPageSelected(int position) {
            Log.i(TAG, "onPageSelected=" + position);
//            setUpPageTitle(position + 1);
            mCurrentPage = position + 1;
            RecordPageHolder pageHolder = mRecordPageHolderList.get(position);
            if (pageHolder != null) {
                drawForm(pageHolder);
            }
            Message msg = mHandler.obtainMessage(MODIFY_UPDATE_ITEM_MSG);
            mHandler.sendMessage(msg);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            Log.i("whd","onPageScrollStateChanged");
        }
    };

    private class RecordPagerAdapter extends PagerAdapter {
        ArrayList<RecordPageHolder> mList;
        Context mContext;
        LayoutInflater mInflater;

        RecordPagerAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
        }

        public void setData(ArrayList<RecordPageHolder> list) {
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

            RecordPageHolder pageHolder = mList.get(position);
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
                RecordPageHolder pageHolder = mList.get(position);
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
//            if (mForceRefresh) {
//                return POSITION_NONE;
//            } else {
                return super.getItemPosition(object);
//            }
        }
    }

}
