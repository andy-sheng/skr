package com.module.playways.doubleplay.inter;

import com.module.playways.doubleplay.DoubleRoundInfo;

public interface IDoublePlayView {
    /**
     * @param pre  之前的，如果刚开始是null
     * @param mCur 当前的，是现在的轮次信息
     */
    void changeRound(DoubleRoundInfo pre, DoubleRoundInfo mCur);

    void gameEnd(DoubleRoundInfo mCur);
}
