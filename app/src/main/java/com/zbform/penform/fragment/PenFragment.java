package com.zbform.penform.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tstudy.blepenlib.BlePenManager;
import com.tstudy.blepenlib.BlePenStreamManager;
import com.tstudy.blepenlib.callback.BleGattCallback;
import com.tstudy.blepenlib.callback.BlePenStreamCallback;
import com.tstudy.blepenlib.callback.BleScanCallback;
import com.tstudy.blepenlib.data.BleDevice;
import com.tstudy.blepenlib.exception.BleException;
import com.zbform.penform.R;
import com.zbform.penform.blepen.MyLicense;
import com.zbform.penform.blepen.ZBFormBlePenManager;
import com.zbform.penform.settings.DeviceAdapter;

import java.util.List;

public class PenFragment extends Fragment implements View.OnClickListener  {


    public static final String TAG = PenFragment.class.getSimpleName();

    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    private static final int REQUEST_OPEN_BT_CODE = 3;

    private Button btn_scan;
    private ImageView img_loading;

    private Animation operatingAnim;
    private DeviceAdapter mDeviceAdapter;
    private ProgressDialog progressDialog;
    private Context mContext;
    private BleDevice mBleDevice;
    private LinearLayout mPenInfoLayout;
    private LinearLayout mScanLayout;
    private TextView mPenName;
    private TextView mPenMac;
    private TextView mPenPower;
    private TextView mPenVersion;

    private String mBlePenVersion;
    private String mBlePenPower;

