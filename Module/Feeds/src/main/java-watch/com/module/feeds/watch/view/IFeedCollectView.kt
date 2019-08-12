package com.module.feeds.watch.view

import com.module.feeds.watch.model.FeedsCollectModel

interface IFeedCollectView {
    fun showCollectList(list: List<FeedsCollectModel>?)

    fun showCollect(model: FeedsCollectModel)

    fun requestError()
}