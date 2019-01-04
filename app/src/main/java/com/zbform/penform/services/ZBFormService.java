package com.zbform.penform.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.blepen.ZBFormBlePenManager;
import com.zbform.penform.db.ZBStrokeEntity;
import com.zbform.penform.db.ZBStrokePointEntity;
import com.zbform.penform.json.FormInfo;
import com.zbform.penform.json.FormItem;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ZBFormService extends Service {
    private static final String TAG = "ZBFormService";

    private FormInfo mDrawFormInfo;
    private boolean mStopCoordDBTask = false;


    private LocalBinder binder = new LocalBinder();
    public class LocalBinder extends Binder {
        public ZBFormService getService() {
            return ZBFormService.this;
        }
    }

    private class DrawCoord{
        int x;
        int y;
        String address;
    }

    private LinkedBlockingQueue<DrawCoord> mCoordQueue = new LinkedBlockingQueue<DrawCoord>();

    private ZBFormBlePenManager.IBlePenDrawCallBack mIBlePenDrawCallBack  =
            new ZBFormBlePenManager.IBlePenDrawCallBack(){

                @Override
                public void onCoordDraw(String pageAddress, int nX, int nY) {
                    DrawCoord coord = new DrawCoord();
                    coord.address = pageAddress;
                    coord.x = nX;
                    coord.y = nY;
                    try {
                        mCoordQueue.put(coord);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onOffLineCoordDraw(String pageAddress, int nX, int nY) {

                }
            };

    private class CoodDBTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... integers) {
            while(true && !mStopCoordDBTask) {
                try {
                    DrawCoord coord = mCoordQueue.take();
                    if (coord.x == -1000) {
                        continue;
                    }
                    ZBStrokeEntity parent = new ZBStrokeEntity();
                    parent.setUserid(ZBformApplication.getmLoginUserId());
                    parent.setFormid("");

                    parent.setRecordid("test1");
                    parent.setItemid("");


                    ZBStrokePointEntity child = new ZBStrokePointEntity();
                    child.setX(coord.x);
                    child.setY(coord.y);
                    child.parent = parent;
                    Log.i(TAG, "save db begin=");

                    ZBformApplication.mDB.saveBindingId(child);

                    Log.i(TAG, "save db end=");

                    List<ZBStrokeEntity> test1 = ZBformApplication.mDB.findAll(Selector.from(ZBStrokeEntity.class));
                    if (test1 == null) {
                        Log.i(TAG, "test1 null");
                    } else {
                        Log.i(TAG, "test1 is" + test1.size());
                        Log.i(TAG, "test1 is" + test1.get(0).getFormid());
                    }

                    List<ZBStrokePointEntity> test = ZBformApplication.mDB.findAll(Selector.from(ZBStrokePointEntity.class));
                    if (test == null) {
                        Log.i(TAG, "test null");
                    } else {
                        Log.i(TAG, "test is" + test.size());
                    }

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

    public void setDrawFormInfo(FormInfo info){
        mDrawFormInfo = info;
    }

    public void stopCoordDBTask(){

        mStopCoordDBTask = true;
        DrawCoord coord = new DrawCoord();
        coord.address = "";
        coord.x = -1000;
        coord.y = -1000;
        try {
            mCoordQueue.put(coord);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
