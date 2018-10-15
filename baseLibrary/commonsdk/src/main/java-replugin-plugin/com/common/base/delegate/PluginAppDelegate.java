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
import android.content.res.Configuration;
import android.support.annotation.NonNull;

/**
 * 负责 插件化框架的初始化工作
 */
public class PluginAppDelegate {

    public final static String TAG = "PluginAppDelegate";

    public void attachBaseContext(@NonNull Application base) {
    }

    public void onCreate(@NonNull Application application) {
    }

    public void onTerminate(@NonNull Application application) {
    }

    public void onConfigurationChanged(Configuration newConfig) {
    }

    public void onLowMemory() {
    }

    public void onTrimMemory(int level) {
    }


}

