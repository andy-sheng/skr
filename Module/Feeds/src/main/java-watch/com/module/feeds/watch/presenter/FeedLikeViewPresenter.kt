package com.module.feeds.watch.presenter

import com.common.mvp.RxLifeCyclePresenter

class FeedLikeViewPresenter : RxLifeCyclePresenter(){

    init {
        addToLifeCycle()
    }

    override fun destroy() {
        super.destroy()
    }
}
