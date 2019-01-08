package com.zbform.penform.task;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.zbform.penform.db.ZBStrokeEntity;
import com.zbform.penform.json.HwData;
import com.zbform.penform.json.Point;
import com.zbform.penform.json.UpLoadStrokeinfo;
import com.zbform.penform.json.ZBFormInnerItem;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.net.ErrorCode;
import com.zbform.penform.net.IZBformNetBeanCallBack;
import com.zbform.penform.net.ZBformNetBean;

import java.util.ArrayList;
import java.util.List;

public class UpLoadStrokeTask extends AsyncTask <Void, Void, Void>{
    private static final String TAG = "UpLoadStrokeTask";
    private ArrayList<ZBFormInnerItem> mInnerItems;
    private Context mContext;
    public UpLoadStrokeTask(Context context){
        super();
        mContext = context;
        mInnerItems = new ArrayList<ZBFormInnerItem>();

    }
    @Override
    protected Void doInBackground(Void... params) {
        //db data convert to jason object
        try {
            List<ZBStrokeEntity> strokeUpLoad = ZBformApplication.mDB.
                    findAll(Selector.from(ZBStrokeEntity.class).
                    where("isupload", "=", 0));
            Log.i(TAG,"zblen="+strokeUpLoad.size());

            for(ZBStrokeEntity entity : strokeUpLoad){
                ZBFormInnerItem item;
                item = findInnerItem(entity.formid,entity.recordid,entity.itemid,entity.userid);
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

                    for(HwData stroke : innerItem.dataList){
                        if (stroke.dList.size() >0){
                            stroke.setD(stroke.dList.toArray(new Point[stroke.dList.size()]));
                        }
                    }

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
        Log.i(TAG,"mInnerItems len="+mInnerItems.size());

        for(ZBFormInnerItem item : mInnerItems) {
            new NetTask(item).execute(mContext);
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

    private HwData findHwData(ZBFormInnerItem item,String tagtime) {
        HwData result = null;
        for (HwData data : item.dataList) {
            if (tagtime.equals(data.getT())) {
                result = data;
                break;
            }

        }
        return result;
    }

    public class UpdateDBTask extends AsyncTask <Void, Void, Void> {
        private ZBFormInnerItem mInnerItem;
        public UpdateDBTask(Context context, ZBFormInnerItem item){
            super();
            mContext = context;
            mInnerItem =  item;
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

    class NetTask implements IZBformNetBeanCallBack {
        private Context mContext;

        private ZBFormInnerItem mInnerItem;
        public NetTask(ZBFormInnerItem innerItem){
            mInnerItem = innerItem;
        }
        public void execute(Context context) {
            mContext = context;
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

            String test = mInnerItem.getItemid();
            if (TextUtils.isEmpty(test)){
                test = "IZBform-181210896100217";
            }
            params.addQueryStringParameter("itemid", Uri.encode(test));


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

//        public void setOnFormTaskListener(com.zbform.penform.task.TestTask.OnFormTaskListener listener) {
//            mOnFormTaskListener = listener;
//        }

        @Override
        public void onStart() {
//            if (mOnFormTaskListener != null) {
//                mOnFormTaskListener.onStartGet();
//            }
        }

        @Override
        public void onCancelled() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoading(long total, long current, boolean isUploading) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSuccess(ResponseInfo<String> responseInfo) {
            try {
                String resultGson = responseInfo.result;
                Log.i(TAG, "result img Gson!!=" + resultGson);
                Gson gson = new Gson();
                UpLoadStrokeinfo upInfo = gson.fromJson(resultGson, new TypeToken<UpLoadStrokeinfo>() {
                }.getType());

                if (upInfo != null && upInfo.header != null
                        && upInfo.results != null && upInfo.results.length > 0) {

                    Log.i(TAG, "errorcode=" + upInfo.header.getErrorCode());
                    if (upInfo.header.getErrorCode().equals(ErrorCode.RESULT_OK)) {

                        new UpdateDBTask(mContext,mInnerItem).execute();
//                        if (mOnFormTaskListener != null) {
//                            List<FormListInfo.Results> data = Arrays.asList(forms.results);
//                            mOnFormTaskListener.onGetSuccess(data);
//                        }

                    } else {
//                        if (mOnFormTaskListener != null) {
//                            mOnFormTaskListener.onGetFail();
//                        }
                    }
                } else {
                    Log.i("whd", "user null");
//                    if (mOnFormTaskListener != null) {
//                        mOnFormTaskListener.onGetFail();
//                    }
                }
            } catch (Exception e) {
                Log.i(TAG, "e=" + e.getMessage());
//
//                if (mOnFormTaskListener != null) {
//                    mOnFormTaskListener.onGetFail();
//                }
            }
        }

        @Override
        public void onFailure(HttpException error, String msg) {
//            if (mOnFormTaskListener != null) {
//                mOnFormTaskListener.onGetFail();
//            }
        }
    }
}
