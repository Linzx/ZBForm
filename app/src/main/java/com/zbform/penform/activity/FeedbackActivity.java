package com.zbform.penform.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.zbform.penform.R;
import com.zbform.penform.settings.FeedbackJsonParser;
import com.zbform.penform.util.CommonUtils;

import org.json.JSONObject;

public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mUploadFeedback;
    private EditText mQuestion,mNumber;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private int mStatusSize;

    String mFeedback,mPhoneNumber;
    private FeedbackJsonParser feedbackJsonParser;

    private static final String TAG = "FeedbackActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feedback);
        initView();
        setClickListener();
    }

    private void setClickListener() {
        feedbackJsonParser = new FeedbackJsonParser();
        mUploadFeedback.setOnClickListener(this);
    }

    private void initView() {
        mUploadFeedback = findViewById(R.id.upload_feedback);
        mQuestion = findViewById(R.id.question);
        mNumber = findViewById(R.id.user_contact);
        toolbar = findViewById(R.id.feedback_toolbar);
        mStatusSize = CommonUtils.getStatusHeight(this);
        setupToolbar();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.actionbar_back);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.user_feedback);
        toolbar.setPadding(0, mStatusSize, 0, 0);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.upload_feedback:
                new UploadFeedback().execute();
                break;
        }

    }

    public class UploadFeedback extends AsyncTask<String,String,String>{
        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG,"uploading");
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG,"do in background");
            mFeedback = mQuestion.getText().toString();
            mPhoneNumber = mNumber.getText().toString();

            try{
                JSONObject params = new JSONObject();
                params.put("accountNo","Darren");
                params.put("content",mFeedback);
                params.put("phone",mPhoneNumber);

                JSONObject jsonObject = feedbackJsonParser.makeHttpRequest(
                        "","POST",params);
                String message = jsonObject.getString("returnMessage");
                Log.d(TAG,"jsonObject:" + jsonObject.toString());
                return message;
            }catch (Exception e){
                e.printStackTrace();
                return "";
            }
        }
    }
}
