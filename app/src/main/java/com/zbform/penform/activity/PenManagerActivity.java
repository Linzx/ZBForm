package com.zbform.penform.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tstudy.blepenlib.BlePenManager;
import com.tstudy.blepenlib.callback.BleScanCallback;
import com.tstudy.blepenlib.data.BleDevice;
import com.tstudy.blepenlib.exception.BleException;
import com.zbform.penform.R;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.blepen.ZBFormBlePenManager;
import com.zbform.penform.dialog.LoadingDialog;
import com.zbform.penform.settings.DeviceAdapter;

import java.util.List;

public class PenManagerActivity extends BaseActivity implements View.OnClickListener, ZBFormBlePenManager.IBlePenStateCallBack {


    public static final String TAG = PenManagerActivity.class.getSimpleName();

    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    private static final int REQUEST_OPEN_BT_CODE = 3;

    private TextView btn_scan;
    private TextView scan_result_title;
    private ProgressBar loading_progress;

    private DeviceAdapter mDeviceAdapter;
    private LoadingDialog progressDialog;
    private Context mContext;
    private BleDevice mBleDevice;
    private LinearLayout mPenInfoLayout;
    private LinearLayout mScanLayout;
    private TextView mPenName;
    private TextView mPenMac;
    private TextView mPenPower;
    private TextView mPenVersion;
    private ActionBar mActionBar;

