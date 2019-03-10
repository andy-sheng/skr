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

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.common.base.BaseFragment;
import com.common.base.ConfigModule;
import com.common.base.delegate.IActivity;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.ActivityUtils;
import com.common.utils.U;
import com.umeng.message.PushAgent;

import java.util.ArrayList;
import java.util.List;

import anet.channel.util.Utils;


/**
 * ================================================
 * {@link Application.ActivityLifecycleCallbacks} 默认实现类
 *
 * @see <a href="http://www.jianshu.com/p/75a5c24174b2">ActivityLifecycleCallbacks 分析文章</a>
 * Created by JessYan on 21/02/2017 14:23
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {
    public final static String TAG = "ActivityLifecycle";

    FragmentLifecycle mFragmentLifecycle;

    // 存一些别的module fragment callback
    List<FragmentManager.FragmentLifecycleCallbacks> mFragmentLifecycles;

    FragmentLifecycle getFragmentLifecycle() {
        if (mFragmentLifecycle == null) {
            mFragmentLifecycle = new FragmentLifecycle();
        }
        return mFragmentLifecycle;
    }

    List<FragmentManager.FragmentLifecycleCallbacks> getExtraFragmentLifecycles() {
        if (mFragmentLifecycles == null) {
            mFragmentLifecycles = new ArrayList<>();
        }
        return mFragmentLifecycles;
    }


    int mActivityCount = 0;

    Handler mUiHanlder = new Handler();

    public ActivityLifecycle() {

    }

    /**
     * Activity super.onCreate 执行完再执行这个
     *
     * @param activity
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        //如果 intent 包含了此字段,并且为 true 说明不加入到 list 进行统一管理
        boolean isNotAdd = false;
        if (activity.getIntent() != null)
            isNotAdd = activity.getIntent().getBooleanExtra(ActivityUtils.IS_NOT_ADD_ACTIVITY_LIST, false);

        if (!isNotAdd)
            U.getActivityUtils().addActivity(activity);

        registerFragmentCallbacks(activity);
        PushAgent.getInstance(U.app()).onAppStart();
    }

    @Override
    public void onActivityStarted(Activity activity) {
        mActivityCount++;
        if (mActivityCount == 1) {
            U.getActivityUtils().setAppForeground(true);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        U.getActivityUtils().setCurrentActivity(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        mActivityCount--;
        if (mActivityCount == 0) {
            U.getActivityUtils().setAppForeground(false);
        }
        if (U.getActivityUtils().getCurrentActivity() == activity) {
            U.getActivityUtils().setCurrentActivity(null);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        U.getActivityUtils().removeActivity(activity);
    }

    /**
     * 给每个 Activity 的所有 Fragment 设置监听其生命周期, Activity 可以通过 {@link IActivity#useFragment()}
     * 设置是否使用监听,如果这个 Activity 返回 false 的话,这个 Activity 下面的所有 Fragment 将不能使用
     * 意味着 {@link BaseFragment} 也不能使用
     *
     * @param activity
     */
    private void registerFragmentCallbacks(Activity activity) {

        boolean useFragment = activity instanceof IActivity ? ((IActivity) activity).useFragment() : true;
        if (activity instanceof FragmentActivity && useFragment) {

            //mFragmentLifecycle 为 Fragment 生命周期实现类, 用于框架内部对每个 Fragment 的必要操作, 如给每个 Fragment 配置 FragmentDelegate
            //注册框架内部已实现的 Fragment 生命周期逻辑
            ((FragmentActivity) activity).getSupportFragmentManager().registerFragmentLifecycleCallbacks(getFragmentLifecycle(), true);

            if (U.getCacheUtils().containsKeyInKeep(ConfigModule.class.getName())) {
                List<ConfigModule> modules = (List<ConfigModule>) U.getCacheUtils().removeFromKeep(ConfigModule.class.getName());
                for (ConfigModule module : modules) {
                    module.injectFragmentLifecycle(getExtraFragmentLifecycles());
                }
            }

            //注册框架外部, 开发者扩展的 Fragment 生命周期逻辑
            for (FragmentManager.FragmentLifecycleCallbacks fragmentLifecycle : getExtraFragmentLifecycles()) {
                ((FragmentActivity) activity).getSupportFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycle, true);
            }
        }
    }

}
