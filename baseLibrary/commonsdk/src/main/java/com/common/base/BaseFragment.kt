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
package com.common.base

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.common.base.delegate.IFragment
import com.common.lifecycle.ActivityLifecycleForRxLifecycle
import com.common.lifecycle.FragmentLifecycleable
import com.common.log.MyLog
import com.common.mvp.Presenter
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.trello.rxlifecycle2.LifecycleTransformer
import com.trello.rxlifecycle2.RxLifecycle
import com.trello.rxlifecycle2.android.FragmentEvent

import org.greenrobot.eventbus.EventBus

import java.io.Serializable
import java.util.HashMap
import java.util.HashSet

import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

/**
 * ================================================
 * 因为 Java 只能单继承,所以如果要用到需要继承特定 @[Fragment] 的三方库,那你就需要自己自定义 @[Fragment]
 * 继承于这个特定的 @[Fragment],然后再按照 [BaseFragment] 的格式,将代码复制过去,记住一定要实现[IFragment]
 *
 *
 * Created by JessYan on 22/03/2016
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
abstract class BaseFragment : Fragment(), IFragment, FragmentLifecycleable, CoroutineScope by MainScope() {
    protected val TAG = this.javaClass.simpleName

    private val mLifecycleSubject = BehaviorSubject.create<FragmentEvent>()

    private val mNeedSaveWhenLowMemory = HashMap<Int, Any?>()

    private val mPresenterSet = HashSet<Presenter>()

    internal var isDestroyed = false

    protected var fragmentOnCreated = false

    var fragmentVisible = true
        private set

    var rootView: View = View(U.app())
        private set

    protected var mRequestCode = 0

    /**
     * Fragment A 启动 Fragment B 处理业务后想拿到结果
     * 会通过 B 的 mFragmentDataListener 返回结果
     */
    var fragmentDataListener: FragmentDataListener? = null

    open fun isInViewPager() = false

    open fun isBlackStatusBarText() = false

    fun addPresent(presenter: Presenter?) {
        if (presenter != null) {
            mPresenterSet.add(presenter)
            presenter.addToLifeCycle()
        }
    }

    fun removePresent(presenter: Presenter?) {
        if (presenter != null) {
            mPresenterSet.remove(presenter)
        }
    }

    override fun provideLifecycleSubject(): Subject<FragmentEvent> {
        return mLifecycleSubject
    }


    /**
     * 不可以在此恢复数据
     * Fragment 生命周期
     * onAttach()
     * onCreate()
     * onCreateView()
     * onActivityCreated()
     *
     * @param savedInstanceState
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        MyLog.d(TAG, "onActivityCreated savedInstanceState=$savedInstanceState")
        super.onActivityCreated(savedInstanceState)
        savedInstanceState?.let {
            val firstStart = it.getBoolean("firstStart", true)
            if (!firstStart) {
                // 需要恢复状态
                onRestoreInstanceState("onActivityCreated", it)
            }
        }
    }

    /**
     * 子类可以覆盖这个方法恢复一些值
     *
     * @param savedInstanceState
     */
    fun onRestoreInstanceState(from: String, savedInstanceState: Bundle) {
        MyLog.d(TAG, "onRestoreInstanceState from=$from savedInstanceState=$savedInstanceState")
        if (savedInstanceState != null && mNeedSaveWhenLowMemory.isEmpty()) {
            for (key in savedInstanceState.keySet()) {
                if (key.startsWith("type_")) {
                    val v = savedInstanceState.get(key)
                    val typeStr = key.substring("type_".length)
                    val type = Integer.parseInt(typeStr)
                    setData(type, v)
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
    override fun onSaveInstanceState(outState: Bundle) {
        MyLog.d(TAG, "onSaveInstanceState outState=$outState")
        super.onSaveInstanceState(outState)
        outState.putBoolean("firstStart", false)
        for (type in mNeedSaveWhenLowMemory.keys) {
            val `object` = mNeedSaveWhenLowMemory[type]
            if (`object` is Serializable) {
                outState.putSerializable("type_$type", `object`)
            } else if (`object` is Parcelable) {
                outState.putParcelable("type_$type", `object`)
            } else if (`object` is String) {
                outState.putString("type_$type", `object`)
            } else if (`object` is Int) {
                outState.putInt("type_$type", `object`)
            } else if (`object` is Long) {
                outState.putLong("type_$type", `object`)
            } else if (`object` is Double) {
                outState.putDouble("type_$type", `object`)
            } else if (`object` is Float) {
                outState.putDouble("type_$type", `object`.toDouble())
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MyLog.w(TAG, "onCreateView")
        val layoutId = initView()
        if (layoutId != 0) {
            rootView = inflater.inflate(layoutId, container, false)
        }
        initData(savedInstanceState)
        /**
         * 吃掉点击事件，防止穿透
         */
        rootView.isClickable = true
        val loadSirInjectView = loadSirReplaceRootView()
        return loadSirInjectView ?: rootView
    }

    /**
     * 只有LoadSir想要register mRootView 时才需要覆盖这个方法
     *
     *
     * 其他的不需要
     *
     * @return
     */
    protected open fun loadSirReplaceRootView(): View? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        MyLog.d(TAG, "onCreate savedInstanceState=$savedInstanceState")
        super.onCreate(savedInstanceState)
        if (useEventBus()) {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this)
            }
        }
        if (savedInstanceState != null) {
            val firstStart = savedInstanceState.getBoolean("firstStart", true)
            if (!firstStart) {
                // 需要恢复状态
                onRestoreInstanceState("onCreate", savedInstanceState)
            }
        }
    }

    override fun onStart() {
        MyLog.d(TAG, "onStart")
        super.onStart()
        for (presenter in mPresenterSet) {
            presenter.start()
        }
    }

    override fun onResume() {
        MyLog.d(TAG, "onResume fragmentVisible=" + fragmentVisible
                + " isHidden()" + isHidden
                + " getUserVisibleHint=" + userVisibleHint
                + " isVisible=" + isVisible
        )
        super.onResume()
        for (presenter in mPresenterSet) {
            presenter.resume()
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
         * []
         */
        if (fragmentVisible) {
            if (isInViewPager()) {
                onFragmentVisible()
            } else {
                val baseFragment = U.getFragmentUtils().getTopFragment(activity)
                if (baseFragment === this) {
                    onFragmentVisible()
                } else {
                    MyLog.d(TAG, "onResume 不在顶部")
                }
            }
        }
    }

    override fun onPause() {
        MyLog.d(TAG, "onPause")
        super.onPause()
        for (presenter in mPresenterSet) {
            presenter.pause()
        }
        if (fragmentVisible) {
            onFragmentInvisible()
        }
        if (activity!!.isFinishing && !isDestroyed) {
            destroy()
            isDestroyed = true
        }
    }

    override fun onStop() {
        MyLog.d(TAG, "onStop")
        super.onStop()
        if (!isDestroyed) {
            for (presenter in mPresenterSet) {
                presenter.stop()
            }
        }
    }

    override fun onDestroyView() {
        MyLog.w(TAG, "onDestroyView")
        super.onDestroyView()
        fragmentOnCreated = false
    }

    @CallSuper
    open fun destroy() {
        MyLog.w(TAG, "destroy")
        for (presenter in mPresenterSet) {
            presenter.destroy()
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    //通知你，你要准备显示了
    open fun notifyToShow() {
        U.getFragmentUtils().showFragment(this)
    }

    //通知你，你要准备隐藏了
    open fun notifyToHide() {
        U.getFragmentUtils().hideFragment(this)
    }

    /**
     * 不要继承onDestroy 这个执行慢
     */
    override fun onDestroy() {
        MyLog.d(TAG, "onDestroy")
        super.onDestroy()
        if (!isDestroyed) {
            destroy()
            isDestroyed = true
        }
    }

    /**
     * 当Fragment可见的时候的回调
     */
    protected open fun onFragmentVisible() {
        MyLog.d(TAG, "onFragmentVisible")
        if (isBlackStatusBarText()) {
            U.getStatusBarUtil().setTransparentBar(activity, true)
        } else {
            U.getStatusBarUtil().setTransparentBar(activity, false)
        }
        StatisticsAdapter.recordPageStart(activity, this.javaClass.simpleName)
    }

    /**
     * 当Fragment不可见的时候的回调
     */
    protected open fun onFragmentInvisible() {
        MyLog.d(TAG, "onFragmentInvisible")
        StatisticsAdapter.recordPageEnd(activity, this.javaClass.simpleName)
    }

    /**
     * 是否使用eventBus,默认为使用(true)，
     *
     * @return
     */
    override fun useEventBus(): Boolean {
        return true
    }

    override fun setUserVisibleHint(visible: Boolean) {
        MyLog.d(TAG, "setUserVisibleHint visible=$visible")
        super.setUserVisibleHint(visible)
        fragmentVisible = visible
        if (visible && isResumed) {   // only at fragment screen is resumed
            fragmentOnCreated = true
            onFragmentVisible()
        } else if (visible) {        // only at fragment onCreated
            fragmentOnCreated = true
        } else if (!visible && fragmentOnCreated) {// only when you go out of fragment screen
            onFragmentInvisible()
        }
    }

    override fun getRequestCode(): Int {
        if (mRequestCode <= 0) {
            mRequestCode = U.getRequestCode()
        }
        return mRequestCode
    }

    /**
     * 是否要消费掉返回键
     * 返回true 代表要消费掉
     */
    open fun onBackPressed(): Boolean {
        return false
    }


    /**
     * 由 Fragment 启动 Activity 并想拿到结果时，会通过这个回调
     * 如果是 Fragment A 启动 Fragment B 并想拿到结果时，参考[.mFragmentDataListener]
     * 在Fragment中使用startActivityForResult之后，
     * onActivityResult的调用是从activity中开始的（即会先调用activity中的onActivityResult）
     * 如果使用getActivity().startActivityForResult是不会响应 fragment 的 onActivityResult的
     * 而是应该直接使startActivityForResult() 才会被执行
     * 为了防止这种情况，不允许继承 onActivityResult。
     * 统一使用使用 onActivityResultReal BaseActivity会对其做特殊处理。
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResult(requestCode, resultCode, data)
    }

    /**
     * 是否要消费掉onActivityResult
     * 返回true 代表要消费掉
     */
    open fun onActivityResultReal(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return false
    }

    /**
     * 在运行时 想与此Fragment通信，偷懒的话可以用这个方法
     */
    override fun setData(type: Int, data: Any?) {
        MyLog.d(TAG, "setData type=$type data=$data")
        mNeedSaveWhenLowMemory[type] = data
    }

    /**
     * 事件在 [ActivityLifecycleForRxLifecycle]发出
     * 绑定 Activity 的指定生命周期
     *
     * @param event
     * @param <T>
     * @return
    </T> */
    fun <T> bindUntilEvent(event: FragmentEvent): LifecycleTransformer<T> {
        return RxLifecycle.bindUntilEvent(provideLifecycleSubject(), event)
    }

    open fun finish() {
        U.getFragmentUtils().popFragment(this)
    }

    override fun onHiddenChanged(hidden: Boolean) {}
}
