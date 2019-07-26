package com.module.feeds.rank.view

import com.component.feeds.model.FeedRankModel

interface IFeedsRank {
    fun showFeedRankTag(list: List<FeedRankModel>?)

    fun showFailed()
}