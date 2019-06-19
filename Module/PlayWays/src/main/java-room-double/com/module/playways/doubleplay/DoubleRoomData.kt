package com.module.playways.doubleplay

import com.module.playways.doubleplay.event.StartDoubleGameEvent
import com.module.playways.doubleplay.event.UpdateLockEvent
import com.module.playways.doubleplay.event.UpdateNoLimitDuraionEvent
import com.module.playways.doubleplay.model.DoubleSyncModel
import com.module.playways.doubleplay.pbLocalModel.LocalCombineRoomMusic
import com.module.playways.doubleplay.pbLocalModel.LocalUserLockInfo
import org.greenrobot.eventbus.EventBus
import java.io.Serializable
import java.util.*

class DoubleRoomData() : Serializable {
    val Tag = "DoubleRoomData"
    internal var mGameId = -1
    internal var mDoubleGameState = DoubleGameState.HAS_NOT_START
    internal var syncStatusTimeMs: Long = 0 //状态同步时的毫秒时间戳
    internal var passedTimeMs: Long = 0 //房间已经经历的毫秒数
    internal var userLockInfo: HashMap<Int, LocalUserLockInfo> = HashMap<Int, LocalUserLockInfo>()
    internal var enableNoLimitDuration: Boolean = false //开启没有限制的持续时间
    internal var mLocalCombineRoomMusic: LocalCombineRoomMusic? = null
    internal var nextMusicDesc: String? = null

    init {

    }

    fun syncRoomInfo(model: DoubleSyncModel?) {
        if (mDoubleGameState != DoubleGameState.END) {
            if (model == null) {
                //结束
                mDoubleGameState = DoubleGameState.END
                return
            }

            if (mDoubleGameState == DoubleGameState.HAS_NOT_START) {
                mDoubleGameState = DoubleGameState.START
            }

            setData(model)
        }
    }

    private fun setData(model: DoubleSyncModel) {
        syncStatusTimeMs = model.syncStatusTimeMs
        passedTimeMs = model.passedTimeMs
        updateCombineRoomMusic(model.currentMusic, model.nextMusicDesc)
        updateLockInfo(model.userLockInfo, model.isEnableNoLimitDuration)
    }

    fun updateCombineRoomMusic(localCombineRoomMusic: LocalCombineRoomMusic?, nextMusicDesc: String?) {
        if (localCombineRoomMusic == null || localCombineRoomMusic.music == null) {
            return
        }

        if (mLocalCombineRoomMusic == null) {
            //游戏开始
            EventBus.getDefault().post(StartDoubleGameEvent(localCombineRoomMusic.music, nextMusicDesc))
        } else if (mLocalCombineRoomMusic!!.music.itemID == localCombineRoomMusic.music.itemID) {
            //还是这个歌曲
        } else {
            //歌曲换了，需要更换歌词
            EventBus.getDefault().post(StartDoubleGameEvent(localCombineRoomMusic.music, nextMusicDesc))
        }

        this.nextMusicDesc = nextMusicDesc
        this.mLocalCombineRoomMusic = localCombineRoomMusic
    }

    fun updateLockInfo(localUserLockInfoList: List<LocalUserLockInfo>, enableNoLimitDuration: Boolean) {
        //多人情况
        for (info in localUserLockInfoList) {
            if (userLockInfo[info.userID] == null) {
                userLockInfo[info.userID] = info
                EventBus.getDefault().post(UpdateLockEvent(info.userID, info.isHasLock))
            } else {
                if (userLockInfo[info.userID]?.isHasLock != info.isHasLock) {
                    userLockInfo[info.userID]?.isHasLock = info.isHasLock
                    EventBus.getDefault().post(UpdateLockEvent(info.userID, info.isHasLock))
                }
            }
        }

        if (this.enableNoLimitDuration != enableNoLimitDuration) {
            this.enableNoLimitDuration = enableNoLimitDuration
            EventBus.getDefault().post(UpdateNoLimitDuraionEvent(enableNoLimitDuration))
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
