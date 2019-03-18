package com.zbform.penform.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.github.chrisbanes.photoview.PhotoView;
import com.zbform.penform.R;

public class PageItemView extends FrameLayout {
    View mLoading;
    PhotoView mImg;
    public PageItemView(@NonNull Context context) {
        super(context);
        View content = View.inflate(context,R.layout.form_pager_item, this);
        mLoading = content.findViewById(R.id.img_loading);
        mImg = content.findViewById(R.id.form_img);
    }

    public PageItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View content = View.inflate(context,R.layout.form_pager_item, this);
    }

    public void showLoading(){
        if (mLoading != null){
            mLoading.setVisibility(View.VISIBLE);
        }
    }

    public void dismissLoading(){
        if (mLoading != null){
            mLoading.setVisibility(View.INVISIBLE);
        }
    }

    public PhotoView getImageView(){
        return mImg;
    }
}
