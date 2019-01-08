package com.zbform.penform.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zbform.penform.R;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.json.HwData;
import com.zbform.penform.json.Point;
import com.zbform.penform.json.RecordDataItem;
import com.zbform.penform.json.RecordInfo;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.task.RecordTask;

import java.util.ArrayList;
import java.util.List;

public class RecordActivity extends BaseActivity implements RecordTask.OnTaskListener {

    public static final String TAG = RecordActivity.class.getSimpleName();

    private static List<RecordInfo.Results> recordResults = new ArrayList<>();
    private RecordTask mTask;
    private String mFormId;
    private String mRecordId;
    private String mRecordCode;
    private int mPage;

    ProgressBar mProgressBar;
    ActionBar mActionBar;
    ImageView mRecordImg;

    Bitmap mFormImg;
    Path mPath = new Path();
    float mScaleX = 0.1929f;
    float mScaleY = 0.23457f;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_record);

        mContext = this;
        mRecordImg = findViewById(R.id.record_img);
        mProgressBar = findViewById(R.id.progress_img);

        mFormId = getIntent().getStringExtra("formId");
        mRecordId = getIntent().getStringExtra("recordId");
        mPage = getIntent().getIntExtra("page", 0);
        mRecordCode = getIntent().getStringExtra("recordCode");
        Log.i(TAG, "form id = " + mFormId + "  record id = " + mRecordId + "  page = " + mPage + "  record code = "+mRecordCode);

        setToolBar();

        initData();
    }
    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(mRecordCode);
    }


    private void initData() {
        // 获取对应的表单记录数据
        mTask = new RecordTask(mContext, mFormId, mRecordId);
        mTask.setTaskListener(this);
        mTask.getRecord();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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
        String url = ApiAddress.getFormImgUri(ZBformApplication.getmLoginUserId(), ZBformApplication.getmLoginUserKey(), mFormId, mPage);
        getFormImg(url);

    }

    @Override
    public void onTaskFail() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }


    private void getFormImg(String url) {
        try {
            Glide.with(RecordActivity.this)
                    .load(url)
                    .asBitmap()
                    .transform(new RecordImgTransformation(mContext))
                    .into(mRecordImg);
            mProgressBar.setVisibility(View.INVISIBLE);
            //.get();
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
            Log.i(TAG, "bitmapp width = "+toTransform.getWidth() +"  height = "+toTransform.getHeight());
            int width = toTransform.getWidth();
            int height = toTransform.getHeight();
            Canvas canvas = new Canvas(toTransform);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            paint.setStrokeWidth(1.5f);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setDither(true);
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);

            //计算scale
            if(width > height) {
                mScaleX = (float)width / 7920f;
                mScaleY = (float)height / 5600f;
            } else {
                mScaleY = (float)height / 7920f;
                mScaleX = (float)width / 5600f;
            }

            if (recordResults.size() > 0) {
                RecordDataItem[] items = recordResults.get(0).getItems();
                for (RecordDataItem item : items) {

                    HwData hwData = new Gson().fromJson(item.getHwdata(), new TypeToken<HwData>() {
                    }.getType());

                    //开始将笔迹数据添加到path中，最终在Form img中画出
                    addHwData2Path(hwData);
                }
            }


            canvas.drawPath(mPath, paint);
            mPath.reset();
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
}
