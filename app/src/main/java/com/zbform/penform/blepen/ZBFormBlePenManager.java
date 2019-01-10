package com.zbform.penform.blepen;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.tstudy.blepenlib.BlePenManager;
import com.tstudy.blepenlib.BlePenStreamManager;
import com.tstudy.blepenlib.callback.BleGattCallback;
import com.tstudy.blepenlib.callback.BlePenStreamCallback;
import com.tstudy.blepenlib.callback.BleScanCallback;
import com.tstudy.blepenlib.data.BleDevice;
import com.tstudy.blepenlib.exception.BleException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import static android.content.Context.WINDOW_SERVICE;
import static com.tstudy.blepenlib.constant.Constant.PEN_COODINAT_MESSAGE;
import static com.tstudy.blepenlib.constant.Constant.PEN_DOWN_MESSAGE;
import static com.tstudy.blepenlib.constant.Constant.PEN_UP_MESSAGE;
import static com.tstudy.blepenlib.constant.Constant.WARN_BATTERY;
import static com.tstudy.blepenlib.constant.Constant.WARN_MEMORY;
/*
  ZBFormBlePenManager Must run in UI thread
 */
public class ZBFormBlePenManager {
    private static final String TAG = "ZBFormBlePenManager";

    private ImageView mImageView;
    private BleDevice mBleDevice;
    private StreamingController mStreamingController;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.CHINA);
    private Bitmap mBitmap;
    private int mWidth = 1920;
    private int mHeight = 1080;
    private Object mSyncObject = new Object();
    private String mBleDeviceName;
    private BlePenStreamCallback mBlePenStreamCallback;
    private String writeString;
    private boolean mCanDraw = false;

    //    private ImageView imgHold;

    private Context mContext;
    private BleScanCallback mBleScanCallback;
    private BleGattCallback mBleGattCallback;

    // 笔的一些基本信息
    private String mBleDeviceMac;
    private String mBleDeviceHwVersion;
    private String mBleDeviceSwVersion;
    private String mBleDeviceSyncNum;
    private int mBleDevicePower;
    private int mBleDeviceUsedMemory;
    private int mBleDeviceUsedBytes;
    private boolean isLowMemory = false;
    private boolean isLowBattery = false;
    private boolean isBleInitSuccess = false;

    private final int MAG_SCAN = 1;
    //    private MyHandle myHandle;
    private boolean isConnectedNow;
    private boolean isReConnected;
    private boolean isResume;
    private String mPageAddress = "0.0.0.0";
    private String mUrlData;
    private Handler mUIHander;
    private IBlePenStateCallBack mIBlePenStateCallBack;
    private IBlePenDrawCallBack mIBlePenDrawCallBack;
    private ArrayList<IZBBleGattCallback> mIZBBleGattCallbackList;

    private static ZBFormBlePenManager mZBFormBlePenManager;
    public static ZBFormBlePenManager getInstance(Context context) {
        if (mZBFormBlePenManager == null) {
            mZBFormBlePenManager = new ZBFormBlePenManager(context);
        }

        return mZBFormBlePenManager;
    }

    public interface IBlePenStateCallBack {
        void onRemainBattery(final int percent);

        void onMemoryFillLevel(final int percent, final int byteNum);

        void onNewSession(final String hardVersion, final String softVersion, final String syncNum);
    }

    public interface IBlePenDrawCallBack {

        void onPenDown();
        void onPenUp();
        void onCoordDraw(final String pageAddress, final int nX, final int nY);
        void onOffLineCoordDraw(final String pageAddress, final int nX, final int nY);
    }

    public interface IZBBleGattCallback {
        void onStartConnect();

        void onConnectFail(BleDevice bleDevice, BleException exception);

        void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status);

        void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status);
    }

    private ZBFormBlePenManager(Context context) {
        //绘图背景初始化
        //获取屏幕的宽高
        mContext = context;
        mIZBBleGattCallbackList = new ArrayList<IZBBleGattCallback>();
        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
//        if (windowManager != null) {
//            Display dis = windowManager.getDefaultDisplay();
//            mWidth = dis.getWidth();
//            mHeight = dis.getHeight();
//        }

        initListener();
        initBlePenStream();
    }

    public void setBlePenStateCallBack(IBlePenStateCallBack callBack) {
        mIBlePenStateCallBack = callBack;
    }

    public void setIBlePenDrawCallBack(IBlePenDrawCallBack callBack) {
        mIBlePenDrawCallBack = callBack;
    }

    public void setZBBleGattCallback(IZBBleGattCallback callback){
        mIZBBleGattCallbackList.add(callback);
    }

    public void setBleDevice(BleDevice device) {
        Log.i("whd", "setBleDevice");
        mBleDevice = device;
        mBleDeviceName = mBleDevice.getName();
        mBleDeviceMac = mBleDevice.getMac();
        initBlePenStream();
        BlePenStreamManager.getInstance().getPenInfo();
        BlePenStreamManager.getInstance().setStandMode();
    }

    public void setDrawView(ImageView view, Bitmap bitmap, int width, int height) {
        mImageView = view;
        mImageView.getDrawingCache(true);
        mWidth = width;
        mHeight = height;
        mStreamingController = new StreamingController(mWidth, mHeight);
        mBitmap = bitmap;
//        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
//        mImageView.setImageBitmap(mBitmap);
    }

    public void startDraw(){
        mCanDraw = true;
        BlePenStreamManager.getInstance().setStandMode();
    }

    public void stopDraw(){
        mCanDraw = false;
        BlePenStreamManager.getInstance().setHoverMode();
    }

    public void connect(final BleDevice bleDevice, IZBBleGattCallback callback) {
        if (callback != null) {
            mIZBBleGattCallbackList.add(callback);
        }
        BlePenManager.getInstance().connect(bleDevice, mBleGattCallback);
    }

    private void initBlePenStream() {
        Log.i("whd", "initBle");
        if (mBleDevice != null && BlePenManager.getInstance().isConnected(mBleDevice)) {
            Log.i("whd", "initBle2");
            isConnectedNow = true;
            //开启笔输出流
            BlePenStreamManager.getInstance().openPenStream(mBleDevice, mBlePenStreamCallback);
        }
    }

    private void initListener() {
        mBlePenStreamCallback = new BlePenStreamCallback() {

            @Override
            public void onOpenPenStreamSuccess() {
                Log.d(TAG, "onOpenPenStreamSuccess: ");
            }

            @Override
            public void onOpenPenStreamFailure(BleException exception) {
                BlePenManager.getInstance().disconnect(mBleDevice);
                Log.d(TAG, "onOpenPenStreamFailure: " + exception.getDescription());
            }

            @Override
            public void onRemainBattery(final int percent) {
                Log.d(TAG, "onRemainBattery: " + percent + "%");
                mBleDevicePower = percent;
                if (mIBlePenStateCallBack != null) {
                    mIBlePenStateCallBack.onRemainBattery(percent);
                }

            }

            @Override
            public void onMemoryFillLevel(final int percent, final int byteNum) {
                Log.d(TAG, "onMemoryFillLevel: " + percent + "%");

                mBleDeviceUsedMemory = percent;
                mBleDeviceUsedBytes = byteNum;

                if (mIBlePenStateCallBack != null) {
                    mIBlePenStateCallBack.onMemoryFillLevel(percent, byteNum);
                }

            }

            @Override
            public void onCoordDraw(final int state, final String pageAddress, final int nX, final int nY, final int nForce, int strokeNum, final long time) {
                Log.i("whd", "onCoordDraw1");
                switch (state) {
                    case PEN_DOWN_MESSAGE:
                        writeString = "down";
                        synchronized (mSyncObject) {
                            if (mStreamingController != null) {
                                mStreamingController.penDown();
                            }
                            if (mIBlePenDrawCallBack != null) {
                                mIBlePenDrawCallBack.onPenDown();
                            }
                            Log.i("whd", "PEN_DOWN_MESSAGE pageAddress="+pageAddress);
                        }
                        break;
                    case PEN_COODINAT_MESSAGE:
                        writeString = "move";
                        synchronized (mSyncObject) {
                            if (nX > 0 && nY > 0 && nForce > 0) {
                                if (mStreamingController != null) {
                                    mStreamingController.addCoordinate(nX, nY, nForce, pageAddress);
                                }
                            }

                            if (mIBlePenDrawCallBack != null) {
                                mIBlePenDrawCallBack.onCoordDraw(pageAddress,nX,nY);
                            }
                        }

                        Log.i("whd", "onCoordDraw pageAddress="+pageAddress);
                        if (!TextUtils.isEmpty(pageAddress)) {
                            if (!"0.0.0.0".equals(mPageAddress) && !mPageAddress.equals(pageAddress)) {
                                Log.i("whd", "onCoordDraw drawClear=");
//                                drawClear();
                            }
                            mPageAddress = pageAddress;
                        }
                        break;
                    case PEN_UP_MESSAGE:
                        writeString = " up ";
                        synchronized (mSyncObject) {
                            if (mStreamingController != null) {
                                mStreamingController.penUp();
                            }
                            if (mIBlePenDrawCallBack != null) {
                                mIBlePenDrawCallBack.onPenUp();
                            }
                        }

                        break;
                    default:
                        writeString = " up ";
                }

                Log.i("whd", "onCoordDraw2");

                if (mCanDraw) {
                    drawBitmap();
                }
                Log.d(TAG, "onCoordDraw: " + writeString);
            }

            @Override
            public void onOffLineCoordDraw(final int state, final String pageAddress, final int nX, final int nY, final int nForce, int strokeNum, final long time, final int offLineDataAllSize, final int offLineDateCurrentSize) {

                Log.i("whd", "onOffLineCoordDraw");
                switch (state) {
                    case PEN_DOWN_MESSAGE:
                        writeString = "outline down";
                        synchronized (mSyncObject) {
                            if (mStreamingController != null) {
                                mStreamingController.penDown();
                            }
                        }
                        break;
                    case PEN_COODINAT_MESSAGE:
                        writeString = "outline move";
                        synchronized (mSyncObject) {
                            if (nX > 0 && nY > 0 && nForce > 0) {
                                if (mStreamingController != null) {
                                    mStreamingController.addCoordinate(nX, nY, nForce, pageAddress);
                                }
                            }
                            if (mIBlePenDrawCallBack != null) {
                                mIBlePenDrawCallBack.onOffLineCoordDraw(pageAddress,nX,nY);
                            }
                        }
                        if (!TextUtils.isEmpty(pageAddress)) {
                            if (!"0.0.0.0".equals(mPageAddress) && !mPageAddress.equals(pageAddress)) {
                                drawClear();
                            }
                            mPageAddress = pageAddress;
                        }

                        break;
                    case PEN_UP_MESSAGE:
                        writeString = "outline  up ";
                        synchronized (mSyncObject) {
                            if (mStreamingController != null) {
                                mStreamingController.penUp();
                            }
                        }
                        break;
                    default:
                        writeString = "outline  up ";
                }
                Log.i("whd", "onOffLineCoordDraw2");
                if (mCanDraw) {
                    drawBitmap();
                }

            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device) {
                Log.d(TAG, "onDisConnected: isActiveDisConnected   " + isActiveDisConnected);
                //笔端关机按钮时回调重连
                if (isActiveDisConnected) {
                    //  txt_connect_status.setText(getString(R.string.active_disconnected));
//                    if (isResume) {
//                        isConnectedNow = false;
//                        BlePenStreamManager.getInstance().closePenStream();
//                        BlePenManager.getInstance().disconnect(bleDevice);
//                        if (!BlePenManager.getInstance().isConnected(bleDevice)) {
//                            startScan();
//                        }
//                    }

                } else {
//                    txt_connect_status.setText(getString(R.string.disconnected));
                    BlePenStreamManager.getInstance().closePenStream();
                    BlePenManager.getInstance().disconnect(mBleDevice);

                }
            }

            @Override
            public void onWarnActiveReport(int statusNum) {
                //0x05  电池电量低警告  0x08 存储空间警告
                switch (statusNum) {
                    case WARN_BATTERY:
                        isLowBattery = true;
                        Log.d(TAG, "handleActiveReport: 电池电量低警告");
                        break;
                    case WARN_MEMORY:
                        isLowMemory = true;
                        Log.d(TAG, "handleActiveReport: 存储空间警告");
                        break;
                    default:
                }
            }

            @Override
            public void onNewSession(final String hardVersion, final String softVersion, final String syncNum) {
                mBleDeviceHwVersion = hardVersion;
                mBleDeviceSwVersion = softVersion;
                mBleDeviceSyncNum = syncNum;

                Log.i(TAG, "hardVersion：" + hardVersion + "  softVersion:" + softVersion + "   syncNum:" + syncNum);
                if (mIBlePenStateCallBack != null) {
                    mIBlePenStateCallBack.onNewSession(hardVersion, softVersion, syncNum);
                }
            }

            @Override
            public void onCurrentTime(long penTime) {
                //如果与当前系统时间差一分钟以上，可以同步笔端时间
                long timeMillis = System.currentTimeMillis();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.CHINA);

//                if (Math.abs(penTime - timeMillis) > 1000 * 60) {
//                    BlePenStreamManager.getInstance().setPenRTC(timeMillis);
//                }
                String formatTime = simpleDateFormat.format(new Date(penTime));
                Toast.makeText(mContext, formatTime, Toast.LENGTH_SHORT).show();
            }
        };

        //断开重连 扫描回调
        mBleScanCallback = new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
