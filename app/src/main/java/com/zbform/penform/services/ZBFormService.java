package com.zbform.penform.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.activity.FormDrawActivity;
import com.zbform.penform.blepen.ZBFormBlePenManager;
import com.zbform.penform.db.ZBStrokeEntity;
import com.zbform.penform.json.FormInfo;
import com.zbform.penform.json.FormItem;
import com.zbform.penform.json.FormListInfo;
import com.zbform.penform.json.HwData;
import com.zbform.penform.json.Point;
import com.zbform.penform.json.UpLoadStrokeinfo;
import com.zbform.penform.json.ZBFormInnerItem;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.net.ErrorCode;
import com.zbform.penform.net.IZBformNetBeanCallBack;
import com.zbform.penform.net.ZBformNetBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ZBFormService extends Service {
    private static final String TAG = "ZBFormService";

    private static final int UPLOAD_DELAY = 8 * 1000; //8s
    private Context mContext;
    private List<FormListInfo.Results> mFormList;
    private FormInfo mDrawFormInfo;
    private String mRecordId;
    private boolean mStopRecordCoord = true;
    private int mCurrentPage = 1;
    private String mPageAddress ="0.0.0.0";
    private boolean mIsRecordDraw = false;
    //    private Executor mExecutor = Executors.newCachedThreadPool();
    private IntentFilter mIntentFilter;
    private NetworkChangeReceiver mNetworkChangeReceiver;


    private LocalBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public ZBFormService getService() {
            return ZBFormService.this;
        }
    }

    private UpLoadQueryHandler mUpLoadQueryHandler;
    private LinkedBlockingQueue<HwData> mCoordQueue = new LinkedBlockingQueue<HwData>();
    private LinkedBlockingQueue<ZBFormInnerItem> mUpLoadQueue = new LinkedBlockingQueue<ZBFormInnerItem>();

    private PenDrawCallBack mIBlePenDrawCallBack = new PenDrawCallBack();

    private class PenDrawCallBack implements ZBFormBlePenManager.IBlePenDrawCallBack {
        HwData mStroke = null;//一个笔画
        long mBeginTime;

        private class TargetForm{
            FormListInfo.Results mForm;
            int mCurrentPage;
            String mAddress;
        }
        @Override
        public void onPenDown() {
            if (!ZBformApplication.sBlePenManager.getCanDraw()){
                return;
            }
            //开始一个笔画
            mStroke = new HwData();

            mStroke.setT(ApiAddress.getTimeStamp());
            mBeginTime = System.currentTimeMillis();
        }

        @Override
        public void onPenUp() {
            //penup 一个笔画结束，开始存储
            if (!ZBformApplication.sBlePenManager.getCanDraw()){
                return;
            }
            long endTime = System.currentTimeMillis();
            int c = (int) (endTime - mBeginTime);//一个笔画的耗时
            mStroke.setC(c);
            try {
                mCoordQueue.put(mStroke);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCoordDraw(String pageAddress, int nX, int nY) {
            Log.i(TAG, "START ACT onCoordDraw mPageAddress="+mPageAddress);
            Log.i(TAG, "START ACT onCoordDraw pageAddress="+pageAddress);
            if (!TextUtils.isEmpty(pageAddress)) {
                if (!"0.0.0.0".equals(pageAddress) &&
                        !mPageAddress.equals(pageAddress)) {

                    //如果现在是在修改记录界面。不要再识别新的了
                    if (!mIsRecordDraw) {
                        TargetForm form = findPageForm(pageAddress);
                        startPageFormActivity(form);
                    }
                    mPageAddress = pageAddress;
                }
            }
            if (!ZBformApplication.sBlePenManager.getCanDraw()){
                return;
            }
            mStroke.setP(pageAddress);
            Point point = new Point();
            point.setX(nX);
            point.setY(nY);
            if (mStroke != null) {
                mStroke.dList.add(point);
            }
        }

        @Override
        public void onOffLineCoordDraw(String pageAddress, int nX, int nY) {
            if (!ZBformApplication.sBlePenManager.getCanDraw()){
                return;
            }
        }

        private TargetForm findPageForm(String address) {
            FormListInfo.Results formTarget = null;
            TargetForm result = null;
            int page = 1;

            if (mFormList != null && mFormList.size() > 0) {
                for (FormListInfo.Results form : mFormList) {
                    //多页查找其他页地址
                    if (form.getPage() > 1) {
                        HashMap<String, Integer> valAddress = findValidateAddress(form.getRinit(), form.getPage());
                        if (valAddress.containsKey(address)) {
                            formTarget = form;
                            page = valAddress.get(address);
                            break;
                        }
                    } else {
                        if (form.getRinit().equals(address)) {
                            formTarget = form;
                            page = 1;
                            break;
                        }
                    }
                }
            }
            if (formTarget != null) {
                result = new TargetForm();
                result.mForm = formTarget;
                result.mCurrentPage = page;
                result.mAddress = address;
            }

            return result;
        }

        /*
         * address:1536.671.58.6
         *         1536.A  .B .C
         *         A 1536
         *         B 0~72
         *         C 0~107
         */
        private HashMap<String, Integer> findValidateAddress(String address, int pages) {
            HashMap<String, Integer> valAddress = new HashMap<String, Integer>();
            valAddress.put(address,1);
            if (TextUtils.isEmpty(address)) {
                Log.i(TAG, "findValidateAddress null");
                return valAddress;
            }
            Log.i(TAG, "findValidateAddress address="+address);
            String[] addressArray = address.split("\\.");
            if (addressArray == null || addressArray.length < 4) {
                if(address==null){
                    Log.i(TAG, "findValidateAddress null1");
                } else {
                    Log.i(TAG, "findValidateAddress invalid="+addressArray.length);
                }
                return valAddress;
            }

            //key:page value:key

            try {
                int addressStatic = Integer.valueOf(addressArray[0]);
                int addressA = Integer.valueOf(addressArray[1]);
                int addressB = Integer.valueOf(addressArray[2]);
                int addressC = Integer.valueOf(addressArray[3]);

                for (int i = 1; i < pages; i++) {
                    int nextA = addressA;
                    int nextB = addressB;
                    int nextC = addressC + i;
                    if (nextC > 107) {
                        nextB += 1;
                        if (nextB > 72) {
                            nextA += 1;
                            nextB = 0;
                        }
                        nextC = i - 1;
                    }
                    String nextAddress = String.valueOf(addressStatic) + "." +
                            String.valueOf(nextA) + "." +
                            String.valueOf(nextB) + "." +
                            String.valueOf(nextC);
                    valAddress.put(nextAddress, i + 1);

                    Log.i(TAG, "nextAddress=" + nextAddress);
                }
            }catch (Exception e){
                Log.i(TAG, "findValidateAddress e=" + e.getMessage());
            }

            return valAddress;
        }

        private void startPageFormActivity(TargetForm form){
            if (form == null) return;
            Intent intent = new Intent(mContext, FormDrawActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("currentpage", form.mCurrentPage);
            Log.i(TAG, "startPageFormActivity currentpage=" + form.mCurrentPage);
            intent.putExtra("page",form.mForm.getPage());
            intent.putExtra("pageaddress",form.mAddress);
            intent.putExtra("formid",form.mForm.getUuid());
            intent.putExtra("formname",form.mForm.getName().replace(".pdf",""));
            startActivity(intent);
        }
    }

    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectionManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                Log.i(TAG, "networkInfo available");
                //有网络，同步一下本地数据
                new UpLoadStrokeDBQuery().execute();
            } else {
                Log.i(TAG, "no networkInfo");
            }
        }
    }

    private class CoodSaveDBTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... integers) {
            while (true) {
                try {
                    if (mStopRecordCoord && mCoordQueue.isEmpty()) {
                        mCurrentPage = 1;
                        mDrawFormInfo = null;
                        mRecordId = "";
                        Log.i(TAG,"stop!!!!!");
                        break;
                    }
                    HwData stroke = mCoordQueue.take();
                    if (stroke.getC() == -1000) {
                        continue;
                    }
                    for (Point point : stroke.dList) {
                        ZBStrokeEntity strokeEntity = new ZBStrokeEntity();
                        strokeEntity.setUserid(ZBformApplication.getmLoginUserId());
                        try {
                            strokeEntity.setFormid(mDrawFormInfo.results[0].getUuid());
                        } catch(Exception e){
                            continue;
                        }
                        strokeEntity.setRecordid(mRecordId);

                        String itemId = findFormRecordId(point.getX(), point.getY());
                        //笔迹不在item内，记录为page * -1
                        if (TextUtils.isEmpty(itemId)) {
                            itemId = String.valueOf(-1 * mCurrentPage);
                            Log.i(TAG,"findFormRecordId null");
                        }
                        Log.i(TAG,"findFormRecordId id ="+itemId);
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

        /// 计算坐标点是哪个formitem
        private String findFormRecordId(int x, int y) {

            String id = "";
            for (int i = 0; i < mDrawFormInfo.results[0].items.length; i++) {
                FormItem item = mDrawFormInfo.results[0].items[i];

                double xoff = x * 0.3 / 8 / 10;
                double yoff = y * 0.3 / 8 / 10;
//                Log.i(TAG, "xoff=" + xoff);
//                Log.i(TAG, "yoff" + yoff);
//                Log.i(TAG, "item.getLocaX()=" + item.getLocaX());
//                Log.i(TAG, "item.getLocaY()=" + item.getLocaY());
//                Log.i(TAG, "LocaX()+LocaW=" + (item.getLocaX() + item.getLocaW()));
//                Log.i(TAG, "LocaY()+LocaH()=" + (item.getLocaY() + item.getLocaH()));

                if (xoff >= item.getLocaX() &&
                        yoff >= item.getLocaY() &&
                        xoff <= (item.getLocaX() + item.getLocaW()) &&
                        yoff <= (item.getLocaY() + item.getLocaH())) {
                    id = item.getItem();
//                    Log.i(TAG, "form item id: " + id);
                    break;
                }

            }
            return id;
        }
    }

    //    private class UpLoadQueueTask extends Thread{
    private class UpLoadQueueTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
//        @Override
//        public void run(){
            while (true) {
                try {
                    Log.i(TAG, "UpLoadQueueTask LOOP");
                    ZBFormInnerItem item = mUpLoadQueue.take();
                    Log.i(TAG, "UpLoadQueueTask TAKE DONE");

                    new UpLoadStrokeNet(item).execute();

                } catch (InterruptedException e) {
                    Log.i(TAG, "queue ex=" + e.getMessage());
                    e.printStackTrace();
//                    return false;
                }
            }
        }
    }

    public class UpLoadStrokeDBQuery extends AsyncTask<Void, Void, Void> {
        private ArrayList<ZBFormInnerItem> mInnerItems;

        public UpLoadStrokeDBQuery() {
            super();
            mInnerItems = new ArrayList<ZBFormInnerItem>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //db data convert to jason object
            try {
                List<ZBStrokeEntity> strokeUpLoad = ZBformApplication.mDB.
                        findAll(Selector.from(ZBStrokeEntity.class).
                                where("isupload", "=", 0));
                if (strokeUpLoad == null) return null;
                Log.i("UpLoadStrokeDBQuery", "zblen=" + strokeUpLoad.size());

                for (ZBStrokeEntity entity : strokeUpLoad) {
                    ZBFormInnerItem item;
                    item = findInnerItem(entity.formid, entity.recordid, entity.itemid, entity.userid);
                    if (item == null) {
                        item = new ZBFormInnerItem();
                        item.formid = entity.formid;
                        item.id = entity.recordid;
                        item.itemid = entity.itemid;
                        item.userid = entity.userid;
                        mInnerItems.add(item);
                    }

                    HwData data;//一个笔画
                    data = findHwData(item, entity.getTagtime());
                    if (data == null) {
                        data = new HwData();
                        //同一个时间戳代表是一个笔画的记录
                        data.setT(entity.getTagtime());
                        data.setC(entity.getStrokeTime());
                        data.setP(entity.getPageAddress());
                        item.dataList.add(data);
                    }
                    Point point = new Point();
                    point.setX(entity.x);
                    point.setY(entity.y);
                    data.dList.add(point);


                }
                //convert to array for jason
                for (ZBFormInnerItem innerItem : mInnerItems) {
                    if (innerItem.dataList.size() > 0) {
                        innerItem.data = innerItem.dataList.toArray(new HwData[innerItem.dataList.size()]);

                        for (HwData stroke : innerItem.dataList) {
                            if (stroke.dList.size() > 0) {
                                stroke.setD(stroke.dList.toArray(new Point[stroke.dList.size()]));
                            }
                        }
//                        try {
//                            Log.i(TAG, "db put in queue");
//                            mUpLoadQueue.put(innerItem);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }
                }
            } catch (DbException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.i(TAG, "mInnerItems len=" + mInnerItems.size());
            for (ZBFormInnerItem item : mInnerItems) {
                new UpLoadStrokeNet(item).execute();
            }

        }

        private ZBFormInnerItem findInnerItem(String formid, String id, String itemid, String userid) {
            ZBFormInnerItem result = null;
            for (ZBFormInnerItem item : mInnerItems) {
                if (formid.equals(item.formid) &&
                        id.equals(item.id) &&
                        itemid.equals(item.itemid) &&
                        userid.equals(item.userid)) {
                    result = item;
                    break;
                }

            }
            return result;
        }

        private HwData findHwData(ZBFormInnerItem item, String tagtime) {
            HwData result = null;
            for (HwData data : item.dataList) {
                if (tagtime.equals(data.getT())) {
                    result = data;
                    break;
                }

            }
            return result;
        }
    }

    private class UpLoadStrokeNet implements IZBformNetBeanCallBack {
        private ZBFormInnerItem mInnerItem;

        public UpLoadStrokeNet(ZBFormInnerItem innerItem) {
            mInnerItem = innerItem;
        }

        public void execute() {
            Log.i(TAG, "begin net upload");
            ZBformNetBean upLoad = new ZBformNetBean(mContext, ApiAddress.Hwitem_put,
                    HttpRequest.HttpMethod.POST);
            RequestParams params = new RequestParams();
            String signCode = ApiAddress.getSignCode(ZBformApplication.getmLoginUserId() +
                    ZBformApplication.getmLoginUserKey() + ApiAddress.SYSTEM_KEY);
            params.addQueryStringParameter("signcode", Uri.encode(signCode));
            params.addQueryStringParameter("timestamp", Uri.encode(ApiAddress.getTimeStamp()));
            params.addQueryStringParameter("userid", Uri.encode(ZBformApplication.getmLoginUserId()));
            params.addQueryStringParameter("formid", Uri.encode(mInnerItem.getFormid()));
            params.addQueryStringParameter("id", Uri.encode(mInnerItem.getId()));
//
//            String test = mInnerItem.getItemid();
//            if (TextUtils.isEmpty(test)){
//                test = "IZBform-181210896100217";
//            }
            params.addQueryStringParameter("itemid", Uri.encode(mInnerItem.getItemid()));


            Log.i(TAG, "mInnerItems formid=" + mInnerItem.getFormid());
            Log.i(TAG, "mInnerItems id=" + mInnerItem.getId());
            Log.i(TAG, "mInnerItems itemid=" + mInnerItem.getItemid());
            Gson gson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create();

            String json = gson.toJson(mInnerItem.data);
//            Log.i(TAG, "mInnerItems datajson=" + json);

            params.addBodyParameter("data", json);
            upLoad.setNetTaskCallBack(this);
            upLoad.execute(params);
        }


        @Override
        public void onStart() {
        }

        @Override
        public void onCancelled() {
            // TODO Auto-generated method stub
//            Log.i(TAG, "UPLOAD onCancelled");
//            try {
//                Log.i(TAG, "UPLOAD onFailure");
//                mUpLoadQueue.put(mInnerItem);
//            } catch (InterruptedException e1) {
//                e1.printStackTrace();
//            }
        }

        @Override
        public void onLoading(long total, long current, boolean isUploading) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSuccess(ResponseInfo<String> responseInfo) {
            try {
                String resultGson = responseInfo.result;
                Log.i(TAG, "result UPLOAD Gson!!=" + resultGson);
                Gson gson = new Gson();
                UpLoadStrokeinfo upInfo = gson.fromJson(resultGson, new TypeToken<UpLoadStrokeinfo>() {
                }.getType());

                if (upInfo != null && upInfo.header != null
                        && upInfo.results != null && upInfo.results.length > 0) {

                    Log.i(TAG, "errorcode=" + upInfo.header.getErrorCode());
                    if (upInfo.header.getErrorCode().equals(ErrorCode.RESULT_OK)) {

                        //上传成功，删除本地数据
                        new UpdateDBTask(mInnerItem).execute();

                        //上传失败，回压进queue,重试直到成功
                    } else {
//                        Log.i(TAG, "UPLOAD fail");
//                        mUpLoadQueue.put(mInnerItem);
                    }
                } else {
//                    Log.i(TAG, "UPLOAD null");
//                    mUpLoadQueue.put(mInnerItem);
                }
            } catch (Exception e) {
                Log.i(TAG, "upload e=" + e.getMessage());
//                try {
//                    mUpLoadQueue.put(mInnerItem);
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
//                }
            }
        }

        @Override
        public void onFailure(HttpException error, String msg) {
//            try {
//                Log.i(TAG, "UPLOAD onFailure");
//                mUpLoadQueue.put(mInnerItem);
//            } catch (InterruptedException e1) {
//                e1.printStackTrace();
//            }
        }
    }

    private class UpdateDBTask extends AsyncTask<Void, Void, Void> {
        private ZBFormInnerItem mInnerItem;

        public UpdateDBTask(ZBFormInnerItem item) {
            super();
            mInnerItem = item;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (HwData data : mInnerItem.dataList) {
                WhereBuilder whereBuilder = WhereBuilder.b();
                whereBuilder.and("userid", "=", mInnerItem.getUserid())
                        .and("formid", "=", mInnerItem.getFormid())
                        .and("recordid", "=", mInnerItem.getId())
                        .and("itemid", "=", mInnerItem.getItemid())
                        .and("tagtime", "=", data.getT())
                        .and("pageAddress", "=", data.getP());
                try {
                    ZBformApplication.mDB.delete(ZBStrokeEntity.class, whereBuilder);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    public class UpLoadQueryHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);

            Log.i(TAG, "on handleMessage ");
            new UpLoadStrokeDBQuery().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            Log.i(TAG, "can draw= "+ZBformApplication.sBlePenManager.getCanDraw());
            //停止书写则停止查询上传数据
            if (!mStopRecordCoord) {
                Message msg = mUpLoadQueryHandler.obtainMessage();
                Log.i(TAG, "send updateMSG ag");
                mUpLoadQueryHandler.sendMessageDelayed(msg, UPLOAD_DELAY);
            }
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "ZBFormService onCreate");
        ZBformApplication.sBlePenManager.setBlePenDrawCallback(mIBlePenDrawCallBack);

        mUpLoadQueryHandler = new UpLoadQueryHandler();
        mContext = this;

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mNetworkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(mNetworkChangeReceiver, mIntentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "ZBFormService onStartCommand1");
        //服务启动开始监听是否有笔迹上传
//        new UpLoadQueueTask().start();
//        new UpLoadQueueTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); //.start();
        //刚启动，查询一次，上传遗漏数据
//        new UpLoadStrokeDBQuery().execute();
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
        unregisterReceiver(mNetworkChangeReceiver);
        super.onDestroy();
    }

    public void setDrawFormInfo(FormInfo info, String recordId) {
        mDrawFormInfo = info;
        mRecordId = recordId;
    }

    public void startDraw() {
        if (!mStopRecordCoord) return;

        Log.i(TAG,"SERVICE startDraw");
        mStopRecordCoord = false;
        new CoodSaveDBTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        ZBformApplication.sBlePenManager.startDraw();
        //开始书写，5s 上传一次
        Message msg = mUpLoadQueryHandler.obtainMessage();
        mUpLoadQueryHandler.sendMessageDelayed(msg, UPLOAD_DELAY);
    }

    public void stopDraw() {
        if (!mIsRecordDraw) {
            mPageAddress = "0.0.0.0";
            Log.i(TAG,"clear pageaddress!!!!!");
        }
        Log.i(TAG,"SERVICE stopdraw0");
        if (mStopRecordCoord) return;
        Log.i(TAG,"SERVICE stopdraw1");
        mStopRecordCoord = true;
        ZBformApplication.sBlePenManager.stopDraw();
        HwData coord = new HwData();
        coord.setC(-1000);
        try {
            mCoordQueue.put(coord);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentPage(int page) {
        mCurrentPage = page;
    }

    public void setFormList(List<FormListInfo.Results> list){
        mFormList = list;
    }

    public void setCurrentPageAddress(String page){
        mPageAddress = page;
    }

    public void setIsRecordDraw(boolean draw){
        mIsRecordDraw = draw;
    }
}
