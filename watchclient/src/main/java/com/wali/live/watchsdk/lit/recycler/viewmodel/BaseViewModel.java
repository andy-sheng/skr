package com.wali.live.watchsdk.lit.recycler.viewmodel;

import java.io.Serializable;

/**
 * Created by lan on 16/6/28.
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
