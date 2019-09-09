package com.module.playways.room.prepare.presenter

import com.common.mvp.RxLifeCyclePresenter

abstract class BaseMatchPresenter : RxLifeCyclePresenter() {
    abstract fun startLoopMatchTask()

    abstract fun cancelMatch()
}
