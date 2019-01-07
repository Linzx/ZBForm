package com.zbform.penform.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

public class RecordFragment extends BaseFragment implements RecordTask.OnTaskListener {

    public static final String TAG = RecordFragment.class.getSimpleName();

    private static List<RecordInfo.Results> recordResults = new ArrayList<>();
    private RecordTask mTask;
    private String mFormId;
    private String mRecordId;
    private int mPage;

    ImageView mRecordImg;

    Bitmap mFormImg;
    static Path mPath = new Path();
    static float mScaleX = 0.1929f;
    static float mScaleY = 0.23457f;

    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mFormId = (String) getArguments().get("formId");
        mRecordId = (String) getArguments().get("recordId");
        mPage = (int) getArguments().get("page");
        Log.i(TAG, "form id = " + mFormId + "  record id = " + mRecordId + "  page = " + mPage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        initView(view);
        initData();
        return view;
    }

    private void getFormImg(String url) {
        try {
            Glide.with(mContext)
                    .load(url)
                    .asBitmap()
                    .transform(new RecordImgTransformation(mContext))
                    .into(mRecordImg);
            //.get();
        } catch (Exception e) {
            Log.i(TAG, "load bitmap ex=" + e.getMessage());
            e.printStackTrace();
        } finally {

        }
    }

    private void initView(View view) {
        mRecordImg = view.findViewById(R.id.record_img);
    }

    private void initData() {
        // 获取对应的表单记录数据
        mTask = new RecordTask(mContext, mFormId, mRecordId);
        mTask.setTaskListener(this);
        mTask.getRecord();
    }

    @Override
    public void onTaskStart() {

    }

    @Override
    public void onTaskSuccess(List<RecordInfo.Results> results) {
        recordResults = results;
        Log.i(TAG, "onTaskSuccess()");
        if (recordResults.size() > 0) {
            RecordDataItem[] items = recordResults.get(0).getItems();
            for (RecordDataItem item : items) {

                HwData hwData = new Gson().fromJson(item.getHwdata(), new TypeToken<HwData>() {
                }.getType());

                Log.i(TAG, hwData.toString());
                //开始将笔迹数据添加到path中，最终在Form img中画出
                addHwData2Path(hwData);
            }
        }

        // 获取Form 表单的图片，准备合成
        String url = ApiAddress.getFormImgUri(ZBformApplication.getmLoginUserId(), ZBformApplication.getmLoginUserKey(), mFormId, mPage);
        getFormImg(url);
    }

    @Override
    public void onTaskFail() {

    }

    private void addHwData2Path(HwData hwData) {
        Log.i(TAG, "addHwData2Path: " + mPath.toString());

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

    private static class RecordImgTransformation extends BitmapTransformation {

        public RecordImgTransformation(Context context) {
            super(context);
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            Log.i(TAG, "transform， outWidth = " + outWidth + "   outHeight = " + outHeight);
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


            canvas.drawPath(mPath, paint);
            mPath.reset();
            return toTransform;

        }

        @Override
        public String getId() {
            return "com.zbform.penform.RecordImgTransformation";
        }
    }
}
