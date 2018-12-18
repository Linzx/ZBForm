package com.pullrefresh.loadmore;

import android.view.View;
import android.view.View.OnClickListener;

import com.pullrefresh.loadmore.ILoadMoreViewFactory.ILoadMoreView;

public interface LoadMoreHandler {

    /**
     * @param contentView
     * @param loadMoreView
     * @param onClickLoadMoreListener
     * @return 是否有 init ILoadMoreView
     */
    public boolean handleSetAdapter(View contentView, ILoadMoreView loadMoreView, OnClickListener
            onClickLoadMoreListener);

    public void setOnScrollBottomListener(View contentView, OnScrollBottomListener onScrollBottomListener);

    void removeFooter();
    void addFooter();
}
