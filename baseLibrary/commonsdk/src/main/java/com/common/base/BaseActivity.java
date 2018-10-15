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

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;

import com.common.base.delegate.IActivity;
import com.common.integration.cache.Cache;
import com.common.integration.cache.IntelligentCache;
import com.common.integration.lifecycle.ActivityLifecycleForRxLifecycle;
import com.common.integration.lifecycle.ActivityLifecycleable;
import com.common.log.MyLog;
import com.common.mvp.Presenter;
import com.common.utils.AndroidBug5497WorkaroundSupportingTranslucentStatus;
import com.common.utils.StatusBarUtil;
import com.common.utils.U;
import com.jude.swipbackhelper.SwipeBackHelper;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.ActivityEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.HashSet;

import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

/**
 * ================================================
 * 因为 Java 只能单继承,所以如果要用到需要继承特定 {@link Activity} 的三方库,那你就需要自己自定义 {@link Activity}
 * 继承于这个特定的 {@link Activity},然后再按照 {@link BaseActivity} 的格式,将代码复制过去,记住一定要实现{@link IActivity}
 * <p>
 * Created by JessYan on 22/03/2016
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public abstract class BaseActivity extends AppCompatActivity implements IActivity, ActivityLifecycleable {
    protected final String TAG = this.getClass().getSimpleName();
    private final BehaviorSubject<ActivityEvent> mLifecycleSubject = BehaviorSubject.create();
    private Cache<String, Object> mCache;

    // 想加入activity生命周期管理的presenter放在这里
    private HashSet<Presenter> mPresenterSet = new HashSet<>();

    protected boolean mIsDestroyed = false;

    /*用来控制是否采用沉浸式*/
    protected boolean mIsProfileMode = true;

    AndroidBug5497WorkaroundSupportingTranslucentStatus mAndroidBug5497WorkaroundSupportingTranslucentStatus;


    public void addPresent(Presenter presenter) {
        if (presenter != null) {
            mPresenterSet.add(presenter);
            presenter.addToLifeCycle();
        }
    }

    public void removePresent(Presenter presenter) {
        if (presenter != null) {
            mPresenterSet.remove(presenter);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        MyLog.d(TAG, "before getBaseContext " + getBaseContext() + " " + super.getClass());
        super.attachBaseContext(newBase);
        MyLog.d(TAG, "after getBaseContext " + getBaseContext() + " " + super.getClass());
    }

    @NonNull
    @Override
    public synchronized Cache<String, Object> provideCache() {
        if (mCache == null) {
            mCache = new IntelligentCache<Object>(50);
        }
        return mCache;
    }

    @NonNull
    @Override
    public final Subject<ActivityEvent> provideLifecycleSubject() {
        return mLifecycleSubject;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (canSlide()) {
            SwipeBackHelper.onCreate(this);
        }

        if (useEventBus()) {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        }

//        try {
        int layoutResID = initView(savedInstanceState);
        //如果initView返回0,框架则不会调用setContentView(),当然也不会 Bind ButterKnife
        if (layoutResID != 0) {
            setContentView(layoutResID);
        }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        initData(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        if (isProfileMode()) {
            setProfileMode();
        }
        mAndroidBug5497WorkaroundSupportingTranslucentStatus = AndroidBug5497WorkaroundSupportingTranslucentStatus.assistActivity(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (canSlide()) {
            SwipeBackHelper.onPostCreate(this);
        }
    }

    /**
     * 可以设置沉浸式的条件，手动配置，同时满足版本要求
     */
    public boolean isProfileMode() {
        boolean r = mIsProfileMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        MyLog.d(TAG, "isProfileMode r=" + r);
        return r;
    }

    /**
     * 设置名片沉浸式样式
     */
    protected void setProfileMode() {
        MyLog.d(TAG, "setProfileMode");

        /**
         * 跟白色状态有关么
         */
        U.getStatusBarUtil().setTransparentBar(this, Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
    }


    @Override
    protected void onStart() {
        super.onStart();
        for (Presenter presenter : mPresenterSet) {
            presenter.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (Presenter presenter : mPresenterSet) {
            presenter.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (Presenter presenter : mPresenterSet) {
            presenter.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (Presenter presenter : mPresenterSet) {
            presenter.stop();
        }
        if (isFinishing() && !mIsDestroyed) {
            destroy();
            mIsDestroyed = true;
        }
    }

    @Override
    protected final void onDestroy() {
        super.onDestroy();
        if (!mIsDestroyed) {
            destroy();
            mIsDestroyed = true;
        }
    }

    protected void destroy() {
        if (canSlide()) {
            SwipeBackHelper.onDestroy(this);
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        for (Presenter presenter : mPresenterSet) {
            presenter.destroy();
        }
        mPresenterSet.clear();

        if (mAndroidBug5497WorkaroundSupportingTranslucentStatus != null) {
            mAndroidBug5497WorkaroundSupportingTranslucentStatus.destroy();
        }
        if (mCache != null) {
            mCache.clear();
        }
    }

    /**
     * 是否使用eventBus,默认为使用(true)，
     *
     * @return
     */
    @Override
    public boolean useEventBus() {
        return true;
    }

    public boolean canSlide() {
        return true;
    }

    public boolean isKeyboardResize() {
        return true;
    }

    /**
     * 这个Activity是否会使用Fragment,框架会根据这个属性判断是否注册{@link android.support.v4.app.FragmentManager.FragmentLifecycleCallbacks}
     * 如果返回false,那意味着这个Activity不需要绑定Fragment,那你再在这个Activity中绑定继承于 {@link com.common.base.BaseFragment} 的Fragment将不起任何作用
     *
     * @return
     */
    @Override
    public boolean useFragment() {
        return true;
    }

    /**
     * 事件在 {@link ActivityLifecycleForRxLifecycle}发出
     * 绑定 Activity 的指定生命周期
     *
     * @param event
     * @param <T>
     * @return
     */
    public <T> LifecycleTransformer<T> bindUntilEvent(final ActivityEvent event) {
        return RxLifecycle.bindUntilEvent(provideLifecycleSubject(), event);
    }
}
