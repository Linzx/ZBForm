package com.zbform.penform.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pullrefresh.PtrClassicFrameLayout;
import com.pullrefresh.PtrDefaultHandler;
import com.pullrefresh.PtrFrameLayout;
import com.pullrefresh.loadmore.OnLoadMoreListener;
import com.zbform.penform.R;
import com.zbform.penform.db.FormSettingEntity;
import com.zbform.penform.json.RecordItem;
import com.zbform.penform.json.RecordListInfo;
import com.zbform.penform.task.RecordListTask;
import com.zbform.penform.util.CommonUtils;
import com.zbform.penform.util.PreferencesUtility;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecordListActivity extends BaseActivity implements RecordListTask.OnTaskListener {
    public static final String TAG = RecordListActivity.class.getSimpleName();
    private PtrClassicFrameLayout ptrClassicFrameLayout;
    private ListView mListView;
    private ListViewAdapter mAdapter;
    private List<RecordListInfo.Results> recordListResults = new ArrayList<>();
    private List<RecordItem> mData = new ArrayList<>();
    private Handler handler = new Handler();
    private RecordListTask mTask;
    private String mFormId;
    private String mFormName;
    private ActionBar mActionBar;

    private int mCurrentPage = 1;
    private boolean mRefresh = false;
    private int mPageSize = 10;
    private int mTotalPage;
    private Context mContext;
//    private PreferencesUtility mPreferencesUtility;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_recordlist);

//        mPreferencesUtility = PreferencesUtility.getInstance(this);
        mContext = this;
        mFormId = getIntent().getStringExtra("formId");
        mFormName = getIntent().getStringExtra("title");
        ptrClassicFrameLayout = findViewById(R.id.record_list_view_frame);
//        ptrClassicFrameLayout.setLoadMoreEnable(true);
        mListView = findViewById(R.id.record_list_view);

        setToolBar();
        initData();
    }

    private void setToolBar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("");
        TextView title = findViewById(R.id.toolbar_title);
        title.setText(mFormName);
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

    private void initData() {
        mTask = new RecordListTask(mContext, mFormId);
        FormSettingEntity entity = CommonUtils.getFormSetting(mFormId);
        if (entity != null) {
            mPageSize = entity.getRecordcount();
            Log.i("whd","mPageSize="+mPageSize);
        }
        mTask.setTaskListener(this);
        mAdapter = new ListViewAdapter(mContext);
        mListView.setAdapter(mAdapter);
        ptrClassicFrameLayout.postDelayed(new Runnable() {

            @Override
            public void run() {
                ptrClassicFrameLayout.autoRefresh(true);
            }
        }, 150);

        ptrClassicFrameLayout.setPtrHandler(new PtrDefaultHandler() {

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "get record list");
                        mCurrentPage = 1;
                        mRefresh = true;
                        if (mPageSize != 0) {
                            //0 代表加载全部
                            mTask.setPageInfo(String.valueOf(mCurrentPage), String.valueOf(mPageSize));
                        }
                        mTask.getRecordList();
//                        ptrClassicFrameLayout.refreshComplete();
//                        ptrClassicFrameLayout.setLoadMoreEnable(true);
                    }
                }, 1500);
            }
        });

        ptrClassicFrameLayout.setOnLoadMoreListener(new OnLoadMoreListener() {

            @Override
            public void loadMore() {
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mRefresh = false;
                        mCurrentPage++;
                        Log.i("whd", "load more mCurrentPage=" + mCurrentPage);
                        mTask.setPageInfo(String.valueOf(mCurrentPage), String.valueOf(mPageSize));
                        mTask.getRecordList();
//                        ptrClassicFrameLayout.loadMoreComplete(true);
                    }
                }, 1000);
            }
        });
    }

    @Override
    public void onTaskStart() {

    }

    @Override
    public void onTaskSuccess(List<RecordListInfo.Results> results) {
        recordListResults = results;
        if (recordListResults.size() > 0) {
            mTotalPage = recordListResults.get(0).getTotalPage();
            mData = Arrays.asList(recordListResults.get(0).getItems());
            if (mData != null && mData.size() > 0) {
                if (mRefresh) {
                    mAdapter.clearData();
                }
                mAdapter.setData(mData);
                mAdapter.notifyDataSetChanged();
                if (mRefresh && mPageSize > 0) {
                    //mPageSize 0 代表加载全部, 不显示底部More
                    ptrClassicFrameLayout.setLoadMoreEnable(true);
                }
            } else {
                if (mCurrentPage > 1) {
                    mCurrentPage--;
                }
                Log.i("whd", "mCurrentPage last=" + mCurrentPage);
                Toast.makeText(mContext, RecordListActivity.this.getResources().getString(R.string.toast_load_last), Toast.LENGTH_SHORT).show();
            }
        }
        ptrClassicFrameLayout.refreshComplete();
        if(ptrClassicFrameLayout.isLoadMoreEnable()) {
            ptrClassicFrameLayout.loadMoreComplete(true);
        }
//        if (mPreferencesUtility.getPreRecordLast() && mData.size() > 0) {
//            RecordItem recordItem = mData.get(0);
//            Intent intent = new Intent(mContext, RecordActivity.class);
//            intent.putExtra("formId", mFormId);
//            intent.putExtra("recordId", recordItem.getHwuuid());
//            intent.putExtra("page", recordItem.getHwpage());
//            intent.putExtra("recordCode", recordItem.getHwcode());
//            startActivity(intent);
//        }
    }

    @Override
    public void onTaskFail() {
        ptrClassicFrameLayout.refreshComplete();
        if(ptrClassicFrameLayout.isLoadMoreEnable()) {
            ptrClassicFrameLayout.loadMoreComplete(true);
        }
    }

    @Override
    public void onTaskCancelled() {
        ptrClassicFrameLayout.refreshComplete();
        if(ptrClassicFrameLayout.isLoadMoreEnable()) {
            ptrClassicFrameLayout.loadMoreComplete(true);
        }
    }


    private class ListViewAdapter extends BaseAdapter {
        private List<RecordItem> datas;
        private LayoutInflater inflater;

        public ListViewAdapter(Context context) {
            super();
            inflater = LayoutInflater.from(context);
            datas = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.listitem_recordlist_layout, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final RecordItem recordItem = datas.get(position);
            holder.recordCode.setText(recordItem.getHwcode());
            holder.modifyDate.setText(convertDateFormat(recordItem.getHwmodifydate()));
            holder.owner.setText(recordItem.getHwname());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, RecordActivity.class);
                    intent.putExtra("formId", mFormId);
                    intent.putExtra("recordId", recordItem.getHwuuid());
                    intent.putExtra("page", recordItem.getHwpage());
                    intent.putExtra("recordCode", recordItem.getHwcode());
                    startActivity(intent);
                }
            });
            return convertView;
        }

        public List<RecordItem> getData() {
            return datas;
        }

        public void setData(List<RecordItem> data) {
            if (data == null) {
                datas = data;
            } else {
                datas.addAll(data);
            }
            Log.i("whd", "date len=" + datas.size());
        }

        public void clearData(){
            if (datas != null) {
                datas.clear();
            }
        }
    }

    public String convertDateFormat(String origin) {
        SimpleDateFormat originDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
        SimpleDateFormat dstDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return dstDateFormat.format(originDateFormat.parse(origin, new ParsePosition(0)));
    }

    public class ViewHolder {
        public TextView recordCode;
        public TextView modifyDate;
        public TextView owner;

        public ViewHolder(View view) {
            recordCode = view.findViewById(R.id.record_code);
            modifyDate = view.findViewById(R.id.modify_date);
            owner = view.findViewById(R.id.owner);
        }

    }
}

