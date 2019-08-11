package com.module.feeds.detail.inter

import com.module.feeds.detail.model.FirstLevelCommentModel
import com.module.feeds.watch.model.FeedsWatchModel

interface IFeedsDetailView {
    fun addCommentSuccess(model: FirstLevelCommentModel)
    fun likeFeed(like: Boolean)
    fun showRelation(isBlacked: Boolean, isFollow: Boolean, isFriend: Boolean)
    fun collectFinish(c: Boolean)
    fun isCollect(c: Boolean)
    fun showFeedsWatchModel(model: FeedsWatchModel)
}