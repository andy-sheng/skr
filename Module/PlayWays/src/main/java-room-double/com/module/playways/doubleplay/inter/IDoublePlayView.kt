package com.module.playways.doubleplay.inter

import com.module.playways.doubleplay.DoubleRoundInfo

interface IDoublePlayView {
    /**
     * @param pre  之前的，如果刚开始是null
     * @param mCur 当前的，是现在的轮次信息
     */
    fun changeRound(pre: DoubleRoundInfo, mCur: DoubleRoundInfo)

    fun gameEnd(mCur: DoubleRoundInfo)
}
