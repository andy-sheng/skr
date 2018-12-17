package com.module.msg;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;

import com.common.base.ConfigModule;
import com.common.base.GlobalParams;
import com.common.base.delegate.AppLifecycles;

import java.util.List;

public class RongIMConfiguration implements ConfigModule {

    public final static String TAG = "RongIMConfiguration";

    @Override
    public void applyOptions(GlobalParams.Builder builder) {

    }

    @Override
    public void injectAppLifecycle(List<AppLifecycles> lifecycles) {
        lifecycles.add(new AppLifecycles() {
            @Override
            public void attachBaseContext(@NonNull Context base) {

            }

            @Override
            public void onMainProcessCreate(@NonNull Application application) {
                RongMsgManager.getInstance().init(application);
            }

            @Override
            public void onOtherProcessCreate(@NonNull Application application) {

            }


            @Override
            public void onTerminate(@NonNull Application application) {

            }
        });
    }

    @Override
    public void injectActivityLifecycle(List<Application.ActivityLifecycleCallbacks> lifecycles) {

    }

    @Override
    public void injectFragmentLifecycle(List<FragmentManager.FragmentLifecycleCallbacks> lifecycles) {

    }

}