    private ZBFormBlePenManager mBlePenManager = ZBformApplication.sBlePenManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_penmanager);

        mContext = this;
        initView();
        mBlePenManager.setBlePenStateCallBack(this);
        mBlePenManager.setZBBleGattCallback(mBleGattCallback);
        boolean initSuccess = mBlePenManager.isBleInitSuccess();
        if (initSuccess) {
            Log.i(TAG, "initSuccess");
            BlePenManager.getInstance().enableLog(true);
        } else {
            Log.i(TAG, "not initSuccess");
            Toast.makeText(mContext, "初始化失败，请到开放平台申请授权或检查设备是否支持蓝牙", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pen_nav_scan:
                if (btn_scan.getText().equals(getString(R.string.start_scan))) {
                    Log.i(TAG, "START Scan");
                    checkPermissions();
                } else if (btn_scan.getText().equals(getString(R.string.stop_scan))) {
                    BlePenManager.getInstance().cancelScan();
                }
                break;
            default:
        }
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(getString(R.string.pref_header_pen_manage));
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLoading() {
        Log.i(TAG, "showLoading");
        if (progressDialog != null && progressDialog.isShowing())
            return;
        progressDialog = new LoadingDialog(this, getString(R.string.connecting_pen));
        progressDialog.show();
    }

    private void dismissLoading() {
        if (progressDialog != null) {
            Log.i(TAG, "dismissLoading");
            progressDialog.dismiss();
        }
    }
    private void initView() {
        btn_scan = findViewById(R.id.pen_nav_scan);
        btn_scan.setText(getString(R.string.start_scan));
        btn_scan.setOnClickListener(this);

        scan_result_title = findViewById(R.id.pen_nav_scan_result_title);
        scan_result_title.setVisibility(View.GONE);
        setToolBar();

        loading_progress = findViewById(R.id.loading_progress);

        mDeviceAdapter = new DeviceAdapter(mContext);
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onConnect(BleDevice bleDevice) {
                if (!BlePenManager.getInstance().isConnected(bleDevice)) {
                    //如果当前设备未连接，取消扫描，连接选中设备。
                    BlePenManager.getInstance().cancelScan();
                    connect(bleDevice);
                }
            }
        });
        ListView listView_device = findViewById(R.id.list_device);
        listView_device.setAdapter(mDeviceAdapter);

        mScanLayout = findViewById(R.id.scan);
        mPenInfoLayout = findViewById(R.id.pen_info);
        mPenName = findViewById(R.id.pen_name);
        mPenMac = findViewById(R.id.pen_mac);
        mPenPower = findViewById(R.id.pen_power);
        mPenVersion = findViewById(R.id.pen_version);

        if (mBlePenManager.isConnectedBleDevice()) {
            Log.i(TAG, "ble device has connected.");
            setPenInfo();
        } else {
            Log.i(TAG, "ble device not connected yet.");

            mScanLayout.setVisibility(View.VISIBLE);
            mPenInfoLayout.setVisibility(View.GONE);
        }
    }

    private void startScan() {
        Log.i(TAG, "startScan()");
        BlePenManager.getInstance().scan(mScanCallback);
        mPenInfoLayout.setVisibility(View.GONE);
    }

    //扫描回调
    BleScanCallback mScanCallback = new BleScanCallback() {
        @Override
        public void onScanStarted(boolean success) {
            Log.i(TAG, "onScanStarted()");

            mDeviceAdapter.clearScanDevice();
            mDeviceAdapter.notifyDataSetChanged();
            loading_progress.setVisibility(View.VISIBLE);
            btn_scan.setText(getString(R.string.stop_scan));
        }

        @Override
        public void onLeScan(BleDevice bleDevice) {
            super.onLeScan(bleDevice);
        }

        @Override
        public void onScanning(BleDevice bleDevice) {
            scan_result_title.setVisibility(View.VISIBLE);
            mDeviceAdapter.addDevice(bleDevice);
            mBleDevice = bleDevice;
            mDeviceAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanFinished(List<BleDevice> scanResultList) {
            loading_progress.setVisibility(View.GONE);
            btn_scan.setText(getString(R.string.start_scan));
            mDeviceAdapter.notifyDataSetChanged();
        }
    };

    private void connect(final BleDevice bleDevice) {
        mBlePenManager.connect(bleDevice);
    }

    //连接回调
    ZBFormBlePenManager.IZBBleGattCallback mBleGattCallback = new ZBFormBlePenManager.IZBBleGattCallback() {
        @Override
        public void onStartConnect() {
            showLoading();
        }

        @Override
        public void onConnectFail(BleDevice bleDevice, BleException exception) {
            loading_progress.setVisibility(View.INVISIBLE);
            btn_scan.setText(getString(R.string.start_scan));
            dismissLoading();
//                Toast.makeText(MainActivity.this, getString(R.string.connect_fail), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
            mBleDevice = bleDevice;
            Log.i(TAG, "onConnectSuccess");
            mBlePenManager.setBleDevice(mBleDevice);
            dismissLoading();
            mDeviceAdapter.addDevice(0,bleDevice);
//                mDeviceAdapter.notifyDataSetChanged();
        }

        @Override
        public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
            dismissLoading();

        }
    };


    public void setPenInfo() {
        mPenInfoLayout.setVisibility(View.VISIBLE);
        mScanLayout.setVisibility(View.GONE);
        mPenName.setText(mBlePenManager.getBleDeviceName());
        mPenMac.setText(mBlePenManager.getBleDeviceMac());
        mPenPower.setText(mBlePenManager.getBleDevicePower() + "%");
        mPenVersion.setText(mBlePenManager.getBleDeviceSwVersion());
    }


    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户允许改权限，0表示允许，-1表示拒绝 PERMISSION_GRANTED = 0， PERMISSION_DENIED = -1
                //permission was granted, yay! Do the contacts-related task you need to do.
                //这里进行授权被允许的处理
                startScan();
            } else {
                //permission denied, boo! Disable the functionality that depends on this permission.
                //这里进行权限被拒绝的处理
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(mContext, "需要打开位置权限才可以搜索到数码笔设备", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(mContext, "检查设备是否支持蓝牙", Toast.LENGTH_LONG).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
//            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_OPEN_BT_CODE);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要向用户解释为什么需要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(mContext, "需要打开位置权限才可以搜索到数码笔设备", Toast.LENGTH_LONG).show();
                }
                //请求权限
                ActivityCompat.requestPermissions((Activity) mContext,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_PERMISSION_LOCATION);
            } else {
                startScan();
            }
        } else {
            startScan();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN_BT_CODE) {
            if (resultCode == RESULT_OK) {
                checkPermissions();
            } else {
                Toast.makeText(mContext, "拒绝蓝牙权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onOpenPenStreamSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPenInfoLayout.setVisibility(View.VISIBLE);
                mScanLayout.setVisibility(View.GONE);
                mPenName.setText(mBlePenManager.getBleDeviceName());
                mPenMac.setText(mBlePenManager.getBleDeviceMac());
            }
        });
    }

    @Override
    public void onRemainBattery(final int percent) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPenPower.setText(percent + "%");
            }
        });
    }

    @Override
    public void onMemoryFillLevel(int percent, int byteNum) {
    }

    @Override
    public void onNewSession(String hardVersion, final String softVersion, String syncNum) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPenVersion.setText(softVersion);
            }
        });
    }
}

