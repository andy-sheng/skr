package com.module.playways.relay.room

import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.component.busilib.constans.GameModeType
import com.module.playways.BaseRoomData
import com.module.playways.RoomDataUtils
import com.module.playways.relay.match.model.JoinRelayRoomRspModel
import com.module.playways.relay.room.event.RelayRoundChangeEvent
import com.module.playways.relay.room.model.RelayConfigModel
import com.module.playways.relay.room.model.RelayRoundInfoModel
import com.module.playways.relay.room.model.ReplayPlayerInfoModel
import com.module.playways.room.prepare.model.PlayerInfoModel
import com.zq.live.proto.RelayRoom.ERRoundStatus
import org.greenrobot.eventbus.EventBus


class RelayRoomData : BaseRoomData<RelayRoundInfoModel>() {
    override fun getPlayerAndWaiterInfoList(): List<PlayerInfoModel> {
        return null!!
    }

    override fun getInSeatPlayerInfoList(): List<PlayerInfoModel> {
        return null!!
    }

    var configModel = RelayConfigModel()// 一唱到底配置
    var peerUser: ReplayPlayerInfoModel? = null

    var unLockMe = false // 我是否解锁
    var unLockPeer = false // 对方是否解锁

    var isHasExitGame = false

    override val gameType: Int
        get() = GameModeType.GAME_MODE_RELAY

    fun hasTimeLimit(): Boolean {
        return unLockMe && unLockPeer
    }

    init {
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
                lastRoundInfoModel?.updateStatus(false, ERRoundStatus.RRS_END.value)
                realRoundInfo = null
                EventBus.getDefault().post(RelayRoundChangeEvent(lastRoundInfoModel, null))
            }
            return
        }
        MyLog.d(TAG, "尝试切换轮次 checkRoundInEachMode mExpectRoundInfo.roundSeq=" + expectRoundInfo!!.roundSeq)
        if (RoomDataUtils.roundSeqLarger<RelayRoundInfoModel>(expectRoundInfo, realRoundInfo) || realRoundInfo == null) {
            // 轮次大于，才切换
            val lastRoundInfoModel = realRoundInfo
            lastRoundInfoModel?.updateStatus(false, ERRoundStatus.RRS_END.value)
            realRoundInfo = expectRoundInfo
            EventBus.getDefault().post(RelayRoundChangeEvent(lastRoundInfoModel, realRoundInfo))
        }
    }

    fun loadFromRsp(rsp: JoinRelayRoomRspModel) {
        this.gameId = rsp.roomID
        this.gameCreateTs = rsp.createTimeMs
        this.agoraToken = rsp.agoraToken
        this.configModel = rsp.config ?: RelayConfigModel()

        this.peerUser = ReplayPlayerInfoModel()
        this.peerUser?.userInfo = rsp.peerUser
        this.peerUser?.isOnline = true
    }


}
