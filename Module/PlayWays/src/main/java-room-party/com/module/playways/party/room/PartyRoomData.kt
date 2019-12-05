package com.module.playways.party.room

import com.component.busilib.constans.GameModeType
import com.module.playways.BaseRoomData
import com.module.playways.party.match.model.JoinPartyRoomRspModel
import com.module.playways.party.room.model.PartyRoundInfoModel
import com.module.playways.room.prepare.model.PlayerInfoModel


class PartyRoomData : BaseRoomData<PartyRoundInfoModel>() {
    companion object {
    }

    override fun getPlayerAndWaiterInfoList(): List<PlayerInfoModel> {
        return null!!
    }

    override fun getInSeatPlayerInfoList(): List<PlayerInfoModel> {
        return null!!
    }

    override val gameType: Int
        get() = GameModeType.GAME_MODE_PARTY


    init {

    }

    /**
     * 检查轮次信息是否需要更新
     */
    override fun checkRoundInEachMode() {
//        if (isIsGameFinish) {
//            MyLog.d(TAG, "游戏结束了，不需要再checkRoundInEachMode")
//            return
//        }
//        if (expectRoundInfo == null) {
//            MyLog.d(TAG, "尝试切换轮次 checkRoundInEachMode mExpectRoundInfo == null")
//            // 结束状态了
//            if (realRoundInfo != null) {
//                val lastRoundInfoModel = realRoundInfo
//                lastRoundInfoModel?.updateStatus(false, ERRoundStatus.RRS_END.value)
//                realRoundInfo = null
//                EventBus.getDefault().post(RelayRoundChangeEvent(lastRoundInfoModel, null))
//            }
//            return
//        }
//        MyLog.d(TAG, "尝试切换轮次 checkRoundInEachMode mExpectRoundInfo.roundSeq=" + expectRoundInfo!!.roundSeq)
//        if (RoomDataUtils.roundSeqLarger<RelayRoundInfoModel>(expectRoundInfo, realRoundInfo) || realRoundInfo == null) {
//            // 轮次大于，才切换
//            val lastRoundInfoModel = realRoundInfo
//            lastRoundInfoModel?.updateStatus(false, ERRoundStatus.RRS_END.value)
//            realRoundInfo = expectRoundInfo
//            EventBus.getDefault().post(RelayRoundChangeEvent(lastRoundInfoModel, realRoundInfo))
//        }
    }

    fun loadFromRsp(rsp: JoinPartyRoomRspModel) {
//        this.gameId = rsp.roomID
//        this.gameCreateTs = rsp.createTimeMs
//        rsp.tokens?.forEach {
//            if (it.userID == MyUserInfoManager.uid.toInt()) {
//                this.agoraToken = it.token
//                return@forEach
//            }
//        }
//        this.configModel = rsp.config ?: RelayConfigModel()
//        this.peerUser = RelayPlayerInfoModel()
//        rsp.users?.forEachIndexed { index, userInfoModel ->
//            var a = rsp.showInfos.getOrNull(index)
//            if (userInfoModel.userId != MyUserInfoManager.uid.toInt()) {
//                this.peerUser?.userInfo = userInfoModel
//                this.peerEffectModel = a
//                this.leftSeat = index != 0
//            } else {
//                this.myEffectModel = a
//            }
//        }
//        this.peerUser?.isOnline = true
//        this.expectRoundInfo = rsp.currentRound
    }

//    override fun toString(): String {
//        return "RelayRoomData(shiftTsForRelay=$shiftTsForRelay, configModel=$configModel, peerUser=$peerUser, unLockMe=$unLockMe, unLockPeer=$unLockPeer, leftSeat=$leftSeat, isHasExitGame=$isHasExitGame)"
//    }


}
