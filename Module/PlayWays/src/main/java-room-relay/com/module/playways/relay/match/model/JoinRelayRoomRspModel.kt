package com.module.playways.relay.match.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import com.module.playways.doubleplay.pbLocalModel.LocalAgoraTokenInfo
import com.module.playways.relay.room.model.RelayConfigModel
import com.module.playways.relay.room.model.RelayRoundInfoModel
import com.module.playways.relay.room.model.RelayUserLockModel
import com.zq.live.proto.GrabRoom.AgoraTokenInfo
import com.zq.live.proto.RelayRoom.RUserEnterMsg
import java.io.Serializable

class JoinRelayRoomRspModel : Serializable {

    @JSONField(name = "roomID")
    var roomID: Int = 0// 房间id
    @JSONField(name = "createTimeMs")
    var createTimeMs: Long = 0// 房间创建时间，绝对时间
    @JSONField(name = "config")
    var config: RelayConfigModel? = null
    @JSONField(name = "currentRound")
    var currentRound: RelayRoundInfoModel? = null // 目前轮次
    var leftSeat = true // 我的位置是否在左边 即数组的0位置是否是我
    @JSONField(name = "enableNoLimitDuration")
    var enableNoLimitDuration: Boolean? = null    // 开启没有限制的持续时间
    @JSONField(name = "tokens")
    var tokens: List<LocalAgoraTokenInfo>? = null    // 声网
    @JSONField(name = "userLockInfo")
    var userLockInfo: List<RelayUserLockModel>? = null  // 解锁信息
    @JSONField(name = "users")
    var users: List<UserInfoModel>? = null  // 用户信息

    override fun toString(): String {
        return "JoinRelayRoomRspModel(roomID=$roomID, createTimeMs=$createTimeMs, currentRound=$currentRound, enableNoLimitDuration=$enableNoLimitDuration, tokens=$tokens, userLockInfo=$userLockInfo, users=$users)"
    }

    companion object {
        fun parseFromPB(msg: RUserEnterMsg): JoinRelayRoomRspModel {
            val result = JoinRelayRoomRspModel()
            result.roomID = msg.roomID
            result.createTimeMs = msg.createdTimeMs
            result.config = RelayConfigModel.parseFromPB(msg.config)
            result.currentRound = RelayRoundInfoModel.parseFromRoundInfo(msg.currentRound)
            result.enableNoLimitDuration = msg.enableNoLimitDuration
            result.tokens = LocalAgoraTokenInfo.toLocalAgoraTokenInfo(msg.tokensList)
            result.users = UserInfoModel.parseFromPB(msg.usersList)
            return result
        }
    }

}