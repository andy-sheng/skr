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
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.delegate.AppLifecycles;
import com.common.blockmoniter.BlockDetectByPrinter;
import com.common.bugly.BuglyInit;
import com.common.image.fresco.FrescoInitManager;
import com.common.jiguang.JiGuangPush;
import com.common.log.MyLog;
import com.common.matrix.MatrixInit;
import com.common.statistics.StatisticsAdapter;
import com.common.umeng.UmengInit;
import com.common.utils.CommonReceiver;
import com.common.utils.U;
import com.kingja.loadsir.LoadSirUtil;
import com.kingja.loadsir.callback.SuccessCallback;
import com.kingja.loadsir.core.LoadSir;
import com.squareup.leakcanary.LeakCanary;

import java.util.List;

import cn.jpush.android.api.JPushInterface;


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
    public final static String TAG = "CommonConfiguration";

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
                if (BuildConfig.DEBUG) {
                    ARouter.openLog();
                    ARouter.openDebug();
                }
                ARouter.init(application); // 尽可能早,推荐在Application中初始化
                MyLog.init();
                FrescoInitManager.initFresco(U.app());
                //PgyCrashManager.register();
                CommonReceiver.register();
                UmengInit.init();

                /**
                 * 延迟初始化极光，极光初始化会耗时 400ms
                 */
                JiGuangPush.init();

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
            }

            @Override
            public void onOtherProcessCreate(@NonNull Application application) {
                Log.d(TAG, "onOtherProcessCreate processName:" + U.getProcessName());
                /**
                 * com.zq.live:channel 是友盟的push的通道，只针对这个进程再init一次就好了
                 */
                if (U.getProcessName().endsWith(":channel")) {
                    UmengInit.init();
                }else if(U.getProcessName().endsWith(":pushcore")){
                    JiGuangPush.init();
                }
                BuglyInit.init(false);
            }

            @Override
            public void onTerminate(@NonNull Application application) {
//                PgyCrashManager.unregister();
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
