package com.zbform.penform.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.zbform.penform.R;
//import com.zbform.penform.service.MediaService;
//import com.zbform.penform.service.MusicPlayer;
import com.zbform.penform.util.IConstants;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


/**
 */
public class BaseActivity extends AppCompatActivity implements ServiceConnection {

    private String TAG = "BaseActivity";


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mToken = MusicPlayer.bindToService(this, this);

        IntentFilter f = new IntentFilter();
//        f.addAction(MediaService.PLAYSTATE_CHANGED);
//        f.addAction(MediaService.META_CHANGED);
//        f.addAction(MediaService.QUEUE_CHANGED);
//        f.addAction(IConstants.MUSIC_COUNT_CHANGED);
//        f.addAction(MediaService.TRACK_PREPARED);
//        f.addAction(MediaService.BUFFER_UP);
//        f.addAction(IConstants.EMPTY_LIST);
//        f.addAction(MediaService.MUSIC_CHANGED);
//        f.addAction(MediaService.LRC_UPDATED);
//        f.addAction(IConstants.PLAYLIST_COUNT_CHANGED);
//        f.addAction(MediaService.MUSIC_LODING);
//        registerReceiver(mPlaybackStatus, new IntentFilter(f));
//        showQuickControl(true);
    }


    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        //mService = MediaAidlInterface.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
//        mService = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        unbindService();
        try {
        } catch (final Throwable e) {
        }
//        mMusicListener.clear();

    }

    public void unbindService() {
//        if (mToken != null) {
//            MusicPlayer.unbindFromService(mToken);
//            mToken = null;
//        }
    }

}
