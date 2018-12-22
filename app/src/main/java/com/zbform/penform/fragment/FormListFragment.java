package com.zbform.penform.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.pullrefresh.loadmore.OnLoadMoreListener;
import com.pullrefresh.loadmore.SwipeRefreshHelper;
import com.zbform.penform.R;

import java.util.ArrayList;
import java.util.List;

public class FormListFragment extends Fragment {

    public Activity mContext;
    private SwipeRefreshLayout mSryt;
    private GridView mListView;

    private List<String> mDatas = new ArrayList<>();
    private GridViewAdapter mAdapter;
    private SwipeRefreshHelper mSwipeRefreshHelper;
    private int page = 0;
    private Handler mHandler = new Handler();
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fromlist, container, false);
        initView(view);
        initData();
        return view;
    }

    private void initView(View view) {
        mSryt = (SwipeRefreshLayout) view.findViewById(R.id.sryt_swipe_listview);
        mListView = (GridView) view.findViewById(R.id.lv_swipe_listview);
        mSryt.setColorSchemeColors(Color.BLUE);
    }

    private void initData() {
        mAdapter = new GridViewAdapter(this.getActivity(), mDatas);
        mListView.setAdapter(mAdapter);
        mSwipeRefreshHelper = new SwipeRefreshHelper(mSryt);

        mSryt.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshHelper.autoRefresh();
            }
        });

        mSwipeRefreshHelper.setOnSwipeRefreshListener(new SwipeRefreshHelper.OnSwipeRefreshListener() {
            @Override
            public void onfresh() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDatas.clear();
                        page = 0;
                        for (int i = 0; i < 17; i++) {
                            mDatas.add(new String("  SwipeListView item  -" + i));
                        }
                        mAdapter.notifyDataSetChanged();
                        mSwipeRefreshHelper.refreshComplete();
                        mSwipeRefreshHelper.setLoadMoreEnable(true);
                    }
                }, 1500);
            }
        });

        mSwipeRefreshHelper.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void loadMore() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDatas.add(new String("  SwipeListView item  - add " + page));
                        mAdapter.notifyDataSetChanged();
                        mSwipeRefreshHelper.loadMoreComplete(true);
                        page++;
//                        Toast.makeText, "load more complete", Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }
        });

    }

    SwipeRefreshHelper.OnSwipeRefreshListener mOnSwipeRefreshListener = new SwipeRefreshHelper.OnSwipeRefreshListener() {
        @Override
        public void onfresh() {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDatas.clear();
                    page = 0;
                    for (int i = 0; i < 17; i++) {
                        mDatas.add(new String("  SwipeListView item  -" + i));
                    }
                    mAdapter.notifyDataSetChanged();
                    mSwipeRefreshHelper.refreshComplete();
                    mSwipeRefreshHelper.setLoadMoreEnable(true);
                }
            }, 1000);
        }
    };
    public class GridViewAdapter extends BaseAdapter {
        private List<String> datas;
        private LayoutInflater inflater;

        public GridViewAdapter(Context context, List<String> data) {
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
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.listitem_layout, parent, false);
            }
            TextView textView = (TextView) convertView;
            textView.setText(datas.get(position));
            return convertView;
        }

        public List<String> getData() {
            return datas;
        }

    }
}
