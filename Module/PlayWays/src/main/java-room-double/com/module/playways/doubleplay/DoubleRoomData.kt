package com.module.playways.doubleplay

import com.common.core.userinfo.model.LocalCombineRoomConfig
import com.common.core.userinfo.model.UserInfoModel
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

    /*
     房间id
     */
    var gameId = -1
        set(value) {
            if (value > 0) field = value
        }

    /*
     点第一首歌前是未开始，点第一首之后是开始状态
     */
    var doubleGameState = DoubleGameState.HAS_NOT_START

    /*
     状态同步时的毫秒时间戳
     */
    var syncStatusTimeMs: Long = 0

    /*
     房间已经经历的毫秒数，目前没啥用
     */
    var passedTimeMs: Long = 0

    /*
     游戏里的人的解锁状态
     */
    var userLockInfo: HashMap<Int, LocalUserLockInfo> = HashMap()

    /*
     所有参与游戏的人
     */
    var userInfoList: HashMap<Int, UserInfoModel>? = null

    /*
     有没有开启无限畅聊模式，匹配进来的默认是4分钟，两个人都解锁之后是无限畅聊
     邀请进来的是默认无限畅聊模式
     */
    var enableNoLimitDuration: Boolean = false //开启没有限制的持续时间

    /*
     下发的歌曲，跟之前的轮次有点类似
     */
    var localCombineRoomMusic: LocalCombineRoomMusic? = null

    /*
     下一首歌的提示语，如果为空表示没有下一首的意思
     */
    var nextMusicDesc: String? = null

    /*
     房间配置,后续需要扩展
     */
    var config: LocalCombineRoomConfig? = null

    init {

    }

    fun syncRoomInfo(model: DoubleSyncModel?) {
        if (doubleGameState != DoubleGameState.END) {
            if (model == null) {
                //结束
                doubleGameState = DoubleGameState.END
                return
            }

            if (doubleGameState == DoubleGameState.HAS_NOT_START) {
                doubleGameState = DoubleGameState.START
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

        if (this.localCombineRoomMusic == null) {
            //游戏开始
            EventBus.getDefault().post(StartDoubleGameEvent(localCombineRoomMusic.music, nextMusicDesc))
        } else if (this.localCombineRoomMusic!!.music.itemID == localCombineRoomMusic.music.itemID) {
            //还是这个歌曲
        } else {
            //歌曲换了，需要更换歌词
            EventBus.getDefault().post(StartDoubleGameEvent(localCombineRoomMusic.music, nextMusicDesc))
        }

        this.nextMusicDesc = nextMusicDesc
        this.localCombineRoomMusic = localCombineRoomMusic
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
        if (doubleGameState.value > this.doubleGameState.value) {
            this.doubleGameState = doubleGameState
        }
    }

    //未开始，已开始，已结束
    enum class DoubleGameState constructor(private val status: Int?) {
        HAS_NOT_START(0), START(1), END(2);

        val value: Int
            get() = status!!
    }
}
