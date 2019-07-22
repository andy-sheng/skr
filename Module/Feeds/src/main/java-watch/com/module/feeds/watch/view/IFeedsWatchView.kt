package com.module.feeds.watch.view

import com.module.feeds.watch.model.FeedsWatchModel

interface IFeedsWatchView {
    fun addWatchList(list: List<FeedsWatchModel>, offset: Int, isClear: Boolean)
}