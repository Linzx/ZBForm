package com.zbform.penform.services;

import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.tstudy.blepenlib.data.BleDevice;
import com.tstudy.blepenlib.data.BleScanState;
import com.tstudy.blepenlib.exception.BleException;
import com.zbform.penform.R;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.blepen.ZBFormBlePenManager;
import com.zbform.penform.util.PreferencesUtility;

import java.util.List;

public class BleConnectService extends Service implements ZBFormBlePenManager.IZBBleScanCallback, ZBFormBlePenManager.IZBBleConnectCallback {

    public static final String TAG = BleConnectService.class.getSimpleName();

    private LocalBinder binder = new LocalBinder();

    ZBFormBlePenManager mBlePenManager = ZBformApplication.sBlePenManager;

    private PreferencesUtility mPreference;

    private String mLastPenName = "null";
    private String mLastPenMac = "null";

    private boolean isConnectSuccess = false;

    private Context mContext;

    private static final String PEN_DEFAULT_VALUE = "null";
    private static final int SCAN_DURATION = 10 * 1000;

    private boolean mScanStoped = false;


    private Handler mScanHandler = new Handler();
    private Runnable mScanRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG,"isConnectSuccess="+isConnectSuccess);
            Log.i(TAG,"getIsConnectedNow="+ZBformApplication.sBlePenManager.getIsConnectedNow());
            if (
//                    !isConnectSuccess &&
                    !ZBformApplication.sBlePenManager.getIsConnectedNow()) {
                if (mBlePenManager.getScanState() == BleScanState.STATE_IDLE) {
                    Log.i(TAG, "Scan Runnable, idle state, start scan");
                    mBlePenManager.scan();
                }
            }
        }
    };

    public void stopAutoScan() {
        Log.i(TAG, "stopAutoScan");
        mScanHandler.removeCallbacksAndMessages(null);
        mBlePenManager.removeZBBleConnectCallback(this);
        mBlePenManager.removeZBBleScanCallback(this);
        if (mBlePenManager.getScanState() != BleScanState.STATE_IDLE) {
            Log.i(TAG, "stopAutoScan1");
            mScanStoped = true;
            mBlePenManager.cancelScan();
        }
    }

    public void startAutoScan() {
        Log.i(TAG, "stopAutoScan");
        mBlePenManager.setZBBleConnectCallback(this);
        mBlePenManager.setZBBleScanCallback(this);
        if (!mLastPenMac.equals(PEN_DEFAULT_VALUE) &&
                !mLastPenName.equals(PEN_DEFAULT_VALUE) &&
                !ZBformApplication.sBlePenManager.getIsConnectedNow()) {
            Log.i(TAG, "startAutoScan1");
            mScanStoped = false;

            mScanHandler.post(mScanRunnable);
        }
    }

    @Override
    public void onStartConnect() {
        Log.i(TAG, "onStartConnect");
        isConnectSuccess = false;
    }

    @Override
    public void onConnectFail(BleDevice bleDevice, BleException exception) {
        Log.i(TAG, "onConnectFail");
        isConnectSuccess = false;
    }

    @Override
    public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
        Log.i(TAG, "onConnectSuccess");
        Toast.makeText(mContext, getString(R.string.scan_connect_pen_success) + ": " + bleDevice.getName(), Toast.LENGTH_LONG).show();
        isConnectSuccess = true;
        //连接了不再继续扫描
        if (mBlePenManager.getScanState() != BleScanState.STATE_IDLE) {
            mBlePenManager.cancelScan();
        }
        mScanHandler.removeCallbacks(mScanRunnable);

    }

    @Override
    public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
        Log.i(TAG, "onDisConnected");
        isConnectSuccess = false;
        mScanHandler.removeCallbacks(mScanRunnable);
        mScanHandler.postDelayed(mScanRunnable, SCAN_DURATION);
    }

    @Override
    public void onScanStarted(boolean success) {
        Log.i(TAG, "onScanStarted");
        mLastPenName = mPreference.getPreferenceValue(PreferencesUtility.BLEPEN_NAME, PEN_DEFAULT_VALUE);
        mLastPenMac = mPreference.getPreferenceValue(PreferencesUtility.BLEPEN_MAC, PEN_DEFAULT_VALUE);
    }

    @Override
    public void onLeScan(BleDevice bleDevice) {
    }

    @Override
    public void onScanning(BleDevice bleDevice) {
        Log.i(TAG, "onScanning");
        if (bleDevice.getMac().equals(mLastPenMac) &&
                bleDevice.getName().equals(mLastPenName) &&
                !mScanStoped) {
            mBlePenManager.connect(bleDevice);
        }
    }

    @Override
    public void onScanFinished(List<BleDevice> scanResultList) {
        Log.i(TAG, "onScanFinished ");
        //一次scan结束如果还没有连接，继续scan
        if (!mLastPenMac.equals(PEN_DEFAULT_VALUE) &&
                !mLastPenName.equals(PEN_DEFAULT_VALUE) &&
//                !isConnectSuccess &&
                !ZBformApplication.sBlePenManager.getIsConnectedNow()) {
            Log.i(TAG, "onScanFinished loop");
            mScanHandler.post(mScanRunnable);
        }
    }

    public class LocalBinder extends Binder {
        public BleConnectService getService() {
            return BleConnectService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mPreference = PreferencesUtility.getInstance(BleConnectService.this);

        mLastPenName = mPreference.getPreferenceValue(PreferencesUtility.BLEPEN_NAME, PEN_DEFAULT_VALUE);
        mLastPenMac = mPreference.getPreferenceValue(PreferencesUtility.BLEPEN_MAC, PEN_DEFAULT_VALUE);

        startAutoScan();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        mBlePenManager.removeZBBleConnectCallback(this);
        mBlePenManager.removeZBBleScanCallback(this);

        mScanHandler.removeCallbacks(mScanRunnable);
        super.onDestroy();
    }
}
