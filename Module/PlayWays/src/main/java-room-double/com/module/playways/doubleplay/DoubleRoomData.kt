package com.module.playways.doubleplay

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.LocalCombineRoomConfig
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.module.playways.doubleplay.event.*
import com.module.playways.doubleplay.model.DoubleSyncModel
import com.module.playways.doubleplay.pbLocalModel.*
import com.zq.live.proto.CombineRoom.EGameStage
import com.zq.live.proto.Common.ESceneType
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
    var doubleGameState = DoubleGameState.START

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

    var doubleRoomOri: DoubleRoomOri? = null

    var inviterId: Long? = null

    var gameSenceDataModel: LocalGameSenceDataModel? = null

    /**
     * 这个是第一次进入游戏场景的时候的数据
     */
    var localGamePanelInfo: LocalGamePanelInfo? = null

    /**
     * 当前场景，默认是0
     */
    var sceneType: Int = 0

    init {

    }

    fun syncRoomInfo(model: DoubleSyncModel?) {
        if (doubleGameState != DoubleGameState.END) {
            if (model == null) {
                MyLog.e(Tag, "syncRoomInfo model is null")
                return
            }

            setData(model)
        } else {
            MyLog.w(Tag, "syncRoomInfo doubleGameState == DoubleGameState.END")
        }
    }

    private fun setData(model: DoubleSyncModel) {
        MyLog.d(Tag, "setData model is " + model.toString())
        syncStatusTimeMs = model.syncStatusTimeMs
        passedTimeMs = model.passedTimeMs
        if (sceneType != model.sceneType) {
            EventBus.getDefault().post(UpdateRoomSceneEvent(sceneType, model.sceneType))
            sceneType = model.sceneType
        }

        if (model.sceneType == ESceneType.ST_Chat.value) {
            updateChatSceneData(model.localChatSenceDataModel)
        } else if (model.sceneType == ESceneType.ST_Game.value) {
            updateGameSceneData(model.localGameSenceDataModel)
        } else if (model.sceneType == ESceneType.ST_Sing.value) {
            updateCombineRoomMusic(model.localSingSenceDataModel.currentMusic, model.localSingSenceDataModel.nextMusicDesc, model.localSingSenceDataModel.isHasNextMusic)
        }

        updateLockInfo(model.userLockInfo, model.isEnableNoLimitDuration)
    }

    fun changeScene(sceneType: Int) {
        if (this.sceneType != sceneType) {
            EventBus.getDefault().post(UpdateRoomSceneEvent(this.sceneType, sceneType))
            this.sceneType = sceneType
        }
    }

    fun updateGameSceneData(localGameSenceDataModel: LocalGameSenceDataModel) {
        if (gameSenceDataModel == null) {
            gameSenceDataModel = localGameSenceDataModel
            EventBus.getDefault().post(UpdateGameSceneEvent(localGameSenceDataModel))
        } else {
            if (gameSenceDataModel?.gameStage != localGameSenceDataModel.gameStage) {
                EventBus.getDefault().post(UpdateGameSceneEvent(localGameSenceDataModel))
            } else {
                if (gameSenceDataModel?.gameStage == EGameStage.GS_ChoicGameItem.value && gameSenceDataModel?.panelSeq != localGameSenceDataModel.panelSeq) {
                    EventBus.getDefault().post(UpdateGameSceneEvent(localGameSenceDataModel))
                } else if (gameSenceDataModel?.gameStage == EGameStage.GS_InGamePlay.value && gameSenceDataModel?.itemID != localGameSenceDataModel.itemID) {
                    EventBus.getDefault().post(UpdateGameSceneEvent(localGameSenceDataModel))
                }
            }
        }
    }

    fun updateChatSceneData(localChatSenceDataModel: LocalChatSenceDataModel) {

    }

    fun updateCombineRoomMusic(localCombineRoomMusic: LocalCombineRoomMusic?, nextMusicDesc: String?, hasNext: Boolean) {
        MyLog.w(Tag, "updateCombineRoomMusic localCombineRoomMusic is $localCombineRoomMusic, nextMusicDesc is $nextMusicDesc, hasNext is $hasNext")
        if (localCombineRoomMusic == null) {
            return
        }

        if (localCombineRoomMusic.music == null) {
            if (this.localCombineRoomMusic != null) {
                EventBus.getDefault().post(NoMusicEvent())
            }

            this.localCombineRoomMusic = null
            return
        }

        MyLog.d(Tag, "updateCombineRoomMusic localCombineRoomMusic is $localCombineRoomMusic, nextMusicDesc is $nextMusicDesc, hasNext is $hasNext");
        if (this.localCombineRoomMusic == null) {
            //localCombineRoomMusic == null 表示之前没有歌曲，localCombineRoomMusic.music == null表示游戏还没开始，还没有人点歌
            if (localCombineRoomMusic.music != null) {
                EventBus.getDefault().post(StartSingEvent(localCombineRoomMusic, nextMusicDesc, hasNext))
            }
        } else if (this.localCombineRoomMusic!!.uniqTag.equals(localCombineRoomMusic.uniqTag)) {
            //还是这个歌曲
            EventBus.getDefault().post(UpdateNextSongDecEvent(nextMusicDesc, hasNext))
        } else {
            //歌曲换了，需要更换歌词
            EventBus.getDefault().post(ChangeSongEvent(localCombineRoomMusic, nextMusicDesc, hasNext))
        }

        this.hasNextMusic = hasNext
        this.nextMusicDesc = nextMusicDesc
        this.localCombineRoomMusic = localCombineRoomMusic
    }

    fun selfUnLock() {
        if (userLockInfoMap[MyUserInfoManager.getInstance().uid.toInt()] != null) {
            userLockInfoMap[MyUserInfoManager.getInstance().uid.toInt()]?.isHasLock = false
            EventBus.getDefault().post(UpdateLockEvent(MyUserInfoManager.getInstance().uid.toInt(), false))
        }
    }

    fun updateLockInfo(localUserLockInfoList: List<LocalUserLockInfo>?, enableNoLimitDuration: Boolean) {
        //多人情况
        if (localUserLockInfoList == null) {
            MyLog.d(Tag, "updateLockInfo localUserLockInfoList is null")
            return
        }

        for (info in localUserLockInfoList) {
            val map = userInfoListMap
            if (map == null || map[info.userID] == null) {
                continue
            }

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

    private fun getFeMaleAvatar() = config?.maskFemaleAvatar ?: ""

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

    fun getAvatarById(id: Int): String {
        if (id == MyUserInfoManager.getInstance().uid.toInt()) {
            return getSelfAvatar()
        } else {
            return getPartnerAvatar()
        }
    }

    fun getPartnerAvatar(): String {
        val info = getAntherUser()
        if (info != null) {
            if (enableNoLimitDuration) {
                return info.avatar
            } else {
                return getMaskAvatar(info.sex)
            }
        } else {
            return ""
        }
    }

    fun getSelfAvatar(): String {
        val info = getMyUser()
        if (info != null) {
            if (enableNoLimitDuration) {
                return MyUserInfoManager.getInstance().avatar
            } else {
                return getMaskAvatar(info.sex)
            }
        } else {
            return ""
        }
    }

    fun getUserHasLockById(id: Int): Boolean {
        val localUserLockInfo = userLockInfoMap[id]
        if (localUserLockInfo == null || localUserLockInfo.isHasLock) {
            return true
        }

        return false
    }

    fun getMyUser(): UserInfoModel? {
        val map = userInfoListMap
        if (map != null) {
            for (info in map) {
                if (info.key.toLong() == MyUserInfoManager.getInstance().uid) {
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

    /**
     * 由于现在房间里可以邀请别人，prepare状态是两个人都到齐算准备完成了
     */
    fun isRoomPrepared(): Boolean {
        if (doubleRoomOri == DoubleRoomOri.CREATE) {
            return userInfoListMap?.size == 2
        } else {
            return true
        }
    }

    /**
     * 是不是创建的房间，通过首页邀请进来
     */
    fun isCreateRoom(): Boolean {
        return doubleRoomOri == DoubleRoomOri.CREATE
    }

    /**
     * 是不是创建的房间，通过首页邀请进来
     */
    fun isGrabInviteRoom(): Boolean {
        return doubleRoomOri == DoubleRoomOri.GRAB_INVITE
    }

    /**
     * 是不是创建的房间，通过首页邀请进来
     */
    fun isMatchRoom(): Boolean {
        return doubleRoomOri == DoubleRoomOri.MATCH
    }

    override fun toString(): String {
        return "DoubleRoomData(Tag='$Tag', gameId=$gameId, doubleGameState=$doubleGameState, syncStatusTimeMs=$syncStatusTimeMs, passedTimeMs=$passedTimeMs, userLockInfoMap=$userLockInfoMap, userInfoListMap=$userInfoListMap, enableNoLimitDuration=$enableNoLimitDuration, localCombineRoomMusic=$localCombineRoomMusic, nextMusicDesc=$nextMusicDesc, config=$config, tokens=$tokens, needMaskUserInfo=$needMaskUserInfo)"
    }

    //未开始，已开始，已结束
    enum class DoubleGameState constructor(private val status: Int?) {
        START(1), END(2);

        val value: Int
            get() = status!!
    }


    /**
     * 这个房间从哪里来，可以从匹配，一场到底邀请，创建房间邀请来
     */
    enum class DoubleRoomOri constructor(private val status: Int?) {
        GRAB_INVITE(0), MATCH(1), CREATE(2);

        val value: Int
            get() = status!!
    }

    companion object {
        fun makeRoomDataFromJsonObject(obj: JSONObject): DoubleRoomData {
            val doubleRoomData = DoubleRoomData()
            doubleRoomData.gameId = obj.getIntValue("roomID")
            doubleRoomData.passedTimeMs = obj.getLongValue("passedTimeMs")
            doubleRoomData.config = JSON.parseObject(obj.getString("config"), LocalCombineRoomConfig::class.java)
            val userList = JSON.parseArray(obj.getString("users"), UserInfoModel::class.java)
            doubleRoomData.enableNoLimitDuration = doubleRoomData.config?.durationTimeMs == -1

            if (userList != null) {
                val hashMap = HashMap<Int, UserInfoModel>()
                for (userInfoModel in userList) {
                    hashMap.put(userInfoModel.userId, userInfoModel)
                }
                doubleRoomData.userInfoListMap = hashMap
            }

            doubleRoomData.tokens = JSON.parseArray(obj.getString("tokens"), LocalAgoraTokenInfo::class.java)
            doubleRoomData.needMaskUserInfo = obj.getBooleanValue("needMaskUserInfo")
            return doubleRoomData
        }
    }
}
