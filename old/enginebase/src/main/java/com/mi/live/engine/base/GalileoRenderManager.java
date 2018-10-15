package com.mi.live.engine.base;

import android.content.Context;

import com.xiaomi.rendermanager.RenderManager;

/**
 * Created by chenyong on 2017/2/7.
 */

public enum GalileoRenderManager {
    INSTANCE;

    private RenderManager mRenderManager;
    private int mInitCount = 0;

    public void init(Context context) {
        if (mInitCount == 0) {
            mRenderManager = new RenderManager();
            boolean success = mRenderManager.constructRenderManager(context);
            if (!success) {
                mRenderManager = null;
                return;
            }
        }
        mInitCount++;
    }

    public void destroy() {
        if (mInitCount == 0) {
            return;
        }
        if (mInitCount == 1) {
            mRenderManager.destructRenderManager();
            mRenderManager = null;
        }
        mInitCount--;
    }

    public RenderManager getRenderManager() {
        return mRenderManager;
    }
}
