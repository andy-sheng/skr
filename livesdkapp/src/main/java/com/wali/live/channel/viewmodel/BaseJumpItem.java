package com.wali.live.channel.viewmodel;

import java.io.Serializable;

/**
 * Created by lan on 16/9/14.
 *
 * @module 频道
 */
public abstract class BaseJumpItem implements Serializable {
    protected final String TAG = getTAG();

    protected String mSchemeUri;

    public String getTAG() {
        return getClass().getSimpleName();
    }

    public String getSchemeUri() {
        return mSchemeUri;
    }
}