//                if (progressDialog != null && !progressDialog.isShowing()) {
//                    progressDialog.show();
//                }
//                txt_connect_status.setText(getString(R.string.start_scan));
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                if (bleDevice != null && bleDevice.getMac() != null) {
                    if (bleDevice.getMac().equals(mBleDeviceMac)) {
//                        connect(bleDevice);
                    }
                }
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
            }
        };

        //连接回调
        mBleGattCallback = new BleGattCallback() {
            @Override
            public void onStartConnect() {
                for (IZBBleGattCallback callback : mIZBBleGattCallbackList){
                    if (callback != null){
                        callback.onStartConnect();
                    }
                }
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                for (IZBBleGattCallback callback : mIZBBleGattCallbackList){
                    if (callback != null){
                        callback.onConnectFail(bleDevice,exception);
                    }
                }
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                for (IZBBleGattCallback callback : mIZBBleGattCallbackList) {
                    if (callback != null) {
                        callback.onConnectSuccess(bleDevice, gatt,status);
                    }
                }
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                for (IZBBleGattCallback callback : mIZBBleGattCallbackList) {
                    if (callback != null) {
                        callback.onDisConnected(isActiveDisConnected, bleDevice, gatt, status);
                    }
                }
            }
        };

    }

    private synchronized void drawClear() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;

        }
        System.gc();
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        synchronized (TouchImageView.class) {
            mImageView.setImageBitmap(mBitmap);
            mImageView.invalidate();
        }
    }

    private synchronized void drawBitmap() {
        Path path = null;
        Paint paint = null;
        if (mStreamingController == null) {
            return;
        }
        if (mBitmap != null) {
            mImageView.setImageBitmap(mBitmap);
        }

        synchronized (mSyncObject) {
            path = mStreamingController.getPath();
            paint = mStreamingController.getPaint();

            if (mBitmap != null) {
                Canvas c = new Canvas(mBitmap);
                c.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
                c.drawPath(path, paint);

                RectF bounds = new RectF();
                path.computeBounds(bounds, true);
                Rect dirty = new Rect((int) bounds.left, (int) bounds.top, (int) bounds.right, (int) bounds.bottom);
                mImageView.invalidate(dirty);

            }
        }
    }

    public String getBleDeviceName() {
        return mBleDeviceName;
    }

    public String getBleDeviceMac() {
        return mBleDeviceMac;
    }

    public int getBleDevicePower() {
        return mBleDevicePower;
    }

    public String getBleDeviceSwVersion() {
        return mBleDeviceSwVersion;
    }

    public String getBleDeviceHwVersion() {
        return mBleDeviceHwVersion;
    }

    public String getBleDeviceSyncNum() {
        return mBleDeviceSyncNum;
    }

    public boolean isLowMemory() {
        return isLowMemory;
    }

    public boolean isLowBattery() {
        return isLowBattery;
    }

    public int getBleDeviceUsedMemory() {
        return mBleDeviceUsedMemory;
    }

    public int getBleDeviceUsedBytes() {
        return mBleDeviceUsedBytes;
    }

    public boolean isBleInitSuccess() {
        return isBleInitSuccess;
    }

    public void setBleInitSuccess(boolean bleInitSuccess) {
        isBleInitSuccess = bleInitSuccess;
    }

    public boolean isConnectedBleDevice(){
        return BlePenManager.getInstance().isConnected(mBleDevice);
    }

    public Bitmap getDrawBitmap(){
        return mBitmap;
    }

    public boolean getCanDraw(){
        return mCanDraw;
    }
}
