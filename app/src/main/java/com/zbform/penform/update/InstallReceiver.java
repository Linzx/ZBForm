package com.zbform.penform.update;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;

/**
 * 安装下载接收器
 * Created by darren on 2018/8/24.
 */

public class InstallReceiver extends BroadcastReceiver {

    private static final String TAG = "InstallReceiver";


    // 安装下载接收器
    @Override public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
         long downloadApkId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
         Log.d(TAG,"will install apk");
            installApk(context,downloadApkId);
        }
    }

    // 安装Apk
    private void installApk(Context context,long downloadApkId) {
        Log.d(TAG,"install apk");
        Uri apkUri;


        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String filePath = CommonCons.APP_FILE_NAME;
            Log.d(TAG,"filePath:" + filePath);
            File apkFile = new File(filePath);
            Log.d(TAG,"apkFile:" + apkFile);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // 授予目录临时共享权限
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                String authority = context.getPackageName() + ".fileProvider";
                Log.d(TAG,"authority :" + authority);
                apkUri = FileProvider.getUriForFile(context, authority, apkFile);
            } else {
                apkUri = Uri.fromFile(apkFile);
            }
//            intent.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }catch (Exception e){
            Log.e(TAG,"安装失败");
            e.printStackTrace();
        }

    }
}