package com.module.feeds.watch.presenter

import com.common.mvp.RxLifeCyclePresenter

class FeedCollectViewPresenter : RxLifeCyclePresenter(){

    init {
        addToLifeCycle()
    }

    override fun destroy() {
        super.destroy()
    }
}
