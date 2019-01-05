package com.zbform.penform.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zbform.penform.R;
import com.zbform.penform.json.RecordInfo;
import com.zbform.penform.task.RecordTask;

import java.util.ArrayList;
import java.util.List;

public class RecordFragment extends BaseFragment implements RecordTask.OnTaskListener {

    public static final String TAG = RecordFragment.class.getSimpleName();

    private List<RecordInfo.Results> recordResults = new ArrayList<>();
    private RecordTask mTask;
    private String mFormId;
    private String mRecordId;
    private int mPage;

    ImageView mRecordImg;

    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mFormId = (String)getArguments().get("formId");
        mRecordId = (String)getArguments().get("recordId");
        mPage =(int)getArguments().get("page");
        Log.i(TAG,"form id = "+mFormId+"  record id = "+mRecordId+"  page = "+mPage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        initView(view);
        initData();
        return view;
    }

    private void initView(View view) {
        mRecordImg = view.findViewById(R.id.record_img);
        mRecordImg.setImageResource(R.drawable.background);
    }

    private void initData() {
        mTask = new RecordTask(mContext, mFormId, mRecordId);
        mTask.setTaskListener(this);
    }

    @Override
    public void onTaskStart() {

    }

    @Override
    public void onTaskSuccess(List<RecordInfo.Results> results) {
        recordResults = results;
        if(recordResults.size() > 0) {
        }
    }

    @Override
    public void onTaskFail() {

    }

}
