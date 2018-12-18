package com.zbform.penform.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.zbform.penform.activity.BaseActivity;

/**
 * Created by isaac on 2018/8/2.
 */
public class BaseFragment extends Fragment {

    public Activity mContext;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.mContext = activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
//        ((BaseActivity) getActivity()).setMusicStateListenerListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
//        ((BaseActivity) getActivity()).removeMusicStateListenerListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}
