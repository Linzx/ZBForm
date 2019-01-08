package com.zbform.penform.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pullrefresh.PtrClassicFrameLayout;
import com.pullrefresh.PtrDefaultHandler;
import com.pullrefresh.PtrFrameLayout;
import com.pullrefresh.loadmore.OnLoadMoreListener;
import com.zbform.penform.R;
import com.zbform.penform.activity.FormDrawActivity;
import com.zbform.penform.activity.RecordActivity;
import com.zbform.penform.activity.ZBformMain;
import com.zbform.penform.json.RecordItem;
import com.zbform.penform.json.RecordListInfo;
import com.zbform.penform.task.RecordListTask;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecordListFragment extends BaseFragment implements RecordListTask.OnTaskListener {

    public static final String TAG = RecordListFragment.class.getSimpleName();
    private PtrClassicFrameLayout ptrClassicFrameLayout;
    private ListView mListView;
    private ListViewAdapter mAdapter;
    private List<RecordListInfo.Results> recordListResults = new ArrayList<>();
    private List<RecordItem> mData = new ArrayList<>();
    private Handler handler = new Handler();
    private RecordListTask mTask;
    private String mFormId;

    private Context mContext;
    private OnFragmentChangeListener mFragmentChangeCallBack;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mFormId = (String)getArguments().get("formId");
        if(getActivity() instanceof ZBformMain) {
            Log.i(TAG, "zbformmain fragment,set fragment change callback.");
            mFragmentChangeCallBack = (OnFragmentChangeListener) mContext;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recordlist, container, false);
        initView(view);
        initData();
        return view;
    }

    private void initView(View view) {
        ptrClassicFrameLayout = view.findViewById(R.id.record_list_view_frame);
        mListView = view.findViewById(R.id.record_list_view);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    private void initData() {
        mTask = new RecordListTask(mContext, mFormId);
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
                        Log.i(TAG,"get record list");
                        mTask.getRecordList();
//                        page = 0;
//                        mData.clear();
//                        for (int i = 0; i < 40; i++) {
//                            mData.add(new String("GridView item  -" + i));
//                        }
//                        mAdapter.notifyDataSetChanged();
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
//                        for (int i = 0; i < 4; i++) {
//                            mData.add(new String("GridView item -- add" + page));
//                        }
//                        mAdapter.notifyDataSetChanged();
//                        ptrClassicFrameLayout.loadMoreComplete(true);
//                        page++;
                        Toast.makeText(mContext, "load more complete", Toast.LENGTH_SHORT).show();
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
        if(recordListResults.size() > 0) {
            mData = Arrays.asList(recordListResults.get(0).getItems());
            mAdapter.setData(mData);
            mAdapter.notifyDataSetChanged();
        }
        ptrClassicFrameLayout.refreshComplete();
    }

    @Override
    public void onTaskFail() {
        ptrClassicFrameLayout.refreshComplete();

    }


    public class ListViewAdapter extends BaseAdapter {
        private List<RecordItem> datas ;
        private LayoutInflater inflater;

        public ListViewAdapter(Context context) {
            super();
            inflater = LayoutInflater.from(context);
            datas = new ArrayList<>();
        }

        public ListViewAdapter(Context context, List<RecordItem> data) {
            super();
            inflater = LayoutInflater.from(context);
            datas = data;
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
//                    mFragmentChangeCallBack.onRecordFragmentSelect(mFormId, recordItem.getHwuuid(),recordItem.getHwcode(),recordItem.getHwpage());
                    Intent intent = new Intent(mContext, RecordActivity.class);
                    intent.putExtra("formId", mFormId);
                    intent.putExtra("recordId", recordItem.getHwuuid());
                    intent.putExtra("page",recordItem.getHwpage());
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
            datas = data;
        }

    }

    public String convertDateFormat(String origin) {
        SimpleDateFormat originDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
        SimpleDateFormat dstDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return dstDateFormat.format(originDateFormat.parse(origin,new ParsePosition(0)));
    }
    public class ViewHolder{
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
