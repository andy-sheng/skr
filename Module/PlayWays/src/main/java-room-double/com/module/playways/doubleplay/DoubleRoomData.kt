package com.module.playways.doubleplay

import com.common.log.MyLog
import com.zq.live.proto.Common.UserInfo

import java.io.Serializable

class DoubleRoomData(doubleGameState: DoubleGameState) : Serializable {

    internal var mDoubleGameState = DoubleGameState.HAS_NOT_START
    internal var mGuestUserInfo: UserInfo? = null
    internal var mGuestMicroState: Boolean = false
    internal var mCurRoundInfo: DoubleRoundInfo? = null

    init {
        mDoubleGameState = doubleGameState
    }

    fun updateRoundInfo(roundInfo: DoubleRoundInfo?) {
        if (mDoubleGameState != DoubleGameState.END) {
            if (roundInfo == null) {
                //结束
                mDoubleGameState = DoubleGameState.END
                return
            }

            if (mDoubleGameState == DoubleGameState.HAS_NOT_START) {
                mDoubleGameState = DoubleGameState.START
            }

            if (mCurRoundInfo == null) {
                //游戏开始
            } else if (mCurRoundInfo!!.roundSeq == roundInfo.roundSeq) {
                //更新
            } else if (roundInfo.roundSeq > mCurRoundInfo!!.roundSeq) {
                //切换
            } else {
                MyLog.d(TAG, "updateRoundInfo roundInfo=$roundInfo, mCurRoundInfo=$mCurRoundInfo")
            }
        }


    }

    fun updateGameState(doubleGameState: DoubleGameState) {
        if (doubleGameState.value > mDoubleGameState.value) {
            mDoubleGameState = doubleGameState
        }
    }

    //未开始，已开始，已结束
    enum class DoubleGameState constructor(private val status: Int?) {
        HAS_NOT_START(0), START(1), END(2);

        val value: Int
            get() = status!!
    }

    companion object {
        val TAG = "DoubleRoomData"
    }
}
