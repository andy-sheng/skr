package com.wali.live.watchsdk.channel.holder.listener;

/**
 * Created by zhaomin on 16-7-19.
 *
 * @module 频道
 * @description 适配器内的跳转接口
 */
public interface JumpListener {
    /**
     * 跳转逻辑：scheme
     */
    void jumpScheme(String uri);

    /**
     * 跳转逻辑：liveShowList
     */
    void jumpWatchWithLiveList(int position);

}
