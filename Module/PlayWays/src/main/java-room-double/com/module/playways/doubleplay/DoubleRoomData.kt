package com.module.playways.doubleplay

import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.LocalCombineRoomConfig
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.module.playways.doubleplay.event.*
import com.module.playways.doubleplay.model.DoubleSyncModel
import com.module.playways.doubleplay.pbLocalModel.LocalAgoraTokenInfo
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
    var userLockInfoMap: HashMap<Int, LocalUserLockInfo> = HashMap()

    /*
     所有参与游戏的人
     */
    var userInfoListMap: HashMap<Int, UserInfoModel>? = null

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

    lateinit var tokens: List<LocalAgoraTokenInfo> //声网token

    var needMaskUserInfo: Boolean = true

    var hasNextMusic: Boolean = false

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
        updateCombineRoomMusic(model.currentMusic, model.nextMusicDesc, model.isHasNextMusic)
        updateLockInfo(model.userLockInfo, model.isEnableNoLimitDuration)
    }

    fun updateCombineRoomMusic(localCombineRoomMusic: LocalCombineRoomMusic?, nextMusicDesc: String?, hasNext: Boolean) {
        if (localCombineRoomMusic == null || localCombineRoomMusic.music == null) {
            return
        }

        if (this.localCombineRoomMusic == null) {
            //localCombineRoomMusic == null 表示之前没有歌曲，localCombineRoomMusic.music == null表示游戏还没开始，还没有人点歌
            if (localCombineRoomMusic.music != null) {
                EventBus.getDefault().post(StartDoubleGameEvent(localCombineRoomMusic.music, nextMusicDesc, hasNext))
            }
        } else if (this.localCombineRoomMusic!!.uniqTag.equals(localCombineRoomMusic.uniqTag)) {
            //还是这个歌曲
            EventBus.getDefault().post(UpdateNextSongDecEvent(nextMusicDesc, hasNext))
        } else {
            //歌曲换了，需要更换歌词
            EventBus.getDefault().post(ChangeSongEvent(localCombineRoomMusic.music, nextMusicDesc, hasNext))
        }

        this.hasNextMusic = hasNext
        this.nextMusicDesc = nextMusicDesc
        this.localCombineRoomMusic = localCombineRoomMusic
    }

    fun updateLockInfo(localUserLockInfoList: List<LocalUserLockInfo>?, enableNoLimitDuration: Boolean) {
        //多人情况
        if (localUserLockInfoList == null) {
            MyLog.d(Tag, "updateLockInfo localUserLockInfoList is null")
            return
        }

        for (info in localUserLockInfoList) {
            if (userLockInfoMap[info.userID] == null) {
                userLockInfoMap[info.userID] = info
                EventBus.getDefault().post(UpdateLockEvent(info.userID, info.isHasLock))
            } else {
                if (userLockInfoMap[info.userID]?.isHasLock != info.isHasLock) {
                    userLockInfoMap[info.userID]?.isHasLock = info.isHasLock
                    EventBus.getDefault().post(UpdateLockEvent(info.userID, info.isHasLock))
                }
            }
        }

        if (this.enableNoLimitDuration != enableNoLimitDuration) {
            this.enableNoLimitDuration = enableNoLimitDuration
            EventBus.getDefault().post(UpdateNoLimitDuraionEvent(enableNoLimitDuration))
        }

    }

    fun getMaskAvatar(sex: Int) = if (sex == 1) getMaleAvatar() else getFeMaleAvatar()

    private fun getMaleAvatar() = config?.maskMaleAvatar ?: ""

    private fun getFeMaleAvatar() = config?.maskMaleAvatar ?: ""

    fun updateGameState(doubleGameState: DoubleGameState) {
        if (doubleGameState.value > this.doubleGameState.value) {
            this.doubleGameState = doubleGameState
        }
    }

    fun getAntherUser(): UserInfoModel? {
        val map = userInfoListMap
        if (map != null) {
            for (info in map) {
                if (info.key.toLong() != MyUserInfoManager.getInstance().uid) {
                    return info.value
                }
            }
        }

        return null
    }

    fun getToken(): String {
        for (token in tokens) {
            if (token.userID.toLong() == MyUserInfoManager.getInstance().uid) {
                return token.token
            }
        }

        MyLog.e(Tag, "自己的token是空的")
        return ""
    }

    override fun toString(): String {
        return "DoubleRoomData(Tag='$Tag', gameId=$gameId, doubleGameState=$doubleGameState, syncStatusTimeMs=$syncStatusTimeMs, passedTimeMs=$passedTimeMs, userLockInfoMap=$userLockInfoMap, userInfoListMap=$userInfoListMap, enableNoLimitDuration=$enableNoLimitDuration, localCombineRoomMusic=$localCombineRoomMusic, nextMusicDesc=$nextMusicDesc, config=$config, tokens=$tokens, needMaskUserInfo=$needMaskUserInfo)"
    }

    //未开始，已开始，已结束
    enum class DoubleGameState constructor(private val status: Int?) {
        HAS_NOT_START(0), START(1), END(2);

        val value: Int
            get() = status!!
    }


}
