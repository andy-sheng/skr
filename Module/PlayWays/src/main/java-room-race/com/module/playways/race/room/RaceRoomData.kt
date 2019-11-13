package com.module.playways.race.room

import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.U
import com.component.busilib.constans.GameModeType
import com.module.playways.BaseRoomData
import com.module.playways.RoomDataUtils
import com.module.playways.race.match.model.JoinRaceRoomRspModel
import com.module.playways.race.room.event.RaceRoundChangeEvent
import com.module.playways.race.room.model.FakeUserInfoModel
import com.module.playways.race.room.model.RacePlayerInfoModel
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.zq.live.proto.RaceRoom.ERUserRole
import com.zq.live.proto.RaceRoom.ERaceRoundStatus
import org.greenrobot.eventbus.EventBus

class RaceRoomData : BaseRoomData<RaceRoundInfoModel>() {

    override val gameType: Int
        get() = GameModeType.GAME_MODE_RACE
    var hasExitGame = false
    var isAccEnable = false
        // 是否开启伴奏,只代表设置里伴奏开关
        set(value) {
            field = value
            U.getPreferenceUtils().setSettingBoolean("grab_acc_enable1", value)
        }

    var runningRoundCount = 0 // 本人在这个房间里已经待了多少轮了
    var hasSignUpSelf: Boolean = false //自己是否报名
    var audience = false // 本人是否是观众

    init {
        isAccEnable = U.getPreferenceUtils().getSettingBoolean("grab_acc_enable1", false)
    }

    /**
     * 检查轮次是否要更新
     */
    override fun checkRoundInEachMode() {
        if (this.isIsGameFinish) {
            return
        }
        if (this.expectRoundInfo == null && this.realRoundInfo != null) {
            // 发送游戏结束
            // TODO 发送轮次切换事件
            val lastRound = this.realRoundInfo
            this.realRoundInfo = this.expectRoundInfo
            EventBus.getDefault().post(RaceRoundChangeEvent(lastRound, this.realRoundInfo))
            return
        }
        val larger = RoomDataUtils.roundSeqLarger(this.expectRoundInfo, this.realRoundInfo)
        if (larger || this.realRoundInfo == null) {
            // TODO
            val lastRound = this.realRoundInfo
//            (this.realRoundInfo as RaceRoundInfoModel).updateStatus(false,轮次结束事件)
            this.realRoundInfo = this.expectRoundInfo
//            (this.realRoundInfo as RaceRoundInfoModel).updateStatus(false,轮次开始事件)
            if (larger) {
                this.runningRoundCount++
            }
            // TODO 发送轮次切换事件
            EventBus.getDefault().post(RaceRoundChangeEvent(lastRound, this.realRoundInfo))
//            EventBus.getDefault().post(GrabRoundChangeEvent(lastRoundInfoModel, mRealRoundInfo as GrabRoundInfoModel))
        }
    }

    override fun getPlayerAndWaiterInfoList(): List<RacePlayerInfoModel> {
        val l = ArrayList<RacePlayerInfoModel>()
        if (realRoundInfo != null) {
            realRoundInfo?.let {
                l.addAll(it.playUsers)
                l.addAll(it.waitUsers)
            }
        } else {
            expectRoundInfo?.let {
                l.addAll(it.playUsers)
                l.addAll(it.waitUsers)
            }
        }
        return l
    }

    fun getPlayerCount(): Int {
        var count = 0
        if (realRoundInfo != null) {
            realRoundInfo?.let {
                count = it.playUsers.size
            }
        } else {
            expectRoundInfo?.let {
                count = it.playUsers.size
            }
        }

        return count
    }

    fun getPlayerOrWaiterInfoModel(userID: Int?): RacePlayerInfoModel? {
        if (userID == null || userID == 0) {
            return null
        }
//        val playerInfoModel = userInfoMap[userID] as RacePlayerInfoModel?
//        if (playerInfoModel == null || playerInfoModel.role == ERUserRole.ERUR_WAIT_USER.value) {
            val l = getPlayerAndWaiterInfoList()
            for (playerInfo in l) {
                if (playerInfo.userInfo.userId == userID) {
//                    userInfoMap.put(playerInfo.userInfo.userId, playerInfo)
                    return playerInfo as RacePlayerInfoModel?
                }
            }
//        } else {
//            return playerInfoModel
//        }
        return null
    }

    /**
     * 该用户是否蒙面 对我来说
     */
    fun isFakeForMe(uid: Int?): Boolean {
        if (uid == MyUserInfoManager.uid.toInt()) {
            return false
        }

        val m = getPlayerOrWaiterInfoModel(uid)
        return if (m != null) {
            realRoundInfo?.unfakeSetForMe?.contains(uid) != true
        } else {
            false
        }
    }

    // 拿到FakeUserInfoModel的信息
    fun getFakeInfo(uid: Int?): FakeUserInfoModel? {
        if (getPlayerOrWaiterInfoModel(uid)?.role != ERUserRole.ERUR_AUDIENCE.value) {
            return getPlayerOrWaiterInfoModel(uid)?.fakeUserInfo
        }
        return null
    }

    override fun getInSeatPlayerInfoList(): List<RacePlayerInfoModel> {
        return getPlayerAndWaiterInfoList()
    }

//    fun getChoiceInfoById(choiceID: Int): SongModel? {
//        return this.realRoundInfo?.games?.getOrNull(choiceID - 1)?.commonMusic
//    }

    fun loadFromRsp(rsp: JoinRaceRoomRspModel) {
        this.gameId = rsp.roomID
        this.expectRoundInfo = rsp.currentRound
//        rsp.games?.let {
//            this.expectRoundInfo?.games = it
//        }
//        rsp.couldChoiceGames?.let {
//            this.couldChoiceGames = it
//        }
        this.realRoundInfo = null
        this.isIsGameFinish = false
        this.hasExitGame = false
        this.agoraToken = rsp.agoraToken
        this.gameCreateTs = rsp.gameCreateTimeMs
        this.gameStartTs = rsp.gameStartTimeMs

        this.expectRoundInfo?.enterStatus = this.expectRoundInfo?.status
                ?: ERaceRoundStatus.ERRS_UNKNOWN.value
        this.expectRoundInfo?.enterSubRoundSeq = this.expectRoundInfo?.subRoundSeq ?: 0
        this.gameConfigMsg = rsp.config
        if (rsp.elapsedTimeMs > 0) {
            // 演唱轮次进来，不能是本局参与者
            //this.expectRoundInfo?.isParticipant = false
            this.expectRoundInfo?.elapsedTimeMs = rsp.elapsedTimeMs
        } else {
            //this.expectRoundInfo?.isParticipant = true
        }
        audience = rsp.audience
    }

}
