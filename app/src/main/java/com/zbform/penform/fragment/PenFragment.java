package com.zbform.penform.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pullrefresh.PtrClassicFrameLayout;
import com.zbform.penform.R;

public class PenFragment extends Fragment {

    public static final String TAG = PenFragment.class.getSimpleName();
    Context mContext;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_penmanager, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
    }

}