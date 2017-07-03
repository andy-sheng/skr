package com.wali.live.watchsdk.channel.presenter;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 频道的Presenter接口，提供开始，结束的接口
 */
public interface IChannelPresenter {
    void setChannelId(long channelId);

    void start();

    void stop();
}