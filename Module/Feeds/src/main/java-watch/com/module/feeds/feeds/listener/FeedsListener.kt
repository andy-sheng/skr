package com.module.feeds.feeds.listener

import com.module.feeds.feeds.model.FeedsWatchModel

interface FeedsListener {
    fun onClickMoreListener(watchModel: FeedsWatchModel?)
    fun onClickLikeListener(position: Int, watchModel: FeedsWatchModel?)
    fun onClickCommentListener(watchModel: FeedsWatchModel?)
    fun onClickHitListener(watchModel: FeedsWatchModel?)
    fun onClickDetailListener(watchModel: FeedsWatchModel?)
    fun onClickCDListener(watchModel: FeedsWatchModel?)
    fun onclickRankListener(watchModel: FeedsWatchModel?)
}