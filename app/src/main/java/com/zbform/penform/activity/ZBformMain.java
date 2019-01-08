package com.zbform.penform.activity;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tstudy.blepenlib.data.BleDevice;
import com.tstudy.blepenlib.exception.BleException;
import com.zbform.penform.R;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.account.GlideCircleTransform;
import com.zbform.penform.adapter.MenuItemAdapter;
import com.zbform.penform.blepen.ZBFormBlePenManager;
import com.zbform.penform.fragment.BaseFragment;
import com.zbform.penform.fragment.FormListFragment;
import com.zbform.penform.fragment.OnFragmentChangeListener;
import com.zbform.penform.fragment.RecordFragment;
import com.zbform.penform.fragment.RecordListFragment;
import com.zbform.penform.services.ZBFormService;
import com.zbform.penform.task.UpLoadStrokeTask;

import java.util.ArrayList;
import java.util.List;

public class ZBformMain extends BaseActivity implements OnFragmentChangeListener {

    private static final String TAG = "ZBformMain";

    private ActionBar mActionBar;
    private Toolbar mToolbar;
    private ArrayList<TextView> tabs = new ArrayList<>();
    private DrawerLayout drawerLayout;
    private ListView mLvLeftMenu;
    private TextView mTootBarTitle;
    private long time = 0;
    private Intent mService;

    private FragmentManager fragmentManager;

    private BaseFragment mCurrentFragmet;

    ZBFormBlePenManager.IZBBleGattCallback bleGattCallback = new ZBFormBlePenManager.IZBBleGattCallback() {
        @Override
        public void onStartConnect() {
        }

        @Override
        public void onConnectFail(BleDevice bleDevice, BleException exception) {
            Log.i(TAG,"main onConnectFail");
            if (mToolbar != null) {
                Menu menu = mToolbar.getMenu();
                setUpMenu(menu);
            }
        }

        @Override
        public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
            Log.i(TAG,"main onConnectSuccess");
            if (mToolbar != null) {
                Menu menu = mToolbar.getMenu();
                setUpMenu(menu);
            }
        }

        @Override
        public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
            Log.i(TAG,"main onDisConnected");
            if (mToolbar != null) {
                Menu menu = mToolbar.getMenu();
                setUpMenu(menu);
            }
        }
    };

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
        ZBformApplication.sBlePenManager.setZBBleGattCallback(bleGattCallback);
        setToolBar();
        setUpDrawer();
        setmTootBarTitle(getString(R.string.menu_item_formlist));
        mCurrentFragmet = new FormListFragment();
        selectFragment(mCurrentFragmet);
        mService = new Intent(this, ZBFormService.class);
        startService(mService);

//        new UpLoadStrokeTask(this).execute();
    }

    private void setUpMenu(Menu menu) {
        if (menu == null) return;
        MenuItem connect = menu.findItem(R.id.pen_connect);
        MenuItem disConnect = menu.findItem(R.id.pen_disconnect);

        if (connect != null && disConnect != null) {
            if (ZBformApplication.sBlePenManager.isConnectedBleDevice()) {
                Log.i(TAG, "CONNECT");
                connect.setVisible(true);
                disConnect.setVisible(false);
            } else {
                Log.i(TAG, "DISCONNECT");
                connect.setVisible(false);
                disConnect.setVisible(true);
            }
        }
    }

    private void setToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
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
                        // 设置
                        Intent intent = new Intent(ZBformMain.this, SettingActivity.class);
                        startActivity(intent);
                        drawerLayout.closeDrawers();
                        break;
                    case 3:
                        // 更新
//                        finish();
                        drawerLayout.closeDrawers();
                    case 4:
                        // 退出
                        finish();
                        drawerLayout.closeDrawers();

                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_pen_state, menu);

        setUpMenu(menu);

        return super.onCreateOptionsMenu(menu);
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
    protected void onResume() {
        super.onResume();

        if (mToolbar != null) {
            Menu menu = mToolbar.getMenu();
            setUpMenu(menu);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(mService);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            int fragSize = fragmentManager.getFragments().size();
            Log.i(TAG, "Fragments size = " + fragSize);
            if (fragmentManager.getFragments().size() > 1) {
                if(mCurrentFragmet instanceof RecordListFragment) {
                    fragmentManager.popBackStack();
                    setmTootBarTitle(getString(R.string.menu_item_formlist));
                } else if(mCurrentFragmet instanceof RecordFragment) {
                    fragmentManager.popBackStack();
                    setmTootBarTitle(getString(R.string.title_record_list));
                }
            }else {
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
        if(fragment instanceof RecordListFragment || fragment instanceof RecordFragment) {
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
        setmTootBarTitle(getString(R.string.title_record_list));
    }

    @Override
    public void onRecordFragmentSelect(String formId, String recordId, String recordCode, int page) {
        mCurrentFragmet = new RecordFragment();
        Bundle bundle = new Bundle();
        bundle.putString("formId", formId);
        bundle.putString("recordId", recordId);
        bundle.putInt("page",page);
        mCurrentFragmet.setArguments(bundle);
        selectFragment(mCurrentFragmet);
        setmTootBarTitle(recordCode);
    }
}
