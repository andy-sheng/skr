package com.module.playways.doubleplay.inter

import com.module.playways.room.song.model.SongModel

interface IDoublePlayView {
    /**
     * @param pre  之前的，如果刚开始是null
     * @param mCur 当前的，是现在的轮次信息
     */
    fun changeRound(mCur: SongModel, mNext: SongModel)

    fun gameEnd(mCur: SongModel)
}
