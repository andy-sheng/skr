package com.module.feeds.watch.presenter

import com.common.mvp.RxLifeCyclePresenter
import com.module.feeds.watch.model.FeedsLikeModel
import com.module.feeds.watch.view.IFeedLikeView

class FeedLikeViewPresenter(var view: IFeedLikeView) : RxLifeCyclePresenter() {

    init {
        addToLifeCycle()
    }

    fun getFeedsLikeList() {
        var list = ArrayList<FeedsLikeModel>()
        var i = 0
        while (i < 5) {
            i++
            list.add(FeedsLikeModel())
        }
        view.addLikeList(list, 0, true)
    }

    override fun destroy() {
        super.destroy()
    }
}
