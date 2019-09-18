package com.module.posts

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

import android.app.Application
import android.content.Context
import android.support.v4.app.FragmentManager
import android.util.Log
import com.common.base.ConfigModule
import com.common.base.GlobalParams
import com.common.base.delegate.AppLifecycles


/**
 * ================================================
 * CommonSDK 的 GlobalConfiguration 含有有每个组件都可公用的配置信息, 每个组件的 AndroidManifest 都应该声明此 ConfigModule
 *
 * @see [ConfigModule wiki 官方文档](https://github.com/JessYanCoding/ArmsComponent/wiki.3.3)
 * Created by JessYan on 30/03/2018 17:16
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
class PostsConfiguration : ConfigModule {

    val TAG = "FeedsConfiguration"
    override fun applyOptions(builder: GlobalParams.Builder) {

    }

    override fun injectAppLifecycle(lifecycles: MutableList<AppLifecycles>) {
        // AppDelegate.Lifecycle 的所有方法都会在基类Application对应的生命周期中被调用,所以在对应的方法中可以扩展一些自己需要的逻辑
        lifecycles.add(object : AppLifecycles {

            override fun attachBaseContext(base: Context) {}

            override fun onMainProcessCreate(application: Application) {
                Log.d(TAG, "application onCreate")
            }

            override fun onOtherProcessCreate(application: Application) {

            }

            override fun onTerminate(application: Application) {

            }
        })
    }

    override fun injectActivityLifecycle(lifecycles: List<Application.ActivityLifecycleCallbacks>) {}

    override fun injectFragmentLifecycle(lifecycles: List<FragmentManager.FragmentLifecycleCallbacks>) {}

}
