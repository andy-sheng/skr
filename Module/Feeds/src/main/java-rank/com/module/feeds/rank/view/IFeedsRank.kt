package com.module.feeds.rank.view

import com.module.feeds.feeds.model.FeedRankModel

interface IFeedsRank {
    fun showFeedRankTag(list: List<FeedRankModel>?)

    fun showFailed()
}