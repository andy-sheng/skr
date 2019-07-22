package com.module.feeds.watch.presenter

import com.common.mvp.RxLifeCyclePresenter
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.view.IFeedsWatchView

class FeedWatchViewPresenter(var view: IFeedsWatchView) : RxLifeCyclePresenter() {
    init {
        addToLifeCycle()
    }

    fun getWatchList(flag: Boolean) {
        var list = ArrayList<FeedsWatchModel>()
        var i: Int = 0
        while (i < 10) {
            i++
            list.add(FeedsWatchModel())
        }
        view.addWatchList(list, 0, true)
    }

    override fun destroy() {
        super.destroy()
    }
}