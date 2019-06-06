/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.common.base;

import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.common.base.delegate.AppDelegate;
import com.common.base.delegate.AppLifecycles;
import com.common.base.delegate.PluginAppDelegate;
import com.common.log.MyLog;
import com.common.utils.U;
import com.component.busilib.BusiLibConfiguration;

import java.util.List;


/**
 * ================================================
 * 本框架由 MVP + Dagger2 + Retrofit + RxJava + Androideventbus + Butterknife 组成
 *
 * @see <a href="https://github.com/JessYanCoding/MVPArms/wiki">请配合官方 Wiki 文档学习本框架</a>
 * Created by JessYan on 22/03/2016
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class BaseApplication extends Application {
    private AppLifecycles mAppDelegate;
    private PluginAppDelegate mPluginAppDelegate = new PluginAppDelegate();

    public String TAG = "BaseApplication";

    /**
     * 这里会在 {@link BaseApplication#onCreate} 之前被调用,可以做一些较早的初始化
     * 常用于 MultiDex 以及插件化框架的初始化
     *
     * @param base
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        TAG += hashCode();
        /**
         * 这里可以了解一下 多dex 安装的原理
         */
        MultiDex.install(this);
        //MultiDex.install(this);
        if (mPluginAppDelegate != null) {
            mPluginAppDelegate.attachBaseContext(this);
        }
        U.setApp(this);
        // 不能用MyLog 它还没初始化好
        MyLog.w(TAG,"attachBaseContext begin");
//        U.getToastUtil().showShort(TAG + " attachBaseContext");
        // 判断是不是主进程，主进程才需要后续逻辑
        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            MyLog.d(TAG, "activityManager == null return");
            return;
        }
        int pid = android.os.Process.myPid();
        List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid != pid) {
                continue;
            }
            // DroidPlugin 会新建进程 所以不是核心进程
            Log.d(TAG, "getPackageName=" + getPackageName() + "  info.processName=" + info.processName);
            U.setProcessName(info.processName);
            if (getPackageName().equals(info.processName)) {
                U.setCoreProcess(true);
            } else {
                U.setCoreProcess(false);
            }
            break;
        }

        if (mAppDelegate == null) {
            this.mAppDelegate = new AppDelegate(base);
        }
        this.mAppDelegate.attachBaseContext(base);
        // 这里耗费 50 ms
        MyLog.w(TAG,"attachBaseContext over");
        //MyLog.e(TAG,BusiLibConfiguration.TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.w(TAG,"onCreate begin");
        // Normal app init code...

        if (mPluginAppDelegate != null) {
            mPluginAppDelegate.onCreate(this);
        }
        // 为了 milink 的初始化
//        Global.init(this, getClientAppInfo());
        if (U.isCoreProcess()) {
            if (mAppDelegate != null) {
                this.mAppDelegate.onMainProcessCreate(this);
            }
        } else {
            if (mAppDelegate != null) {
                this.mAppDelegate.onOtherProcessCreate(this);
            }
        }
        MyLog.w(TAG,"onCreate over");
    }

//    private ClientAppInfo getClientAppInfo() {
//        return new ClientAppInfo.Builder(10008)
//                .setAppName("WALI_LIVE_SDK")
//                .setPackageName("com.mi.liveassistant")
//                .setReleaseChannel("DEBUG")
//                .setVersionName("4.51.1")
//                .setVersionCode(451001)
//                .setLanguageCode("ZH-CN")
//                .setServiceProcessName("com.mi.liveassistant:remote")
//                .build();
//    }

    /**
     * 在模拟环境中程序终止时会被调用
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
        if (mPluginAppDelegate != null) {
            mPluginAppDelegate.onTerminate(this);
        }
        if (U.isCoreProcess()) {
            if (mAppDelegate != null) {
                this.mAppDelegate.onTerminate(this);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mPluginAppDelegate != null) {
            mPluginAppDelegate.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mPluginAppDelegate != null) {
            mPluginAppDelegate.onLowMemory();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (mPluginAppDelegate != null) {
            mPluginAppDelegate.onTrimMemory(level);
        }
    }

    @Override
    public Intent registerReceiver(
            BroadcastReceiver receiver, IntentFilter filter) {
        MyLog.w(TAG,"registerReceiver" + " receiver=" + receiver + " filter=" + filter);
        return super.registerReceiver(receiver, filter);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        MyLog.w(TAG,"registerReceiver" + " receiver=" + receiver + " filter=" + filter + " broadcastPermission=" + broadcastPermission + " scheduler=" + scheduler);
        return super.registerReceiver(receiver, filter, broadcastPermission, scheduler);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler, int flags) {
        MyLog.w(TAG,"registerReceiver" + " receiver=" + receiver + " filter=" + filter + " broadcastPermission=" + broadcastPermission + " scheduler=" + scheduler + " flags=" + flags);
        return super.registerReceiver(receiver, filter, broadcastPermission, scheduler, flags);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        MyLog.w(TAG,"unregisterReceiver" + " receiver=" + receiver);
        super.unregisterReceiver(receiver);
    }
}