    private ZBFormBlePenManager mBlePenManager;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mBlePenManager = ZBFormBlePenManager.getInstance(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_penmanager, container, false);
        initView(view);
        boolean initSuccess = BlePenManager.getInstance().init(((Activity) mContext).getApplication(), MyLicense.getBytes());
        if (initSuccess) {
            Log.i(TAG,"initSuccess");
            BlePenManager.getInstance().enableLog(true);
        } else {
            Log.i(TAG,"not initSuccess");
            Toast.makeText(mContext, "初始化失败，请到开放平台申请授权或检查设备是否支持蓝牙", Toast.LENGTH_LONG).show();
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BlePenStreamManager.getInstance().closePenStream();
        BlePenManager.getInstance().disconnect(mBleDevice);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                if (btn_scan.getText().equals(getString(R.string.start_scan))) {
                    checkPermissions();
                } else if (btn_scan.getText().equals(getString(R.string.stop_scan))) {
                    BlePenManager.getInstance().cancelScan();
                }
                break;
            default:
        }
    }

    private void initView(View view) {
        btn_scan = view.findViewById(R.id.btn_scan);
        btn_scan.setText(getString(R.string.start_scan));
        btn_scan.setOnClickListener(this);

        img_loading = view.findViewById(R.id.img_loading);
        operatingAnim = AnimationUtils.loadAnimation(mContext, R.anim.rotate_test);
        operatingAnim.setInterpolator(new LinearInterpolator());
        progressDialog = new ProgressDialog(mContext);

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

            @Override
            public void onDisConnect(final BleDevice bleDevice) {
                if (BlePenManager.getInstance().isConnected(bleDevice)) {
                    //如果当前设备已连接，断开连接。
                    BlePenManager.getInstance().disconnect(bleDevice);
                }
            }

            @Override
            public void onDetail(BleDevice bleDevice) {
                if (BlePenManager.getInstance().isConnected(bleDevice)) {
                    //跳到绘制界面
//                    Intent intent = new Intent(MainActivity.this, DrawActivity.class);
//                    intent.putExtra(DrawActivity.KEY_DATA, bleDevice);
//                    startActivity(intent);
                }
            }
        });
        ListView listView_device = view.findViewById(R.id.list_device);
        listView_device.setAdapter(mDeviceAdapter);

        mScanLayout = view.findViewById(R.id.scan);
        mPenInfoLayout = view.findViewById(R.id.pen_info);
        mPenName = view.findViewById(R.id.pen_name);
        mPenMac = view.findViewById(R.id.pen_mac);
        mPenPower = view.findViewById(R.id.pen_power);
        mPenVersion = view.findViewById(R.id.pen_version);

        mScanLayout.setVisibility(View.VISIBLE);
        mPenInfoLayout.setVisibility(View.GONE);
    }

    private void startScan() {
        //扫描回调
        BleScanCallback callback = new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();
                img_loading.startAnimation(operatingAnim);
                img_loading.setVisibility(View.VISIBLE);
                btn_scan.setText(getString(R.string.stop_scan));
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btn_scan.setText(getString(R.string.start_scan));
                mDeviceAdapter.notifyDataSetChanged();
            }
        };
        BlePenManager.getInstance().scan(callback);
        mPenInfoLayout.setVisibility(View.GONE);
    }

    private void openPenStream(BleDevice bleDevice){
        BlePenStreamManager.getInstance().openPenStream(bleDevice, new BlePenStreamCallback(){

            @Override
            public void onOpenPenStreamSuccess() {
                Log.i(TAG, "[openPenStream]: onOpenPenStreamSuccess");
            }

            @Override
            public void onOpenPenStreamFailure(BleException e) {
                Log.i(TAG, "[openPenStream]: onOpenPenStreamFailure");

            }

            @Override
            public void onRemainBattery(int i) {
                Log.i(TAG, "[openPenStream]: onRemainBattery: "+i+"%");
                mBlePenPower = i+"%";
                Log.i(TAG,"mBlePenPower = "+mBlePenPower);
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPenPower.setText(mBlePenPower);
                    }
                });

            }

            @Override
            public void onMemoryFillLevel(int i, int i1) {
                Log.i(TAG, "[openPenStream]: onMemoryFillLevel");

            }

            @Override
            public void onCoordDraw(int i, String s, int i1, int i2, int i3, int i4, long l) {
                Log.i(TAG, "[openPenStream]: onCoordDraw");

            }

            @Override
            public void onOffLineCoordDraw(int i, String s, int i1, int i2, int i3, int i4, long l, int i5, int i6) {
                Log.i(TAG, "[openPenStream]: onOffLineCoordDraw");

            }

            @Override
            public void onDisConnected(boolean b, BleDevice bleDevice) {
                Log.i(TAG, "[openPenStream]: onDisConnected");

            }

            @Override
            public void onWarnActiveReport(int i) {
                Log.i(TAG, "[openPenStream]: onWarnActiveReport");

            }

            @Override
            public void onNewSession(String s, String s1, String s2) {
                Log.i(TAG, "[openPenStream]: onNewSession: hardVersion = "+s +"   softVersion = "+s1);
                mBlePenVersion = s1;
                Log.i(TAG,"mBlePenVersion = "+mBlePenVersion);
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPenVersion.setText(mBlePenVersion);
                    }
                });
            }

            @Override
            public void onCurrentTime(long l) {
                Log.i(TAG, "[openPenStream]: onCurrentTime");

            }
        });
    }

    private void connect(final BleDevice bleDevice) {
        //连接回调
        BleGattCallback bleGattCallback = new BleGattCallback() {
            @Override
            public void onStartConnect() {
                progressDialog.setMessage("正在连接蓝牙点阵笔:"+bleDevice.getName());

                progressDialog.show();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btn_scan.setText(getString(R.string.start_scan));
                progressDialog.dismiss();
//                Toast.makeText(MainActivity.this, getString(R.string.connect_fail), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                mBleDevice = bleDevice;
                Log.i(TAG,"onConnectSuccess");
                openPenStream(mBleDevice);
                progressDialog.dismiss();
//                mDeviceAdapter.addDevice(0,bleDevice);
//                mDeviceAdapter.notifyDataSetChanged();
                setPenInfo();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();
                mDeviceAdapter.removeDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();

//                if (isActiveDisConnected) {
//                    Toast.makeText(MainActivity.this, getString(R.string.active_disconnected), Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(MainActivity.this, getString(R.string.disconnected), Toast.LENGTH_LONG).show();
//                }
            }
        };
        BlePenManager.getInstance().connect(bleDevice, bleGattCallback);
    }

    public void setPenInfo(){
        if(mBleDevice != null){
            mPenInfoLayout.setVisibility(View.VISIBLE);
            mScanLayout.setVisibility(View.GONE);
            mPenName.setText(mBleDevice.getName());
            mPenMac.setText(mBleDevice.getMac());
//            mPenPower.setText(mBlePenPower);
//            mPenVersion.setText(mBlePenVersion);
        }
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
                    Toast.makeText(mContext, "自Android 6.0开始需要打开位置权限才可以搜索到Ble设备", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(mContext, "检查设备是否支持蓝牙BLE", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(mContext, "自Android 6.0开始需要打开位置权限才可以搜索到Ble设备", Toast.LENGTH_LONG).show();
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

/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN_BT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                checkPermissions();
            } else {
                Toast.makeText(mContext, "拒绝蓝牙权限", Toast.LENGTH_SHORT).show();
            }
        }
    }*/
}