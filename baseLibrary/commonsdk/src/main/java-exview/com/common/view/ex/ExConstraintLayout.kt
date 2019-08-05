package com.common.view.ex

import android.content.Context
import android.graphics.Canvas
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.MotionEvent

import com.common.lifecycle.ActivityLifecycleForRxLifecycle
import com.common.rx.ViewEvent
import com.common.view.ex.shadow.ShadowConfig
import com.common.view.ex.shadow.ShadowHelper
import com.trello.rxlifecycle2.LifecycleTransformer
import com.trello.rxlifecycle2.RxLifecycle

import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

open class ExConstraintLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr), RxLifecycleView, CoroutineScope by MainScope() {

    internal var mShadowConfig: ShadowConfig? = null

    internal var mListener: Listener? = null

    private var mLifecycleSubject: BehaviorSubject<ViewEvent>? = null

    constructor(context: Context) : this(context, null, 0) {}

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    init {
        attrs?.let {
            loadAttributes(context, it)
        }
    }

    private fun loadAttributes(context: Context, attrs: AttributeSet) {
        AttributeInject.injectBackground(this, context, attrs)
        //        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.background);
        //        int shadowColor = typedArray.getInt(R.styleable.background_bl_shadow_Color, Color.TRANSPARENT);
        //        typedArray.recycle();
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mShadowConfig != null) {
            //实现阴影
            ShadowHelper.draw(canvas, this, mShadowConfig)
        }
    }

    fun setShadowConfig(shadowConfig: ShadowConfig) {
        mShadowConfig = shadowConfig
        //invalidate();
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        /**
         * 是否需要在顶部把这个事件给拦截住，目前用于点击空白区域 ，美颜面板的消失
         */
        return if (mListener != null && mListener!!.onInterceptTouchEvent(ev)) {
            true
        } else super.onInterceptTouchEvent(ev)
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mLifecycleSubject != null) {
            mLifecycleSubject!!.onNext(ViewEvent.DETACH)
        }
        cancel()
    }

    /**
     * 事件在 [ActivityLifecycleForRxLifecycle]发出
     * 绑定 Activity 的指定生命周期
     *
     * @param <T>
     * @return
    </T> */
    override fun <T> bindDetachEvent(): LifecycleTransformer<T> {
        return RxLifecycle.bindUntilEvent(provideLifecycleSubject(), ViewEvent.DETACH)
    }

    fun provideLifecycleSubject(): Subject<ViewEvent> {
        if (mLifecycleSubject == null) {
            mLifecycleSubject = BehaviorSubject.create()
        }
        return mLifecycleSubject as Subject<ViewEvent>
    }

    interface Listener {
        fun onInterceptTouchEvent(ev: MotionEvent): Boolean
    }
}
