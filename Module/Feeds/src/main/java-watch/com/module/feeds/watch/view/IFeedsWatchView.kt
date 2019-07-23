package com.module.feeds.watch.view

import com.module.feeds.watch.model.FeedsWatchModel

interface IFeedsWatchView {
    fun addWatchList(list: List<FeedsWatchModel>, isClear: Boolean)

    fun requestTimeShort()  // 请求时间间隔太短

    fun requestError()
}