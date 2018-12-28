package com.zbform.penform.blepen;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Handler;

import static android.content.Context.WINDOW_SERVICE;
import static com.tstudy.blepenlib.constant.Constant.PEN_COODINAT_MESSAGE;
import static com.tstudy.blepenlib.constant.Constant.PEN_DOWN_MESSAGE;
import static com.tstudy.blepenlib.constant.Constant.PEN_UP_MESSAGE;
import static com.tstudy.blepenlib.constant.Constant.WARN_BATTERY;
import static com.tstudy.blepenlib.constant.Constant.WARN_MEMORY;

public class ZBFormBlePenManager {
    private static ZBFormBlePenManager mZBFormBlePenManager;
    public static ZBFormBlePenManager getInstance(Context context){
        if(mZBFormBlePenManager == null){
            mZBFormBlePenManager = new ZBFormBlePenManager(context);
        }

        return mZBFormBlePenManager;
    }

    public interface IZBFormBlePenCallBack{
        /*

         */
        public void onRemainBattery(final int percent);
        public void onMemoryFillLevel(final int percent, final int byteNum);
        public void onReadPageAddress(String address);
    }

    public static final String KEY_DATA = "DEVICE_DATA";
    private TouchImageView mImageView;
    private BleDevice bleDevice;
    private static final String TAG = "DrawActivity_tag";
    private StreamingController mStreamingController;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.CHINA);
    private Bitmap mBitmap;
    private int mWidth = 1920;
    private int mHeight = 1080;
    private Object mSyncObject = new Object();
    private String mBleDeviceName;
    private BlePenStreamCallback mBlePenStreamCallback;
    private String writeString;
    private boolean openStandardMode;
//    private ImageView imgHold;
    private Context mContext;
    private BleScanCallback mBleScanCallback;
    private BleGattCallback mBleGattCallback;
    private String mBleDeviceMac;
    private final int MAG_SCAN = 1;
