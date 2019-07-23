package com.module.feeds.watch.view

import com.module.feeds.watch.model.FeedsLikeModel

interface IFeedLikeView {
    fun addLikeList(list: List<FeedsLikeModel>, offset: Int, isClear: Boolean)

    fun requestError()
}