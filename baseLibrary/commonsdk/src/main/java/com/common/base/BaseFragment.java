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

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.base.delegate.IFragment;
import com.common.cache.Cache;
import com.common.cache.IntelligentCache;
import com.common.image.model.BaseImage;
import com.common.lifecycle.ActivityLifecycleForRxLifecycle;
import com.common.lifecycle.FragmentLifecycleable;
import com.common.log.MyLog;
import com.common.mvp.Presenter;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.U;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.FragmentEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.HashMap;
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

    private HashMap<Integer, Object> mNeedSaveWhenLowMemory = new HashMap<>();

    private HashSet<Presenter> mPresenterSet = new HashSet<>();

    boolean isDestroyed = false;

    protected boolean fragmentOnCreated = false;
    protected boolean fragmentVisible = true;

    protected View mRootView;

    protected int mRequestCode = 0;

    /**
     * Fragment A 启动 Fragment B 处理业务后想拿到结果
     * 会通过 B 的 mFragmentDataListener 返回结果
     */
    protected FragmentDataListener mFragmentDataListener;

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
    public final Subject<FragmentEvent> provideLifecycleSubject() {
        return mLifecycleSubject;
    }


    @NonNull
    @Override
    public synchronized Cache<String, Object> provideCache() {
        if (mCache == null) {
            mCache = new IntelligentCache<Object>(50);
        }
        return mCache;
    }

    /**
     * 可以在此恢复数据
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        MyLog.d(TAG, "onActivityCreated" + " savedInstanceState=" + savedInstanceState);
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            boolean firstStart = savedInstanceState.getBoolean("firstStart", true);
            if (!firstStart) {
                // 需要恢复状态
                onRestoreInstanceState("onActivityCreated", savedInstanceState);
            }
        }
    }

    /**
     * 子类可以覆盖这个方法恢复一些值
     *
     * @param savedInstanceState
     */
    public void onRestoreInstanceState(String from, @NonNull Bundle savedInstanceState) {
        MyLog.d(TAG, "onRestoreInstanceState" + " from=" + from + " savedInstanceState=" + savedInstanceState);
        if (savedInstanceState != null && mNeedSaveWhenLowMemory.isEmpty()) {
            for (String key : savedInstanceState.keySet()) {
                if (key.startsWith("type_")) {
                    Object v = savedInstanceState.get(key);
                    String typeStr = key.substring("type_".length());
                    int type = Integer.parseInt(typeStr);
                    setData(type, v);
                }
            }
        }
    }

    /**
     * 当系统认为你的fragment存在被销毁的可能时，onSaveInstanceState 就会被调用
     * 不包括用户主动退出fragment导致其被销毁，比如按BACK键后fragment被主动销毁
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        MyLog.d(TAG, "onSaveInstanceState" + " outState=" + outState);
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putBoolean("firstStart", false);
            for (int type : mNeedSaveWhenLowMemory.keySet()) {
                Object object = mNeedSaveWhenLowMemory.get(type);
                if (object instanceof Serializable) {
                    outState.putSerializable("type_" + type, (Serializable) object);
                } else if (object instanceof Parcelable) {
                    outState.putParcelable("type_" + type, (Parcelable) object);
                } else if (object instanceof String) {
                    outState.putString("type_" + type, (String) object);
                } else if (object instanceof Integer) {
                    outState.putInt("type_" + type, (Integer) object);
                } else if (object instanceof Long) {
                    outState.putLong("type_" + type, (Long) object);
                } else if (object instanceof Double) {
                    outState.putDouble("type_" + type, (Double) object);
                } else if (object instanceof Float) {
                    outState.putDouble("type_" + type, (Float) object);
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MyLog.w(TAG, "onCreateView");
        int layoutId = initView();
        if (layoutId != 0) {
            mRootView = inflater.inflate(layoutId, container, false);
        }
        initData(savedInstanceState);
        if (mRootView != null) {
            /**
             * 吃掉点击事件，防止穿透
             */
            mRootView.setClickable(true);
        }
        View loadSirInjectView = loadSirReplaceRootView();
        if (loadSirInjectView == null) {
            return mRootView;
        } else {
            return loadSirInjectView;
        }
    }

    /**
     * 只有LoadSir想要register mRootView 时才需要覆盖这个方法
     * <p>
     * 其他的不需要
     *
     * @return
     */
    protected View loadSirReplaceRootView() {
        return null;
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
        if (savedInstanceState != null) {
            boolean firstStart = savedInstanceState.getBoolean("firstStart", true);
            if (!firstStart) {
                // 需要恢复状态
                onRestoreInstanceState("onCreate", savedInstanceState);
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
        MyLog.d(TAG, "onResume fragmentVisible=" + fragmentVisible
                + " isHidden()" + isHidden()
                + " getUserVisibleHint=" + getUserVisibleHint()
                + " isVisible=" + isVisible()
        );
        super.onResume();
        for (Presenter presenter : mPresenterSet) {
            presenter.resume();
        }

        /**
         * 区分几种情况
         * A B C Fragment 在 viewpager 里
         * 返回时 getTopFragment 为C 其实是A可见，这就要将
         * BaseFragment baseFragment = U.getFragmentUtils().getTopFragment(getActivity()); 去掉
         *
         * 如果去掉
         * 还有一种是 A B C 正常的叠在 Activity 中，返回时
         * A B C onResume 都会触发
         * 所以需要标示出 这个是否在 viewpager里
         * {@link isInViewPager()}
         */
        if (fragmentVisible) {
            if (isInViewPager()) {
                onFragmentVisible();
            } else {
                BaseFragment baseFragment = U.getFragmentUtils().getTopFragment(getActivity());
                if (baseFragment == this) {
                    onFragmentVisible();
                } else {
                    MyLog.d(TAG, "onResume 不在顶部");
                }
            }
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
        fragmentOnCreated = false;
    }

    @CallSuper
    public void destroy() {
        MyLog.w(TAG, "destroy");
        for (Presenter presenter : mPresenterSet) {
            presenter.destroy();
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    //通知你，你要准备显示了
    public void notifyToShow() {
        U.getFragmentUtils().showFragment(this);
    }

    //通知你，你要准备隐藏了
    public void notifyToHide() {
        U.getFragmentUtils().hideFragment(this);
    }

    /**
     * 不要继承onDestroy 这个执行慢
     */
    @Override
    public final void onDestroy() {
        MyLog.d(TAG, "onDestroy");
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
        StatisticsAdapter.recordPageStart(getActivity(), this.getClass().getSimpleName());
    }

    /**
     * 当Fragment不可见的时候的回调
     */
    protected void onFragmentInvisible() {
        MyLog.d(TAG, "onFragmentInvisible");
        StatisticsAdapter.recordPageEnd(getActivity(), this.getClass().getSimpleName());
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
        MyLog.d(TAG, "setUserVisibleHint" + " visible=" + visible);
        super.setUserVisibleHint(visible);
        fragmentVisible = visible;
        if (visible && isResumed()) {   // only at fragment screen is resumed
            fragmentOnCreated = true;
            onFragmentVisible();
        } else if (visible) {        // only at fragment onCreated
            fragmentOnCreated = true;
        } else if (!visible && fragmentOnCreated) {// only when you go out of fragment screen
            onFragmentInvisible();
        }
    }

    @Override
    public int getRequestCode() {
        if (mRequestCode <= 0) {
            mRequestCode = U.getRequestCode();
        }
        return mRequestCode;
    }

    /**
     * 是否要消费掉返回键
     * 返回true 代表要消费掉
     */
    protected boolean onBackPressed() {
        return false;
    }


    /**
     * 由 Fragment 启动 Activity 并想拿到结果时，会通过这个回调
     * 如果是 Fragment A 启动 Fragment B 并想拿到结果时，参考{@link #mFragmentDataListener}
     * 在Fragment中使用startActivityForResult之后，
     * onActivityResult的调用是从activity中开始的（即会先调用activity中的onActivityResult）
     * 如果使用getActivity().startActivityForResult是不会响应 fragment 的 onActivityResult的
     * 而是应该直接使startActivityForResult() 才会被执行
     * 为了防止这种情况，不允许继承 onActivityResult。
     * 统一使用使用 onActivityResultReal BaseActivity会对其做特殊处理。
     */
    public final void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 是否要消费掉onActivityResult
     * 返回true 代表要消费掉
     */
    public boolean onActivityResultReal(int requestCode, int resultCode, Intent data) {
        return false;
    }

    /**
     * 在运行时 想与此Fragment通信，偷懒的话可以用这个方法
     */
    @Override
    public void setData(int type, @Nullable Object data) {
        MyLog.d(TAG, "setData" + " type=" + type + " data=" + data);
        mNeedSaveWhenLowMemory.put(type, data);
    }

    /**
     * Fragment A 启动 Fragment B 处理业务后想拿到结果
     * 会通过 B 的 mFragmentDataListener 返回结果
     */
    public void setFragmentDataListener(FragmentDataListener fragmentDataListener) {
        mFragmentDataListener = fragmentDataListener;
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

    public void finish() {
        U.getFragmentUtils().popFragment(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
    }

    public boolean isInViewPager() {
        return false;
    }
}
