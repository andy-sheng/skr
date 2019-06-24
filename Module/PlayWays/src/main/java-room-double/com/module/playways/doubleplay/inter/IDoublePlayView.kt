package com.module.playways.doubleplay.inter

import com.module.playways.doubleplay.pushEvent.DoubleEndCombineRoomPushEvent
import com.module.playways.room.song.model.SongModel

interface IDoublePlayView {
    /**
     * @param pre  之前的，如果刚开始是null
     * @param mCur 当前的，是现在的轮次信息
     */
    fun changeRound(mCur: SongModel, mNext: String, hasNext: Boolean)

    fun picked()

    fun gameEnd(doubleEndCombineRoomPushEvent: DoubleEndCombineRoomPushEvent)

    fun showLockState(userID: Int, lockState: Boolean)

    fun showNoLimitDurationState(noLimit: Boolean)

    fun startGame(mCur: SongModel, mNext: String, hasNext: Boolean)

    fun finishActivityWithError()

    fun updateNextSongDec(mNext: String, hasNext: Boolean)

    fun finishActivity()
}
