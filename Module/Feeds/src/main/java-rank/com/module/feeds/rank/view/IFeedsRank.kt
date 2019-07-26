package com.module.feeds.rank.view

import com.module.feeds.watch.model.FeedRankModel

interface IFeedsRank {
    fun showFeedRankTag(list: List<FeedRankModel>?)

    fun showFailed()
}