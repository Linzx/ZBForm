package com.zbform.penform.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.blepen.ZBFormBlePenManager;
import com.zbform.penform.db.ZBStrokeEntity;
import com.zbform.penform.json.FormInfo;
import com.zbform.penform.json.FormItem;
import com.zbform.penform.json.HwData;
import com.zbform.penform.json.Point;
import com.zbform.penform.json.ZBFormInnerItem;
import com.zbform.penform.net.ApiAddress;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ZBFormService extends Service {
    private static final String TAG = "ZBFormService";

    private FormInfo mDrawFormInfo;
    private String mRecordId;
    private boolean mStopRecordCoord = false;


    private LocalBinder binder = new LocalBinder();
    public class LocalBinder extends Binder {
        public ZBFormService getService() {
            return ZBFormService.this;
        }
    }

    private LinkedBlockingQueue<HwData> mCoordQueue = new LinkedBlockingQueue<HwData>();

    private PenDrawCallBack mIBlePenDrawCallBack  = new PenDrawCallBack();
     private class PenDrawCallBack implements ZBFormBlePenManager.IBlePenDrawCallBack {
         HwData mStroke = null;//一个笔画
         long mBeginTime;
                @Override
                public void onPenDown() {
                    //开始一个笔画
                    mStroke = new HwData();

                    mStroke.setT(ApiAddress.getTimeStamp());
                    mBeginTime = System.currentTimeMillis();
                }

                @Override
                public void onPenUp() {
                    //penup 一个笔画结束，开始存储
                    long endTime = System.currentTimeMillis();
                    int c = (int)(endTime - mBeginTime);//一个笔画的耗时
                    mStroke.setC(c);
                    try {
                        mCoordQueue.put(mStroke);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCoordDraw(String pageAddress, int nX, int nY) {
                    mStroke.setP(pageAddress);
                    Point point = new Point();
                    point.setX(nX);
                    point.setY(nY);
                    if (mStroke != null){
                        mStroke.dList.add(point);
                    }
                }

                @Override
                public void onOffLineCoordDraw(String pageAddress, int nX, int nY) {

                }
            }

    private class CoodDBTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... integers) {
            while (true) {
                try {
                    if (mStopRecordCoord && mCoordQueue.isEmpty()) {
                        break;
                    }
                    HwData stroke = mCoordQueue.take();
                    if (stroke.getC() == -1000) {
                        continue;
                    }
                    for (Point point : stroke.dList) {
                        ZBStrokeEntity strokeEntity = new ZBStrokeEntity();
                        strokeEntity.setUserid(ZBformApplication.getmLoginUserId());
                        strokeEntity.setFormid(mDrawFormInfo.results[0].getUuid());
                        strokeEntity.setRecordid(mRecordId);

                        String itemId = findFormRecordId(point.getX(), point.getY());
                        //笔迹不在item内，记录为page * -1
                        if(TextUtils.isEmpty(itemId)){
                            itemId = String.valueOf(-1 * mDrawFormInfo.results[0].getPage());
                        }
                        strokeEntity.setItemid(itemId);

                        strokeEntity.setIsupload(false);
                        strokeEntity.setX(point.getX());
                        strokeEntity.setY(point.getY());
                        strokeEntity.setTagtime(stroke.getT());
                        strokeEntity.setPageAddress(stroke.getP());
                        strokeEntity.setStrokeTime(stroke.getC());

                        ZBformApplication.mDB.saveBindingId(strokeEntity);
                    }

                    Log.i(TAG, "save db end");

//                    List<ZBStrokeEntity> test1 = ZBformApplication.mDB.findAll(Selector.from(ZBStrokeEntity.class));
//                    if (test1 == null) {
//                        Log.i(TAG, "test1 null");
//                    } else {
//                        Log.i(TAG, "test1 is" + test1.size());
//                        Log.i(TAG, "test1 is" + test1.get(0).getFormid());
//                    }

                } catch (DbException dbe) {
                    dbe.printStackTrace();
                    return false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }
    }

    /// 计算坐标点是哪个formitem
    private String findFormRecordId(int x, int y){

        String id = "";
        for (int i = 0;i < mDrawFormInfo.results[0].items.length; i++) {
            FormItem item = mDrawFormInfo.results[0].items[i];

            double xoff = x * 0.3 / 8 / 10;
            double yoff = y * 0.3 / 8 / 10;
            Log.i(TAG,"xoff="+xoff);
            Log.i(TAG,"yoff"+yoff);
            Log.i(TAG,"item.getLocaX()="+item.getLocaX());
            Log.i(TAG,"item.getLocaY()="+item.getLocaY());
            Log.i(TAG,"LocaX()+LocaW="+(item.getLocaX() + item.getLocaW()));
            Log.i(TAG,"LocaY()+LocaH()="+(item.getLocaY() + item.getLocaH()));

            if(xoff >= item.getLocaX() &&
                    yoff >= item.getLocaY() &&
                    xoff <= (item.getLocaX() + item.getLocaW()) &&
                    yoff <= (item.getLocaY() + item.getLocaH())){
                id = item.getItem();
                break;
            }

        }
        return id;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "ZBFormService onCreate");
        ZBformApplication.sBlePenManager.setIBlePenDrawCallBack(mIBlePenDrawCallBack);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "ZBFormService onStartCommand1");

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setDrawFormInfo(FormInfo info, String recordId){
        mDrawFormInfo = info;
        mRecordId =recordId;
    }

    public void startRecordCoord(){
        new CoodDBTask().execute();
    }

    public void stopRecordCoord(){

        mStopRecordCoord = true;
        HwData coord = new HwData();
        coord.setC(-1000);
        try {
            mCoordQueue.put(coord);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
