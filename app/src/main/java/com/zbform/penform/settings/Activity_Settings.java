package com.zbform.penform.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.zbform.penform.R;
import com.zbform.penform.activity.FeedbackActivity;
//import com.zbform.penform.connect.ConnectActivity;
import com.zbform.penform.util.CommonUtils;


public class Activity_Settings extends AppCompatActivity implements View.OnClickListener {


    private Toolbar toolbar;
    private ActionBar actionBar;
    private int mStatusSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);
        initView();
        setOnListener();
    }

    private void setOnListener() {
        findViewById(R.id.txt_album).setOnClickListener(this);
        findViewById(R.id.txt_collect).setOnClickListener(this);
        findViewById(R.id.txt_money).setOnClickListener(this);
        findViewById(R.id.txt_card).setOnClickListener(this);
        findViewById(R.id.txt_smail).setOnClickListener(this);
        findViewById(R.id.txt_setting).setOnClickListener(this);
    }

    private void initView() {
        // TODO Auto-generated method stub
        toolbar = findViewById(R.id.settings_toolbar);
        mStatusSize = CommonUtils.getStatusHeight(this);
        setupToolbar();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.actionbar_back);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.settings);
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
        switch (v.getId()) {
            case R.id.txt_album:// 图片质量

                break;
            case R.id.txt_collect:// 兴趣标签
                break;
            case R.id.txt_money:// 用户反馈
                Intent intent = new Intent(Activity_Settings.this, FeedbackActivity.class);
                startActivity(intent);
                break;
            case R.id.txt_card:// Smart_Connection
               // Intent intent1 = new Intent(Activity_Settings.this, ConnectActivity.class);
                //startActivity(intent1);
                break;
            case R.id.txt_smail:// 关于我们

                break;
            case R.id.txt_setting:// 退出登录
                break;
            default:
                break;
        }
    }

}
