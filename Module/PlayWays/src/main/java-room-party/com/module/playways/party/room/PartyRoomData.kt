package com.module.playways.party.room

import com.common.log.MyLog
import com.component.busilib.constans.GameModeType
import com.module.playways.BaseRoomData
import com.module.playways.party.match.model.JoinPartyRoomRspModel
import com.module.playways.party.room.event.PartyRoundChangeEvent
import com.module.playways.party.room.model.PartyActorInfoModel
import com.module.playways.party.room.model.PartyPlayerInfoModel
import com.module.playways.party.room.model.PartyRoundInfoModel
import com.module.playways.party.room.model.PartySeatInfoModel
import com.module.playways.room.prepare.model.PlayerInfoModel
import org.greenrobot.eventbus.EventBus


class PartyRoomData : BaseRoomData<PartyRoundInfoModel>() {
    var roomName = ""//房间名称
    var topicName = ""//房间主题
    var notice = ""// 房间公告
    var syncStatusTimeMs = 0L
    var onlineUserCnt = 0 //在线人数
    var applyUserCnt = 0 //申请人数
    var users = ArrayList<PartyPlayerInfoModel>() // 当前的用户信息 包括 主持人管理员 以及 嘉宾
    var seats = ArrayList<PartySeatInfoModel>() // 座位信息

    //  var seatsMap = HashMap<Int, PartyActorInfoModel>() // 座位信息 key为座位序号  value 的座位状态 和该位置上的嘉宾信息

    // 题目信息在轮次信息里 轮次信息在父类的 realRoundInfo 中

    companion object {
    }

    override fun getPlayerAndWaiterInfoList(): List<PlayerInfoModel> {
        return users
    }

    override fun getInSeatPlayerInfoList(): List<PlayerInfoModel> {
        return users
    }

    override val gameType: Int
        get() = GameModeType.GAME_MODE_PARTY


    init {

    }

    fun getPlayerInfoById(userId: Int): PartyPlayerInfoModel? {
        for (info in getPlayerAndWaiterInfoList()) {
            if (info.userID == userId) {
                return info as PartyPlayerInfoModel
            }
        }
        return null
    }

    /**
     *  座位信息 key为座位序号  value 的座位状态 和该位置上的嘉宾信息
     */
    fun getSeatInfoMap(): HashMap<Int, PartyActorInfoModel> {
        var seatsMap = HashMap<Int, PartyActorInfoModel>()
        this.seats?.let {
            for (info in it) {
                var partyActorInfoModel = PartyActorInfoModel()
                partyActorInfoModel.seat = info
                if (info.userID > 0) {
                    partyActorInfoModel.player = getPlayerInfoById(info.userID)
                }
                seatsMap[info.seatSeq] = partyActorInfoModel
            }
        }
        return seatsMap;
    }

    /**
     * 检查轮次信息是否需要更新
     */
    override fun checkRoundInEachMode() {
        if (isIsGameFinish) {
            MyLog.d(TAG, "游戏结束了，不需要再checkRoundInEachMode")
            return
        }
        if (expectRoundInfo == null) {
            MyLog.d(TAG, "尝试切换轮次 checkRoundInEachMode mExpectRoundInfo == null")
            // 结束状态了
            if (realRoundInfo != null) {
                val lastRoundInfoModel = realRoundInfo
//                lastRoundInfoModel?.updateStatus(false, ERRoundStatus.RRS_END.value)
                realRoundInfo = null
                EventBus.getDefault().post(PartyRoundChangeEvent(lastRoundInfoModel, null))
            }
            return
        }
        MyLog.d(TAG, "尝试切换轮次 checkRoundInEachMode mExpectRoundInfo.roundSeq=" + expectRoundInfo!!.roundSeq)
        if ((expectRoundInfo?.roundSeq ?: 0) > (realRoundInfo?.roundSeq
                        ?: 0) || realRoundInfo == null) {
            // 轮次大于，才切换
            val lastRoundInfoModel = realRoundInfo
//            lastRoundInfoModel?.updateStatus(false, ERRoundStatus.RRS_END.value)
            realRoundInfo = expectRoundInfo
            EventBus.getDefault().post(PartyRoundChangeEvent(lastRoundInfoModel, realRoundInfo))
        }
    }

    fun loadFromRsp(rsp: JoinPartyRoomRspModel) {
        this.gameId = rsp.roomID
        this.agoraToken = rsp.agoraToken
        this.applyUserCnt = rsp.applyUserCnt ?: 0
        this.gameStartTs = (rsp.gameStartTimeMs?.toLong() ?: 0L)
        this.notice = rsp.notice ?: ""
        this.onlineUserCnt = rsp.onlineUserCnt ?: 0
        this.roomName = rsp.roomName ?: ""
        this.topicName = rsp.topicName ?: ""
        this.users = rsp.users ?: ArrayList()
        this.expectRoundInfo = rsp.currentRound
        this.seats = rsp.seats ?: ArrayList()
    }


}
