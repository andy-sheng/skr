package com.wali.live.watchsdk.videothird.data.engine;

/**
 * Created by yangli on 2017/9/21.
 */
public interface IPlayerCallback<PLAYER> {

    void onPrepared(PLAYER player);

    void onCompletion(PLAYER player);

    void onSeekComplete(PLAYER player);

    void onVideoSizeChanged(PLAYER player, int width, int height);

    void onError(PLAYER player, int what, int extra);

    void onInfo(PLAYER player, int what, int extra);
}