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
import com.zbform.penform.json.FormInfo;
import com.zbform.penform.json.FormItem;

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
            while(true && !mStopRecordCoord) {
                try {
                    DrawCoord coord = mCoordQueue.take();
                    if (coord.x == -1000) {
                        continue;
                    }
                    ZBStrokeEntity stroke = new ZBStrokeEntity();
                    stroke.setUserid(ZBformApplication.getmLoginUserId());
                    stroke.setFormid(mDrawFormInfo.results[0].getUuid());

                    stroke.setRecordid(mRecordId);

                    String itemId = "";//findFormRecordId(coord.x,coord.y);
                    stroke.setItemid(itemId);

                    stroke.setIsupload(false);
                    stroke.setX(coord.x);
                    stroke.setY(coord.y);

                    ZBformApplication.mDB.saveBindingId(stroke);

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
