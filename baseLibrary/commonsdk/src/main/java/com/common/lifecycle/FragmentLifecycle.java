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
package com.common.lifecycle;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.common.log.MyLog;

/**
 * ================================================
 * {@link FragmentManager.FragmentLifecycleCallbacks} 默认实现类
 * <p>
 * Created by JessYan on 04/09/2017 16:04
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class FragmentLifecycle extends FragmentManager.FragmentLifecycleCallbacks {

    public final static String TAG = "FragmentLifecycle";
    
    public FragmentLifecycle() {
    }

    @Override
    public void onFragmentAttached(FragmentManager fm, Fragment f, Context context) {
//        MyLog.w(TAG," - onFragmentAttached");
    }

    @Override
    public void onFragmentCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
//        MyLog.w(TAG," - onFragmentCreated");
    }

    @Override
    public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState) {
//        MyLog.w(TAG," - onFragmentViewCreated");
    }

    @Override
    public void onFragmentActivityCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
//        MyLog.w(TAG," - onFragmentActivityCreated");
    }

    @Override
    public void onFragmentStarted(FragmentManager fm, Fragment f) {
//        MyLog.w(TAG," - onFragmentStarted");
    }

    @Override
    public void onFragmentResumed(FragmentManager fm, Fragment f) {
//        MyLog.w(TAG," - onFragmentResumed");
    }

    @Override
    public void onFragmentPaused(FragmentManager fm, Fragment f) {
//        MyLog.w(TAG," - onFragmentPaused");
    }

    @Override
    public void onFragmentStopped(FragmentManager fm, Fragment f) {
//        MyLog.w(TAG," - onFragmentStopped");
    }

    @Override
    public void onFragmentSaveInstanceState(FragmentManager fm, Fragment f, Bundle outState) {
//        MyLog.w(TAG," - onFragmentSaveInstanceState");
    }

    @Override
    public void onFragmentViewDestroyed(FragmentManager fm, Fragment f) {
//        MyLog.w(TAG," - onFragmentViewDestroyed");
    }

    @Override
    public void onFragmentDestroyed(FragmentManager fm, Fragment f) {
//        MyLog.w(TAG," - onFragmentDestroyed");
    }

    @Override
    public void onFragmentDetached(FragmentManager fm, Fragment f) {
//        MyLog.w(TAG," - onFragmentDetached");
    }

}
