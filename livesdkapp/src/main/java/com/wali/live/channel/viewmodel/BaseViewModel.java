package com.wali.live.channel.viewmodel;

import java.io.Serializable;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 数据模型抽象基类
 */
public abstract class BaseViewModel implements Serializable {
    protected final String TAG = getTAG();

    public <VM extends BaseViewModel> VM get() {
        return (VM) this;
    }

    protected String getTAG() {
        return getClass().getSimpleName();
    }
}
