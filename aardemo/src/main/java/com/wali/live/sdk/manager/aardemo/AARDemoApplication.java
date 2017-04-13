package com.wali.live.sdk.manager.aardemo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.wali.live.livesdk.live.MiLiveSdkController;

/**
 * Created by chenyong on 2017/4/6.
 */

public class AARDemoApplication extends Application {

    public static final int CHANNEL_ID = 50002;

    @Override
    public void onCreate() {
        super.onCreate();
        MiLiveSdkController.getInstance().init(this, CHANNEL_ID, "TEST SECRET");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            MultiDex.install(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
