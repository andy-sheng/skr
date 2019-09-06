package com.common.view

import android.view.View
import android.view.ViewStub
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * 包裹懒加载view的一些常用方法
 */
abstract class ExViewStub(protected var mViewStub: ViewStub?) : View.OnAttachStateChangeListener, CoroutineScope by MainScope() {
    protected var mParentView: View? = null

    val realView: View?
        get() = if (mParentView == null) {
            mViewStub
        } else mParentView

    fun tryInflate() {
        if (mParentView == null) {
            mParentView = mViewStub!!.inflate()
            mParentView?.let {
                it.addOnAttachStateChangeListener(this)
                it.translationY = mViewStub!!.translationY
                onViewAttachedToWindow(it)
                init(it)
            }
            mViewStub = null
        }
    }

    open fun setVisibility(visibility: Int) {
        if (visibility == View.GONE) {
            if (mParentView != null) {
                mParentView?.setVisibility(View.GONE)
                mParentView?.clearAnimation()
            }
        } else {
            tryInflate()
            mParentView?.setVisibility(visibility)
        }
    }

    protected abstract fun init(parentView: View)

    /**
     * 只要描述这个 ExViewStub 包裹的Layout到底是哪个
     * 方便查找维护，对代码逻辑不会有任何作用
     *
     * @return
     */
    protected abstract fun layoutDesc(): Int

    override fun onViewAttachedToWindow(v: View) {

    }

    override fun onViewDetachedFromWindow(v: View) {
        cancel()
    }

    fun setTranslateY(ty: Float) {
        if (mParentView == null) {
            mViewStub!!.translationY = ty
            return
        }
        mParentView!!.translationY = ty
    }
}
