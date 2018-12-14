package com.zbform.penform.fragment;

import android.app.Activity;
import android.support.v4.app.DialogFragment;

/**
 * Created by isaac on 2018/8/2.
 */
public class AttachDialogFragment extends DialogFragment {

    public Activity mContext;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.mContext = activity;
    }


}
