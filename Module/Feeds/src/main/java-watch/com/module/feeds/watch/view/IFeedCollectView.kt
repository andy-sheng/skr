package com.module.feeds.watch.view

import com.module.feeds.watch.model.FeedsCollectModel

interface IFeedCollectView {
    fun addLikeList(list: List<FeedsCollectModel>?, isClear: Boolean)

    fun showCollect(model: FeedsCollectModel)

    fun requestError()
}