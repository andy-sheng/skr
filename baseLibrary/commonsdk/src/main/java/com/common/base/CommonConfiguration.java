/*
 * Copyright 2018 JessYan
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

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.delegate.AppLifecycles;
import com.common.bugly.BuglyInit;
import com.common.huawei.LoadedApkHuaWei;
import com.common.image.fresco.FrescoInitManager;
import com.common.jiguang.JiGuangPush;
import com.common.log.MyLog;
import com.common.matrix.MatrixInit;
import com.common.umeng.UmengInit;
import com.common.utils.CommonReceiver;
import com.common.utils.U;
import com.glidebitmappool.BitmapPoolAdapter;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;

import java.util.List;


/**
 * ================================================
 * CommonSDK 的 GlobalConfiguration 含有有每个组件都可公用的配置信息, 每个组件的 AndroidManifest 都应该声明此 ConfigModule
 *
 * @see <a href="https://github.com/JessYanCoding/ArmsComponent/wiki#3.3">ConfigModule wiki 官方文档</a>
 * Created by JessYan on 30/03/2018 17:16
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class CommonConfiguration implements ConfigModule {
    public final String TAG = "CommonConfiguration";

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
                MyLog.e(TAG, "onMainProcessCreate begin");
                MyLog.w(TAG, "MyLog begin");
                MyLog.init();
                // 无法异步延迟，因为 module 接口 还需要ARouter
                MyLog.w(TAG, "ARouter begin");
                if (BuildConfig.DEBUG) {
                    ARouter.openLog();
                    ARouter.openDebug();
                }
                ARouter.init(application); // 尽可能早,推荐在Application中初始化

                MyLog.w(TAG, "FrescoInitManager begin");
                FrescoInitManager.initFresco(U.app());
                //PgyCrashManager.register();
                CommonReceiver.register();
                MyLog.w(TAG, "Umeng begin");
                UmengInit.init();
                MyLog.w(TAG, "Jiguang begin");

                InitManager.initMainThread(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * 延迟初始化极光，极光初始化会耗时 400ms
                         */
                        JiGuangPush.init(true);
                    }
                }, 5000);

                MyLog.w(TAG, "Bugly begin");
                BuglyInit.init(true);
                /**
                 * 初始化Matrix，分debug和release
                 */
                MatrixInit.init();
                //leakCanary 引用
                LeakCanary.install(application);
                // 卡顿检测 ,使用matrix，能检测出帧问题
                // BlockDetectByPrinter.start();
                // 所有的都会过这个
                //Debug.startMethodTracing();

                InitManager.initMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (U.getDeviceUtils().isEmui()) {
                            MyLog.e(TAG, "是emui系统，hook广播白名单");
                            LoadedApkHuaWei.hookHuaWeiVerifier(U.app(), new LoadedApkHuaWei.TooManyBroadcastCallback() {
                                @Override
                                public void tooManyBroadcast(int registedCount, int totalCount) {
                                    MyLog.e(TAG, "注册了太多广播 tooManyBroadcast" + " registedCount=" + registedCount + " totalCount=" + totalCount);
                                }
                            });
                        } else {
                            MyLog.e(TAG, "不是emui系统,cancel");
                        }
                    }
                }, 20 * 1000);

                QbSdk.setTbsListener(new TbsListener() {
                    @Override
                    public void onDownloadFinish(int i) {
                        MyLog.w(TAG, "x5 onDownloadFinish" + " i=" + i);
                    }

                    @Override
                    public void onInstallFinish(int i) {
                        MyLog.w(TAG, "x5 onInstallFinish" + " i=" + i);
                    }

                    @Override
                    public void onDownloadProgress(int i) {
                        MyLog.w(TAG, "x5 onDownloadProgress" + " i=" + i);
                    }
                });
                //x5内核初始化接口
                QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

                    @Override
                    public void onViewInitFinished(boolean arg0) {
                        //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                        MyLog.w(TAG, " x5 onViewInitFinished is " + arg0);
                    }

                    @Override
                    public void onCoreInitFinished() {
                        MyLog.w(TAG, "x5 onCoreInitFinished");
                    }
                };
                QbSdk.initX5Environment(application, cb);

                // 这里耗费 900ms
                MyLog.e(TAG, "onMainProcessCreate over version="+U.getAppInfoUtils().getVersionName());
            }

            @Override
            public void onOtherProcessCreate(@NonNull Application application) {
                Log.d(TAG, "onOtherProcessCreate processName:" + U.getProcessName());
                /**
                 * com.zq.live:channel 是友盟的push的通道，只针对这个进程再init一次就好了
                 */
                if (U.getProcessName().endsWith(":channel")) {
                    UmengInit.init();
                } else if (U.getProcessName().endsWith(":pushcore")) {
                    JiGuangPush.init(false);
                }
                BuglyInit.init(false);
            }

            @Override
            public void onTerminate(@NonNull Application application) {
//                PgyCrashManager.unregister();
                BitmapPoolAdapter.clearMemory();
                //GlideBitmapPool.trimMemory(level);
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
