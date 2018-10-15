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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.base.delegate.IFragment;
import com.common.integration.cache.Cache;
import com.common.integration.cache.IntelligentCache;
import com.common.integration.lifecycle.ActivityLifecycleForRxLifecycle;
import com.common.integration.lifecycle.FragmentLifecycleable;
import com.common.log.MyLog;
import com.common.mvp.Presenter;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.FragmentEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.HashSet;

import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

/**
 * ================================================
 * 因为 Java 只能单继承,所以如果要用到需要继承特定 @{@link Fragment} 的三方库,那你就需要自己自定义 @{@link Fragment}
 * 继承于这个特定的 @{@link Fragment},然后再按照 {@link BaseFragment} 的格式,将代码复制过去,记住一定要实现{@link IFragment}
 * <p>
 * Created by JessYan on 22/03/2016
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public abstract class BaseFragment extends Fragment implements IFragment, FragmentLifecycleable {
    protected final String TAG = this.getClass().getSimpleName();
    private final BehaviorSubject<FragmentEvent> mLifecycleSubject = BehaviorSubject.create();
    private Cache<String, Object> mCache;

    private HashSet<Presenter> mPresenterSet = new HashSet<>();

    boolean isDestroyed = false;

    protected boolean fragmentOnCreated = false;
    protected boolean fragmentVisible = false;

    View mRootView;


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
    public final Subject<FragmentEvent> provideLifecycleSubject() {
        return mLifecycleSubject;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = initView(inflater, container, savedInstanceState);
        initData(savedInstanceState);
        return mRootView;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        MyLog.d(TAG, "onCreate" + " savedInstanceState=" + savedInstanceState);
        super.onCreate(savedInstanceState);
        if (useEventBus()) {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        }
    }

    @Override
    public void onStart() {
        MyLog.d(TAG, "onStart");
        super.onStart();
        for (Presenter presenter : mPresenterSet) {
            presenter.start();
        }
    }

    @Override
    public void onResume() {
        MyLog.d(TAG, "onResume");
        super.onResume();
        for (Presenter presenter : mPresenterSet) {
            presenter.resume();
        }
        if (fragmentVisible) {
            onFragmentVisible();
        }
    }

    @Override
    public void onPause() {
        MyLog.d(TAG, "onPause");
        super.onPause();
        for (Presenter presenter : mPresenterSet) {
            presenter.pause();
        }
        if (fragmentVisible) {
            onFragmentInvisible();
        }
        if (getActivity().isFinishing() && !isDestroyed) {
            destroy();
            isDestroyed = true;
        }
    }

    @Override
    public void onStop() {
        MyLog.d(TAG, "onStop");
        super.onStop();
        if (!isDestroyed) {
            for (Presenter presenter : mPresenterSet) {
                presenter.stop();
            }
        }
    }

    @Override
    public void onDestroyView() {
        MyLog.w(TAG, "onDestroyView");
        super.onDestroyView();
    }

    public void destroy() {
        MyLog.w(TAG, "destroy");
        for (Presenter presenter : mPresenterSet) {
            presenter.destroy();
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    /**
     * 不要继承onDestroy 这个执行慢
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!isDestroyed) {
            destroy();
            isDestroyed = true;
        }
    }

    /**
     * 当Fragment可见的时候的回调
     */
    protected void onFragmentVisible() {
        MyLog.d(TAG, "onFragmentVisible");
    }

    /**
     * 当Fragment不可见的时候的回调
     */
    protected void onFragmentInvisible() {
        MyLog.d(TAG, "onFragmentInvisible");
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

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isResumed()) {   // only at fragment screen is resumed
            fragmentVisible = true;
            fragmentOnCreated = true;
            onFragmentVisible();
        } else if (visible) {        // only at fragment onCreated
            fragmentVisible = true;
            fragmentOnCreated = true;
        } else if (!visible && fragmentOnCreated) {// only when you go out of fragment screen
            fragmentVisible = false;
            onFragmentInvisible();
        }
    }

    /**
     * 事件在 {@link ActivityLifecycleForRxLifecycle}发出
     * 绑定 Activity 的指定生命周期
     *
     * @param event
     * @param <T>
     * @return
     */
    public <T> LifecycleTransformer<T> bindUntilEvent(final FragmentEvent event) {
        return RxLifecycle.bindUntilEvent(provideLifecycleSubject(), event);
    }
}
