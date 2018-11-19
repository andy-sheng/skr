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
package com.common.base.delegate;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.util.Log;

import com.common.base.BuildConfig;
import com.common.integration.ConfigModule;
import com.common.utils.U;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.RePluginCallbacks;
import com.qihoo360.replugin.RePluginConfig;
import com.qihoo360.replugin.RePluginEventCallbacks;

import java.util.ArrayList;
import java.util.List;


/**
 * 负责 插件化框架的初始化工作
 */
public class PluginAppDelegate {

    public final static String TAG = "PluginAppDelegate";

    public void attachBaseContext(@NonNull Application base) {
        // DroidPlugin 必须在这初始化， 因为会新起进程，两边的进程都要初始化到
//        com.morgoo.droidplugin.PluginHelper.getInstance().applicationAttachBaseContext(base);

            RePluginConfig c = createConfig(base);
            RePlugin.App.attachBaseContext(base, c);
    }

    public void onCreate(@NonNull Application application) {
        //        com.morgoo.droidplugin.PluginHelper.getInstance().applicationOnCreate(getBaseContext());
            RePlugin.App.onCreate();
    }

    public void onTerminate(@NonNull Application application) {
    }

    public void onConfigurationChanged(Configuration newConfig) {
            // 如果App的minSdkVersion >= 14，该方法可以不调用
            RePlugin.App.onConfigurationChanged(newConfig);
    }

    public void onLowMemory() {
            // 如果App的minSdkVersion >= 14，该方法可以不调用
            RePlugin.App.onLowMemory();
    }

    public void onTrimMemory(int level) {
            // 如果App的minSdkVersion >= 14，该方法可以不调用
            RePlugin.App.onTrimMemory(level);
    }

    /**
     * 子类可以复写此方法来自定义RePluginConfig。请参见 RePluginConfig 的说明
     *
     * @return 新的RePluginConfig对象
     * @see RePluginConfig
     */
    protected RePluginConfig createConfig(Application base) {
        RePluginConfig c = new RePluginConfig();
        // 允许“插件使用宿主类”。默认为“关闭”
        c.setUseHostClassIfNotFound(true);

        // FIXME RePlugin默认会对安装的外置插件进行签名校验，这里先关掉，避免调试时出现签名错误
        c.setVerifySign(!BuildConfig.DEBUG);

        c.setPrintDetailLog(true);
        c.setMoveFileWhenInstalling(false);
        // 针对“安装失败”等情况来做进一步的事件处理
        c.setEventCallbacks(new RePluginEventCallbacks(base) {
            @Override
            public void onInstallPluginFailed(String path, InstallResult code) {
                // FIXME 当插件安装失败时触发此逻辑。您可以在此处做“打点统计”，也可以针对安装失败情况做“特殊处理”
                // 大部分可以通过RePlugin.install的返回值来判断是否成功
                if (BuildConfig.DEBUG) {
                    U.getToastUtil().showShort("onInstallPluginFailed: Failed! path=" + path + "; r=" + code);
                }
                super.onInstallPluginFailed(path, code);
            }

            @Override
            public void onStartActivityCompleted(String plugin, String activity, boolean result) {
                // FIXME 当打开Activity成功时触发此逻辑，可在这里做一些APM、打点统计等相关工作
                super.onStartActivityCompleted(plugin, activity, result);
            }
        });

        // FIXME 若宿主为Release，则此处应加上您认为"合法"的插件的签名，例如，可以写上"宿主"自己的。
        // RePlugin.addCertSignature("AAAAAAAAA");

        // 在Art上，优化第一次loadDex的速度
        // c.setOptimizeArtLoadDex(true);

        c.setCallbacks(new RePluginCallbacks(base) {
            @Override
            public boolean onPluginNotExistsForActivity(Context context, String plugin, Intent intent, int process) {
                // FIXME 当插件"没有安装"时触发此逻辑，可打开您的"下载对话框"并开始下载。
                // FIXME 其中"intent"需传递到"对话框"内，这样可在下载完成后，打开这个插件的Activity
                if (BuildConfig.DEBUG) {
                    U.getToastUtil().showShort("onPluginNotExistsForActivity: Start download... p=" + plugin + "; i=" + intent);
                }
                return super.onPluginNotExistsForActivity(context, plugin, intent, process);
            }
        });

        return c;
    }

}

