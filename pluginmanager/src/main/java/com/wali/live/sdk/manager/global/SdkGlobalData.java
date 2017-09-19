package com.wali.live.sdk.manager.global;

import android.app.Application;

/**
 * Created by chengsimin on 2016/12/8.
 */
public class SdkGlobalData {
    private static Application sApp;

    public static void setApplication(Application app) {
        sApp = app;
    }

    public static Application app() {
        return sApp;
    }
}
