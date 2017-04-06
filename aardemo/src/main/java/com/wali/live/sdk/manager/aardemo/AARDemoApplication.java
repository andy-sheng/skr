package com.wali.live.sdk.manager.aardemo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.base.log.MyLog;

/**
 * Created by chenyong on 2017/4/6.
 */

public class AARDemoApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            MultiDex.install(this);
        } catch (Exception e) {
            MyLog.e(e);
        }
    }
}
