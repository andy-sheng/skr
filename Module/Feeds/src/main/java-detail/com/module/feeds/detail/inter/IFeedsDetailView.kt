package com.module.feeds.detail.inter

import com.module.feeds.detail.model.FirstLevelCommentModel

interface IFeedsDetailView {
    fun addCommentSuccess(model: FirstLevelCommentModel)
    fun likeFeed(like: Boolean)
    fun showRelation(isBlacked: Boolean, isFollow: Boolean, isFriend: Boolean)
    fun collectFinish(c: Boolean)
    fun isCollect(c: Boolean)
    fun showExtraInfo(commentCnt: Int, exposure: Int, isLiked: Boolean, shareCnt: Int, starCnt: Int)
}