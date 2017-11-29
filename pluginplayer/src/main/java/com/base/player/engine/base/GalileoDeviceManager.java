package com.base.player.engine.base;

import android.content.Context;

import com.xiaomi.devicemanager.DeviceManager;

/**
 * Created by chenyong on 2017/1/22.
 */

public enum GalileoDeviceManager {
    INSTANCE;

    private DeviceManager mDeviceManager;
    private int mInitCount = 0;

    public void init(Context context) {
        if (mInitCount == 0) {
            mDeviceManager = new DeviceManager();
            boolean success = mDeviceManager.constructDeviceManager(context);
            if (!success) {
                mDeviceManager = null;
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
            mDeviceManager.stopAudioDevice();
            mDeviceManager.stopCamera();
            mDeviceManager.destructDeviceManager();
            mDeviceManager = null;
        }
        mInitCount--;
    }

    public DeviceManager getDeviceManger() {
        return mDeviceManager;
    }
}