//    private MyHandle myHandle;
    private boolean isConnectedNow;
    private boolean isReConnected;
    private boolean isResume;
    private String mPageAddress = "0.0.0.0";
    private String mUrlData;
    private Handler mUIHander;
    private IZBFormBlePenCallBack mIZBFormBlePenCallBack;

    private ZBFormBlePenManager(Context context) {
        //绘图背景初始化
        //获取屏幕的宽高
        mContext =  context;
        WindowManager systemService = (WindowManager) context.getSystemService(WINDOW_SERVICE);
//        if (systemService != null) {
//            Display dis = systemService.getDefaultDisplay();
//            mWidth = dis.getWidth();
//            mHeight = dis.getHeight();
//        }



        initListener();
        initBle();
    }

    public void setIZBFormBlePenCallBack(IZBFormBlePenCallBack callBack) {
        mIZBFormBlePenCallBack = callBack;
    }

    public void setBleDevice(BleDevice device){
        Log.i("whd","setBleDevice");
        bleDevice = device;
        mBleDeviceName = bleDevice.getName();
        mBleDeviceMac = bleDevice.getMac();
        initBle();

        BlePenStreamManager.getInstance().setStandMode();
    }

    public void setDrawView(TouchImageView view, Bitmap bitmap, int width, int height){
        mImageView = view;
        mImageView.getDrawingCache(true);
        mWidth = width;
        mHeight = height;
        mStreamingController = new StreamingController(mWidth, mHeight);
        mBitmap =bitmap;
//        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
//        mImageView.setImageBitmap(mBitmap);
    }

    private void initListener() {
        mBlePenStreamCallback = new BlePenStreamCallback() {

            @Override
            public void onOpenPenStreamSuccess() {
                Log.d(TAG, "onOpenPenStreamSuccess: ");
            }

            @Override
            public void onOpenPenStreamFailure(BleException exception) {
                BlePenManager.getInstance().disconnect(bleDevice);
                Log.d(TAG, "onOpenPenStreamFailure: " + exception.getDescription());
            }

            @Override
            public void onRemainBattery(final int percent) {
                Log.d(TAG, "onRemainBattery: " + percent + "%");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        txt_battery.setText(percent + "%");
//                    }
//                });
                if (mIZBFormBlePenCallBack != null){
                    mIZBFormBlePenCallBack.onRemainBattery(percent);
                }

            }

            @Override
            public void onMemoryFillLevel(final int percent, final int byteNum) {
                Log.d(TAG, "onMemoryFillLevel: " + percent + "%");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        txt_memory.setText(percent + "%，已使用字节数：" + byteNum);
//                    }
//                });
                if (mIZBFormBlePenCallBack != null){
                    mIZBFormBlePenCallBack.onMemoryFillLevel(percent,byteNum);
                }

            }

            @Override
            public void onCoordDraw(final int state, final String pageAddress, final int nX, final int nY, final int nForce, int strokeNum, final long time) {
                //runOnUiThread(new Runnable() {
                   // @Override
                   // public void run() {
                Log.i("whd","onCoordDraw1");
                        switch (state) {
                            case PEN_DOWN_MESSAGE:
                                writeString = "down";
                                synchronized (mSyncObject) {
                                    mStreamingController.penDown();
                                }
                                break;
                            case PEN_COODINAT_MESSAGE:
                                writeString = "move";
                                synchronized (mSyncObject) {
                                    if (nX > 0 && nY > 0 && nForce > 0) {
                                        mStreamingController.addCoordinate(nX, nY, nForce, pageAddress);
                                    }
                                }
                                if (!TextUtils.isEmpty(pageAddress)){
                                    if (!"0.0.0.0".equals(mPageAddress) && !mPageAddress.equals(pageAddress)) {
                                        drawClear();
                                    }
                                    mPageAddress = pageAddress;
                                    if (mIZBFormBlePenCallBack != null){
                                        mIZBFormBlePenCallBack.onReadPageAddress(mPageAddress);
                                    }
                                }

                                break;
                            case PEN_UP_MESSAGE:
                                writeString = " up ";
                                synchronized (mSyncObject) {
                                    mStreamingController.penUp();
                                }

                                break;
                            default:
                                writeString = " up ";
                        }

//                        if (myProgress.getVisibility() == View.VISIBLE) {
//                            myProgress.setVisibility(View.GONE);
//                            txt_progress.setVisibility(View.GONE);
//                            Log.d(TAG, "run:离线传输完毕 ");
                       // }
                Log.i("whd","onCoordDraw2");

                        drawBitmap();
                        Log.d(TAG, "onCoordDraw: " + writeString);
//                        txt_time.setText("time:" + simpleDateFormat.format(new Date(time)));
//                        txt_write.setText(writeString);
//                        txt_coordinate.setText(nX + "/" + nY);
//                        txt_force.setText(nForce + "");
//                        txt_paper_addres.setText(pageAddress);
//                        imgHold.setTranslationX(nX * mStreamingController.m_scaleX - imgHold.getHeight() / 2);
//                        imgHold.setTranslationY(nY * mStreamingController.m_scaleY - imgHold.getWidth());

                  //  }
                //});
            }

            @Override
            public void onOffLineCoordDraw(final int state, final String pageAddress, final int nX, final int nY, final int nForce, int strokeNum, final long time, final int offLineDataAllSize, final int offLineDateCurrentSize) {
                ///runOnUiThread(new Runnable() {
                  //  @Override
                    //public void run() {
                Log.i("whd","onOffLineCoordDraw");
                        switch (state) {
                            case PEN_DOWN_MESSAGE:
                                writeString = "outline down";
                                synchronized (mSyncObject) {
                                    mStreamingController.penDown();
                                }
                                break;
                            case PEN_COODINAT_MESSAGE:
                                writeString = "outline move";
                                synchronized (mSyncObject) {
                                    if (nX > 0 && nY > 0 && nForce > 0) {
                                        mStreamingController.addCoordinate(nX, nY, nForce, pageAddress);
                                    }
                                }
                                if (!TextUtils.isEmpty(pageAddress)){
                                    if (!"0.0.0.0".equals(mPageAddress) && !mPageAddress.equals(pageAddress)) {
                                        drawClear();
                                    }
                                    mPageAddress = pageAddress;
                                }

                                break;
                            case PEN_UP_MESSAGE:
                                writeString = "outline  up ";
                                synchronized (mSyncObject) {
                                    mStreamingController.penUp();
                                }
                                break;
                            default:
                                writeString = "outline  up ";
                        }
                Log.i("whd","onOffLineCoordDraw2");
                        drawBitmap();
//                        if (myProgress.getVisibility() == View.GONE) {
//                            Log.d(TAG, "run:离线传输开始 ");
//                            myProgress.setVisibility(View.VISIBLE);
//                            txt_progress.setVisibility(View.VISIBLE);
//                        }
//                        myProgress.setProgress(100 * offLineDateCurrentSize / offLineDataAllSize);
//                        txt_progress.setText(" 上传进度：" + offLineDateCurrentSize + "/" + offLineDataAllSize);
//                        Log.d(TAG, "onOffLineCoordDraw: " + writeString);
//
//                        txt_time.setText("time:" + simpleDateFormat.format(new Date(time)));
//                        txt_write.setText(writeString);
//                        txt_coordinate.setText(nX + "/" + nY);
//                        txt_force.setText(nForce + "");
//                        txt_paper_addres.setText(pageAddress);
//                        imgHold.setTranslationX(nX * mStreamingController.m_scaleX - imgHold.getHeight() / 2);
//                        imgHold.setTranslationY(nY * mStreamingController.m_scaleY - imgHold.getWidth());

//                        if (offLineDateCurrentSize == offLineDataAllSize && myProgress.getVisibility() == View.VISIBLE) {
//                            myProgress.setVisibility(View.GONE);
//                            txt_progress.setVisibility(View.GONE);
//                            Log.d(TAG, "run:离线传输完毕 ");
//                        }
                //    }
                //});
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
                    BlePenManager.getInstance().disconnect(bleDevice);

                }
            }

            @Override
            public void onWarnActiveReport(int statusNum) {
                //0x05  电池电量低警告  0x08 存储空间警告
                switch (statusNum) {
                    case WARN_BATTERY:
                        Log.d(TAG, "handleActiveReport: 电池电量低警告");
                        break;
                    case WARN_MEMORY:
                        Log.d(TAG, "handleActiveReport: 存储空间警告");
                        break;
                    default:
                }
            }

            @Override
            public void onNewSession(final String hardVersion, final String softVersion, final String syncNum) {
                //Version serial number
                final String msg = "hardVersion：" + hardVersion + "  softVersion:" + softVersion + "   syncNum:" + syncNum;
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        txt_errorCount.setText(hardVersion);
//                        if (!TextUtils.isEmpty(softVersion)) {
//                            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
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
                        connect(bleDevice);
                    }
                }
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
//                txt_connect_status.setText(getString(R.string.stop_scan));
//                if (progressDialog != null && progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
            }
        };

        //连接回调
        mBleGattCallback = new BleGattCallback() {
            @Override
            public void onStartConnect() {
//                if (progressDialog != null && !progressDialog.isShowing()) {
//                    progressDialog.show();
//                }
//                txt_connect_status.setText(getString(R.string.connect));

            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {

//                if (progressDialog != null && progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
//                txt_connect_status.setText(getString(R.string.connect_fail));
//
//                Toast.makeText(mContext, getString(R.string.connect_fail), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
//                txt_connect_status.setText(getString(R.string.connected));
//
//                if (progressDialog != null && progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
//                openStandardMode = true;
//                btn_hover_mode.setText("获取悬浮坐标");
//                imgHold.setVisibility(View.GONE);
//                isReConnected = true;
                initBle();

            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
//                if (progressDialog != null && progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
//
//                if (isActiveDisConnected) {
//                    Toast.makeText(mContext, getString(R.string.active_disconnected), Toast.LENGTH_SHORT).show();
//                    txt_connect_status.setText(getString(R.string.active_disconnected));
//
//                } else {
//                    Toast.makeText(mContext, getString(R.string.disconnected), Toast.LENGTH_SHORT).show();
//                    txt_connect_status.setText(getString(R.string.disconnected));
//                }
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

    private void connect(final BleDevice bleDevice) {

        BlePenManager.getInstance().connect(bleDevice, mBleGattCallback);
    }

    private void initBle() {
        Log.i("whd","initBle");
        if (BlePenManager.getInstance().isConnected(bleDevice)) {
            Log.i("whd","initBle2");
            isConnectedNow = true;
            //开启笔输出流
            BlePenStreamManager.getInstance().openPenStream(bleDevice, mBlePenStreamCallback);
//            myHandle.removeMessages(MAG_SCAN);
        }
    }

}
