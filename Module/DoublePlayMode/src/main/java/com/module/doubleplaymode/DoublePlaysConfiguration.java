package com.module.doubleplaymode;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.common.base.ConfigModule;
import com.common.base.GlobalParams;
import com.common.base.delegate.AppLifecycles;

import java.util.List;


public class DoublePlaysConfiguration implements ConfigModule {
    public final static String TAG = "DoublePlaysConfigure";

    @Override
    public void applyOptions(GlobalParams.Builder builder) {

    }

    @Override
    public void injectAppLifecycle(List<AppLifecycles> lifecycles) {
        // AppDelegate.Lifecycle 的所有方法都会在基类Application对应的生命周期中被调用,所以在对应的方法中可以扩展一些自己需要的逻辑
        lifecycles.add(new AppLifecycles() {

            @Override
            public void attachBaseContext(@NonNull Context base) {
            }

            @Override
            public void onMainProcessCreate(@NonNull Application application) {
                Log.d(TAG, "application onCreate");

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
