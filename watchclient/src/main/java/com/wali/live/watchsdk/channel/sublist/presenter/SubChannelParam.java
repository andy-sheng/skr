package com.wali.live.watchsdk.channel.sublist.presenter;

import java.io.Serializable;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 二级页面的数据参数封装类
 */
public class SubChannelParam implements Serializable {
    // require
    private int mId;
    private String mTitle;

    // optional
    private long mChannelId;
    private String mKey;
    private long mKeyId;

    // ui related
    private int mAnimation;

    // data related
    private int mSource;

    public SubChannelParam(int id, String title, long channelId, int source) {
        mId = id;
        mTitle = title;
        mChannelId = channelId;
        mSource = source;
    }

    public SubChannelParam(int id, String title, long channelId, String key, long keyId, int animation, int source) {
        mId = id;
        mTitle = title;
        mChannelId = channelId;
        mKey = key;
        mKeyId = keyId;
        mAnimation = animation;
        mSource = source;
    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public long getChannelId() {
        return mChannelId;
    }

    public String getKey() {
        return mKey;
    }

    public long getKeyId() {
        return mKeyId;
    }

    public int getAnimation() {
        return mAnimation;
    }

    public int getSource() {
        return mSource;
    }
}