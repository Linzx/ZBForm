package com.zbform.penform.activity;

/**
 * Created by isaac on 2018/8/2.
 */
public interface MusicStateListener {

    /**
     * 更新歌曲状态信息
     */
     void updateTrackInfo();

     void updateTime();

     void changeTheme();

     void reloadAdapter();
}
