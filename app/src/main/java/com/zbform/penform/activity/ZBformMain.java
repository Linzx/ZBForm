package com.zbform.penform.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.zbform.penform.R;
import com.zbform.penform.account.GlideCircleTransform;
import com.zbform.penform.adapter.MenuItemAdapter;
import com.zbform.penform.fragment.BaseFragment;
import com.zbform.penform.fragment.FormListFragment;
import com.zbform.penform.fragment.OnFragmentChangeListener;
import com.zbform.penform.fragment.RecordListFragment;

import java.util.ArrayList;
import java.util.List;

public class ZBformMain extends BaseActivity implements OnFragmentChangeListener {

    private static final String TAG = "ZBformMain";

    private ActionBar mActionBar;
    private ArrayList<TextView> tabs = new ArrayList<>();
    private DrawerLayout drawerLayout;
    private ListView mLvLeftMenu;
    private TextView mTootBarTitle;
    private long time = 0;

    private FragmentManager fragmentManager;

    private BaseFragment mCurrentFragmet;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, ZBformMain.class);
        activity.startActivity(intent);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawableResource(R.color.background_material_light_1);
        fragmentManager = getSupportFragmentManager();

        drawerLayout = (DrawerLayout) findViewById(R.id.fd);
        mLvLeftMenu = (ListView) findViewById(R.id.id_lv_left_menu);

        mTootBarTitle = (TextView)findViewById(R.id.toolbar_title);
        setToolBar();
        setUpDrawer();
        setmTootBarTitle(getString(R.string.menu_item_formlist));
        mCurrentFragmet = new FormListFragment();
        selectFragment(mCurrentFragmet);
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        mActionBar.setTitle("");
    }

    private void setmTootBarTitle(String title){
        mTootBarTitle.setText(title);
    }

    private void setUpDrawer() {
        LayoutInflater inflater = LayoutInflater.from(this);
        mLvLeftMenu.addHeaderView(inflater.inflate(R.layout.nav_header_main, mLvLeftMenu, false));
        //圆形头像
        ImageView avtar = findViewById(R.id.top_bac);
        Glide.with(this)
                .load(R.drawable.logo)
                .error(R.drawable.logo)
                .transform(new GlideCircleTransform(this))
                .into(avtar);

        mLvLeftMenu.setAdapter(new MenuItemAdapter(this));
        mLvLeftMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 1:
                        // 表单列表
                        if (!(mCurrentFragmet instanceof FormListFragment)) {
                            mCurrentFragmet = new FormListFragment();
                            selectFragment(mCurrentFragmet);
                            setmTootBarTitle(getString(R.string.menu_item_formlist));
                        }
                        drawerLayout.closeDrawers();
                        break;
                    case 2:
                        // 表单记录列表

                        drawerLayout.closeDrawers();
                        break;
                    case 3:
                        // 设置
                        Intent intent = new Intent(ZBformMain.this, SettingActivity.class);
                        startActivity(intent);
                        drawerLayout.closeDrawers();
                        break;
                    case 4:
                        // 更新
                        finish();
                        drawerLayout.closeDrawers();
                    case 5:
                        // 退出
                        finish();
                        drawerLayout.closeDrawers();

                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case android.R.id.home: //Menu icon
                drawerLayout.openDrawer(Gravity.LEFT);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.i(TAG, "Fragments size = "+fragmentManager.getFragments().size());
            if (fragmentManager.getFragments().size() > 1 && mCurrentFragmet instanceof RecordListFragment) {
                fragmentManager.popBackStack();
                setmTootBarTitle(getString(R.string.menu_item_formlist));
            } else {
                if ((System.currentTimeMillis() - time > 1000)) {
                    Toast.makeText(this, "再按一次返回桌面", Toast.LENGTH_SHORT).show();
                    time = System.currentTimeMillis();
                } else {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(intent);
                }
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    fragment.onRequestPermissionsResult(requestCode,permissions,grantResults);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    private void selectFragment(BaseFragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container,  fragment);
        if(fragment instanceof RecordListFragment) {
            transaction.addToBackStack(fragment.getClass().getSimpleName());
        }
        transaction.commit();
    }

    @Override
    public void onRecordListFragmentSelect(String formId) {
        mCurrentFragmet = new RecordListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("formId", formId);
        mCurrentFragmet.setArguments(bundle);
        selectFragment(mCurrentFragmet);
        setmTootBarTitle(getString(R.string.menu_item_formrecord));
    }

    @Override
    public void onRecordFragmentSelect(String formId, String RecordId) {

    }

}
