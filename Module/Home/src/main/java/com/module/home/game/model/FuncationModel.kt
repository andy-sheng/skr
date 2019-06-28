package com.module.home.game.model

// 做任务，排行榜，练歌房（主要用来标记红点）
class FuncationModel(mTaskHasRed: Boolean) {

    var isTaskHasRed = false

    init {
        this.isTaskHasRed = mTaskHasRed
    }
}
