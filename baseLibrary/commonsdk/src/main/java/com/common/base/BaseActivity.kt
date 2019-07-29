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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.LayoutInflaterCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager

import com.common.base.delegate.IActivity
import com.common.cache.Cache
import com.common.cache.IntelligentCache
import com.common.lifecycle.ActivityLifecycleForRxLifecycle
import com.common.lifecycle.ActivityLifecycleable
import com.common.log.MyLog
import com.common.mvp.Presenter
import com.common.statistics.StatisticsAdapter
import com.common.utils.AndroidBug5497WorkaroundSupportingTranslucentStatus
import com.common.utils.U
import com.jude.swipbackhelper.SwipeBackHelper
import com.trello.rxlifecycle2.LifecycleTransformer
import com.trello.rxlifecycle2.RxLifecycle
import com.trello.rxlifecycle2.android.ActivityEvent

import org.greenrobot.eventbus.EventBus

import java.util.HashSet

import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * ================================================
 * 因为 Java 只能单继承,所以如果要用到需要继承特定 [Activity] 的三方库,那你就需要自己自定义 [Activity]
 * 继承于这个特定的 [Activity],然后再按照 [BaseActivity] 的格式,将代码复制过去,记住一定要实现[IActivity]
 *
 *
 * Created by JessYan on 22/03/2016
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
abstract class BaseActivity : AppCompatActivity(), IActivity, ActivityLifecycleable,CoroutineScope by MainScope() {
    protected val TAG = this.javaClass.simpleName
    private val mLifecycleSubject = BehaviorSubject.create<ActivityEvent>()
    // 想加入activity生命周期管理的presenter放在这里
    private val mPresenterSet = HashSet<Presenter>()

    //protected boolean mOnlyForFragmentContainer = true; // 是否只是fragment 的容器，会影响打点统计，如果只是容器，不统计Activity的session

    protected var mIsDestroyed = false

    /*用来控制是否采用沉浸式*/
    protected var mIsProfileMode = true

    internal var mAndroidBug5497WorkaroundSupportingTranslucentStatus: AndroidBug5497WorkaroundSupportingTranslucentStatus? = null

    /**
     * 可以设置沉浸式的条件，手动配置，同时满足版本要求
     */
    //        MyLog.d(TAG, "isProfileMode r=" + r);
    fun isProfileMode() = mIsProfileMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT


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

    override fun attachBaseContext(newBase: Context) {
        //        MyLog.d(TAG, "before getBaseContext " + getBaseContext() + " " + super.getClass());
        super.attachBaseContext(newBase)
        //        MyLog.d(TAG, "after getBaseContext " + getBaseContext() + " " + super.getClass());
    }

