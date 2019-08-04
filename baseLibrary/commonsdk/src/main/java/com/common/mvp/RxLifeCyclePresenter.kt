package com.common.mvp

import android.support.annotation.CallSuper

import com.common.lifecycle.Lifecycleable
import com.trello.rxlifecycle2.LifecycleTransformer
import com.trello.rxlifecycle2.RxLifecycle

import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * Created by yangli on 16-8-5.
 */
abstract class RxLifeCyclePresenter : Presenter, PresenterLifecycleable , CoroutineScope by MainScope(){
    protected val TAG = tag
    private val mBehaviorSubject = BehaviorSubject.create<PresenterEvent>()
    internal var hasAddToLifeCycle = false

    protected val tag: String
        get() = javaClass.simpleName


    @CallSuper
    override fun addToLifeCycle() {
        hasAddToLifeCycle = true
    }

    @CallSuper
    override fun start() {
        mBehaviorSubject.onNext(PresenterEvent.START)
    }

    @CallSuper
    override fun resume() {
        mBehaviorSubject.onNext(PresenterEvent.RESUME)
    }

    @CallSuper
    override fun pause() {
        mBehaviorSubject.onNext(PresenterEvent.PAUSE)
    }

    @CallSuper
    override fun stop() {
        mBehaviorSubject.onNext(PresenterEvent.STOP)
    }

    @CallSuper
    override fun destroy() {
        mBehaviorSubject.onNext(PresenterEvent.DESTROY)
        cancel()
    }

    override fun provideLifecycleSubject(): Subject<PresenterEvent> {
        return mBehaviorSubject
    }

    fun <T> bindUntilEvent(event: PresenterEvent): LifecycleTransformer<T> {
        if (!hasAddToLifeCycle) {
            throw IllegalStateException("please add present to lifeCycle before call bindUntilEvent")
        }
        return RxLifecycle.bindUntilEvent(mBehaviorSubject, event)
    }


}
