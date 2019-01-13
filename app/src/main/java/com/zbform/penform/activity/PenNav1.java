package com.zbform.penform.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.zbform.penform.R;

public class PenNav1 extends BaseActivity implements View.OnClickListener {

    TextView mStartConnect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pen_nav1);

        mStartConnect = findViewById(R.id.pen_nav_start_connect);

        mStartConnect.setOnClickListener(this);

        setToolBar();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.pen_nav_start_connect) {
            Intent intent = new Intent(this, PenNav2.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(getString(R.string.pen_nav_title));
    }
}
