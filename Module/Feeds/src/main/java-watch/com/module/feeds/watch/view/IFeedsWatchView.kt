package com.module.feeds.watch.view

import com.module.feeds.watch.model.FeedsWatchModel

interface IFeedsWatchView {
    fun addWatchList(list: List<FeedsWatchModel>?, isClear: Boolean, hasMore: Boolean)

    // fun requestTimeShort()  // 请求时间间隔太短

    fun requestError()

    fun feedLikeResult(position: Int, model: FeedsWatchModel, isLike: Boolean)

    fun feedDeleteResult(position: Int, model: FeedsWatchModel)

    fun showCollect(position: Int, model: FeedsWatchModel)
}