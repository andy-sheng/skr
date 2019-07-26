package com.module.feeds.detail.inter

import com.module.feeds.detail.model.FirstLevelCommentModel

interface IFeedsDetailView {
    fun addCommentSuccess(model: FirstLevelCommentModel)
    fun likeFeed(like: Boolean)
    fun showRelation(isBlacked: Boolean, isFollow: Boolean, isFriend: Boolean)
}