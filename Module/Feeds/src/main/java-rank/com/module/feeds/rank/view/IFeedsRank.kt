package com.module.feeds.rank.view

import com.module.feeds.rank.model.FeedRankTagModel

interface IFeedsRank {
    fun showFeedRankTag(list: List<FeedRankTagModel>?)

    fun showFailed()
}