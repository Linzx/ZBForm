package com.zbform.penform.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pullrefresh.PtrClassicFrameLayout;
import com.pullrefresh.PtrDefaultHandler;
import com.pullrefresh.PtrFrameLayout;
import com.pullrefresh.loadmore.GridViewWithHeaderAndFooter;
import com.pullrefresh.loadmore.OnLoadMoreListener;
import com.zbform.penform.R;
import com.zbform.penform.json.FormListInfo;
import com.zbform.penform.task.FormListTask;
import com.zbform.penform.view.GridDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class FormListFragment extends BaseFragment implements FormListTask.OnFormTaskListener{

    private PtrClassicFrameLayout ptrClassicFrameLayout;
//    private GridViewWithHeaderAndFooter mGridView;
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private List<String> mData = new ArrayList<>();
    private Handler handler = new Handler();
    private FormListTask mTask;

    private int page = 0;
    private Context mContext;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fromlist, container, false);
        initView(view);
        initData();
        return view;
    }

    private void initView(View view) {
        ptrClassicFrameLayout = (PtrClassicFrameLayout) view.findViewById(R.id.form_grid_view_frame);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.form_recycler_view);
        mRecyclerView.addItemDecoration(new GridDividerItemDecoration(mContext));
    }

    private void initData() {
        mTask = new FormListTask();
        mTask.setOnFormTaskListener(this);

        mAdapter = new RecyclerViewAdapter(mContext);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext,2));
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
                        mTask.execute(mContext);
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
    public void onStartGet() {

    }

    @Override
    public void onGetSuccess(List<FormListInfo.Results> results) {
        Log.i("whd","onget success");
        if (mAdapter != null && results!= null){
            Log.i("whd","onget success1="+results.size());
            mAdapter.setData(results);
            mAdapter.notifyDataSetChanged();
        }
        ptrClassicFrameLayout.refreshComplete();
    }

    @Override
    public void onGetFail() {
        ptrClassicFrameLayout.refreshComplete();

    }


    public class RecyclerViewAdapter extends RecyclerView.Adapter<ChildViewHolder> {
        private List<FormListInfo.Results> datas;
        private LayoutInflater inflater;

        public RecyclerViewAdapter(Context context) {
            super();
            inflater = LayoutInflater.from(context);
        }

        public RecyclerViewAdapter(Context context, List<FormListInfo.Results> data) {
            super();
            inflater = LayoutInflater.from(context);
            datas = data;
        }


        @NonNull
        @Override
        public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.listitem_layout, null);
            ChildViewHolder viewHolder = new ChildViewHolder(view);
            viewHolder.itemImg.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {

                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
            FormListInfo.Results item = datas.get(position);
            holder.itemName.setText(item.getName());
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getItemCount() {
            if (datas != null ) {
                return datas.size();
            }else  {
                return 0;
            }
        }

//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.listitem_layout, parent, false);
//            }
//            TextView textView = (TextView) convertView.findViewById(R.id.form_name);
//            FormListInfo.Results item = datas.get(position);
//            textView.setText(item.getName());
//            return convertView;
//        }

        public List<FormListInfo.Results> getData() {
            return datas;
        }

        public void setData(List<FormListInfo.Results> results) {
            datas = results;
        }

    }

    public class ChildViewHolder extends RecyclerView.ViewHolder {
        public TextView itemName;
        public ImageView itemImg;

        public ChildViewHolder(View view) {
            super(view);
            itemName = (TextView) view.findViewById(R.id.form_name);
            itemImg = (ImageView) view.findViewById(R.id.form_img);
        }

    }
}
