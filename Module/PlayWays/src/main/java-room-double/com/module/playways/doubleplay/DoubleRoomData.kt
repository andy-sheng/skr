package com.module.playways.doubleplay

import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.Common.UserInfo
import java.io.Serializable

class DoubleRoomData() : Serializable {
    val Tag = "DoubleRoomData"
    internal var mGameId = -1
    internal var mFrom = -1
    internal var mDoubleGameState = DoubleGameState.HAS_NOT_START
    internal var mGuestUserInfo: UserInfo? = null
    internal var mGuestMicroState: Boolean = false
    internal var mCurRoundInfo: SongModel? = null
    internal var mNextRoundInfo: SongModel? = null

    init {

    }

    fun updateRoundInfo(mCurSongModel: SongModel?, mNextSongModel: SongModel?) {
        if (mDoubleGameState != DoubleGameState.END) {
            if (mCurSongModel == null && mNextSongModel == null) {
                //结束
                mDoubleGameState = DoubleGameState.END
                return
            }

            if (mDoubleGameState == DoubleGameState.HAS_NOT_START) {
                mDoubleGameState = DoubleGameState.START
            }

            if (mCurRoundInfo == null) {
                //游戏开始
                mCurRoundInfo = mCurSongModel
                mNextRoundInfo = mNextSongModel
            } else if (mCurRoundInfo!!.itemID != mCurSongModel!!.itemID) {
                //更新
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
}
