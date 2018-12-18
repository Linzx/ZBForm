package com.zbform.penform.banner;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import com.facebook.drawee.view.SimpleDraweeView;

public class FrescoImageLoader extends ImageLoader {

    //提供createImageView 方法，方便fresco自定义ImageView
    @Override
    public ImageView createImageView(Context context) {
        SimpleDraweeView simpleDraweeView = new SimpleDraweeView(context);
        return simpleDraweeView;
    }

    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        //用fresco加载图片
        Uri uri = Uri.parse((String) path);
        imageView.setImageURI(uri);
    }
}