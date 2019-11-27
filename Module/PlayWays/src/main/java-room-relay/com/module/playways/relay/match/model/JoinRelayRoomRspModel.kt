package com.module.playways.relay.match.model

import com.common.core.userinfo.model.UserInfoModel
import com.module.playways.relay.room.model.RelayConfigModel
import com.module.playways.relay.room.model.RelayRoundInfoModel
import java.io.Serializable

class JoinRelayRoomRspModel : Serializable {
    var roomID: Int = 0// 房间id
    var createTimeMs: Long = 0// 房间创建时间，绝对时间
    var peerUser: UserInfoModel? = null
    var config: RelayConfigModel? = null
    var agoraToken: String? = null// 声网token
    var currentRound: RelayRoundInfoModel? = null // 目前轮次
    var leftSeat = true // 我的位置是否在左边 即数组的0位置是否是我

    override fun toString(): String {
        return "JoinRelayRoomRspModel(roomID=$roomID, createTimeMs=$createTimeMs, user=$peerUser, config=$config, agoraToken=$agoraToken, currentRound=$currentRound)"
    }

}