    override fun provideLifecycleSubject(): Subject<ActivityEvent> {
        return mLifecycleSubject
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        MyLog.d(TAG, "onCreate" + hashCode())
        /**
         * 解决虚拟按键遮挡布局问题
         * 只会让虚拟按键变透明，布局没有动
         */
        //        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        //animationEnter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        /**
         * 注意，这个方法的作用是可以接管 View 的实例化过程。在这里我可以对 View 或者 其属性都可以做替换。
         * 可能的用途
         * 1. 批量修改 View 的属性，但不建议使用
         * 2. 用于我们代码无法控制的第三方库，替换他的 View 或者 属性,
         * 可以用 javassist 字节码技术先往第三方的activity 搞一个代理，
         * 其余的就随便我们操作了。
         * 3. 动态换肤功能
         * 这块动态换肤功能，网上的文章也很多，但是基本的原理都一样，也是用了我们本文的知识，和上面的更换字体类似，我们可以对做了标记的View进行识别，然后在onCreateView遍历到它的时候，更改它的一些属性，比如背景色等，然后再交给系统去生成View。
         * 4. 无需编写shape、selector，直接在xml设置值
         */
//        LayoutInflaterCompat.setFactory2(LayoutInflater.from(this), object : LayoutInflater.Factory2 {
//
//            //'这个方法是Factory接口里面的，因为Factory2是继承Factory的'
//            override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View?{
//                return delegate.createView( null,name, context, attrs)
//            }
//
//            //'这个方法是Factory2里面定义的方法'
//            override fun onCreateView(parent: View, name: String, context: Context, attrs: AttributeSet): View {
//                //                MyLog.e(TAG, "parent:" + parent + ",name = " + name);
//                val n = attrs.attributeCount
//                for (i in 0 until n) {
//                    //                    MyLog.e(TAG, attrs.getAttributeName(i) + " , " + attrs.getAttributeValue(i));
//                }
//                //我们只是更换了参数，但最终实例化View的逻辑还是交给了AppCompatDelegateImpl
//                return delegate.createView(parent, name, context, attrs)
//            }
//        })

        super.onCreate(savedInstanceState)
        if (canSlide()) {
            SwipeBackHelper.onCreate(this)
        }

        var layoutResID = initView(savedInstanceState)
        //如果initView返回0,框架则不会调用setContentView(),当然也不会 Bind ButterKnife
        //        if (layoutResID != 0) {
        //            /**
        //             * 这里会导致 AndroidBug5497WorkaroundSupportingTranslucentStatus 没法注册
        //             * 当然这里也不需要注册，注册逻辑应该是Fragment自己控制。
        //             */
        //            setContentView(layoutResID);
        //        }
        if (layoutResID == 0) {
            layoutResID = R.layout.empty_activity_layout
        }
        setContentView(layoutResID)
        //        ViewGroup contentFrameLayout = (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);
        //        View parentView = contentFrameLayout.getChildAt(0);
        //        if (parentView != null && Build.VERSION.SDK_INT >= 14) {
        //            parentView.setFitsSystemWindows(true);
        //        }
        initData(savedInstanceState)
        if (useEventBus()) {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this)
            }
        }
    }

    override fun setContentView(layoutResID: Int) {
        MyLog.d(TAG, "setContentView id=$layoutResID")
        super.setContentView(layoutResID)
        if (isProfileMode()) {
            setProfileMode()
        }
        mAndroidBug5497WorkaroundSupportingTranslucentStatus = AndroidBug5497WorkaroundSupportingTranslucentStatus.assistActivity(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (canSlide()) {
            SwipeBackHelper.onPostCreate(this)
        }
    }

    /**
     * 设置名片沉浸式样式
     */
    protected fun setProfileMode() {
        //        MyLog.d(TAG, "setProfileMode");
        /**
         * 跟白色状态有关么
         */
        U.getStatusBarUtil().setTransparentBar(this, Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    }


    override fun onRestart() {
        MyLog.d(TAG, "onRestart" + hashCode())
        super.onRestart()
    }

    override fun onStart() {
        MyLog.d(TAG, "onStart" + hashCode())
        super.onStart()
        for (presenter in mPresenterSet) {
            presenter.start()
        }
    }

    override fun onResume() {
        MyLog.d(TAG, "onResume" + hashCode())
        StatisticsAdapter.recordSessionStart(this, this.javaClass.simpleName)
        super.onResume()
        for (presenter in mPresenterSet) {
            presenter.resume()
        }
    }

    override fun onPause() {
        MyLog.d(TAG, "onPause" + hashCode())
        super.onPause()
        /**
         * 02-22 21:39:58.831 D/PlayWaysActivity(20945): onPause137180481
         * 02-22 21:39:58.831 D/UmengStatistics(20945): recordSessionEnd key=PlayWaysActivity
         * 02-22 21:39:58.832 D/SongSelectFragment(20945): onPause
         * 02-22 21:39:58.832 D/SongSelectFragment(20945): onFragmentInvisible
         * 02-22 21:39:58.832 D/UmengStatistics(20945): recordPageEnd pageName=SongSelectFragment
         *
         * 这么改因为先调用 Activity 的onPause 在调用 Fragment 的 onPause
         * 导致统计顺序可能有问题
         */
        //        mUiHanlder.post(new Runnable() {
        //            @Override
        //            public void run() {
        //                StatisticsAdapter.recordSessionEnd(activity, activity.getClass().getSimpleName());
        //            }
        //        });
        StatisticsAdapter.recordSessionEnd(this, this.javaClass.simpleName)
        for (presenter in mPresenterSet) {
            presenter.pause()
        }
    }

    override fun onStop() {
        MyLog.d(TAG, "onStop" + hashCode())
        super.onStop()
        for (presenter in mPresenterSet) {
            presenter.stop()
        }
        if (isFinishing && !mIsDestroyed) {
            destroy()
            mIsDestroyed = true
        }
    }

    override fun onDestroy() {
        MyLog.d(TAG, "onDestroy" + hashCode())
        if (MyLog.isDebugLogOpen()) {
            super.onDestroy()
            if (!mIsDestroyed) {
                destroy()
                mIsDestroyed = true
            }
        } else {
            try {
                super.onDestroy()
                if (!mIsDestroyed) {
                    destroy()
                    mIsDestroyed = true
                }
            } catch (e: Exception) {
                MyLog.e(e)
            }

        }
    }

    override fun finish() {
        MyLog.d(TAG, "start finish")
        super.finish()
        //animationOut();
    }

    protected fun animationEnter() {
        overridePendingTransition(R.anim.translate_right_to_center, R.anim.translate_center_to_left)
    }

    protected fun animationOut() {
        overridePendingTransition(R.anim.translate_left_to_center, R.anim.translate_center_to_right)
    }

    protected open fun destroy() {
        MyLog.d(TAG, "destroy")
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        if (canSlide()) {
            SwipeBackHelper.onDestroy(this)
        }
        for (presenter in mPresenterSet) {
            presenter.destroy()
        }
        mPresenterSet.clear()

        if (mAndroidBug5497WorkaroundSupportingTranslucentStatus != null) {
            mAndroidBug5497WorkaroundSupportingTranslucentStatus!!.destroy()
        }
        cancel()
    }


    override fun onBackPressed() {
        if (U.getCommonUtils().isFastDoubleClick()) {
            return
        }

        /**
         * 先看看有没有顶层的 fragment 要处理这个事件的
         * 因为有可能顶层的 fragment 要收回键盘 表情面板等操作
         */
        val fragment = U.getFragmentUtils().getTopFragment(this)
        if (fragment != null) {
            if (fragment.onBackPressed()) {
                // 以及消费掉了
                return
            }
        }

        /**
         * 这里有个问题，就是如果是首页Activity 的Fragment 很可能走不到 super.onBackPressed() 因为被拦截了
         * 所以首页 onBackPressedForActivity 自己特殊处理下
         */
        if (onBackPressedForActivity()) {
            // activity也消费掉了
            return
        }
        // 才能走系统的消费
        super.onBackPressed()
    }

    /**
     * activity 请只覆盖这个方法 不覆盖onBackPressed
     *
     * @return
     */
    open fun onBackPressedForActivity(): Boolean {
        return false
    }


    /**
     * 是否使用eventBus,默认为使用(true)，
     *
     * @return
     */
    override fun useEventBus(): Boolean {
        return true
    }

    open fun canSlide(): Boolean {
        return canSlide
    }

    /**
     * 当键盘出现时，自己管理布局
     * 如果为true，则会接受到 android5497 传出的 keyborad事件，自行调整布局
     * 如果为false，则会自动上滑调整布局
     *
     * @return
     */
    open fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        /**
         * 先看看有没有顶层的 fragment 要处理这个事件的
         * 因为有可能顶层的 fragment 要收回键盘 表情面板等操作
         */
        val fm = this.supportFragmentManager
        if (fm != null) {
            val fls = fm.fragments
            var topFragmentNotInViewPager: BaseFragment? = null
            // 倒过来遍历
            for (i in fls.indices.reversed()) {
                val f = fls[i]
                if (f is BaseFragment) {
// 如果Fragment 在ViewPager 是是否可见判断顶部
                    if (f.isInViewPager()) {
                        if (f.fragmentVisible) {
                            if (f.onActivityResultReal(requestCode, resultCode, data)) {
                                return
                            }
                        }
                    } else {
                        if (topFragmentNotInViewPager == null) {
                            topFragmentNotInViewPager = f
                        }
                    }
                }
            }
            // 如果Fragment 不在viewPager中 则 最后一个就是顶部的
            if (topFragmentNotInViewPager != null) {
                if (topFragmentNotInViewPager.onActivityResultReal(requestCode, resultCode, data)) {
                    return
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
        MyLog.d(TAG, "onActivityResult requestCode=$requestCode resultCode=$resultCode data=$data")
    }

    /**
     * 这个Activity是否会使用Fragment,框架会根据这个属性判断是否注册[android.support.v4.app.FragmentManager.FragmentLifecycleCallbacks]
     * 如果返回false,那意味着这个Activity不需要绑定Fragment,那你再在这个Activity中绑定继承于 [com.common.base.BaseFragment] 的Fragment将不起任何作用
     *
     * @return
     */
    override fun useFragment(): Boolean {
        return true
    }

    /**
     * 事件在 [ActivityLifecycleForRxLifecycle]发出
     * 绑定 Activity 的指定生命周期
     *
     * @param event
     * @param <T>
     * @return
    </T> */
    fun <T> bindUntilEvent(event: ActivityEvent): LifecycleTransformer<T> {
        return RxLifecycle.bindUntilEvent(provideLifecycleSubject(), event)
    }

    companion object {

        internal var canSlide = U.app().resources.getBoolean(R.bool.translucent_no_bug)  //保存结果
    }
}
