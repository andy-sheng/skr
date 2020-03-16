package com.module.playways.battle.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.module.playways.room.prepare.model.PlayerInfoModel
import com.zq.live.proto.BattleRoom.BUserStatus
import com.zq.live.proto.PartyRoom.POnlineInfo
import java.io.Serializable

class BattlePlayerInfoModel : PlayerInfoModel() {

    companion object {
        fun parseFromPb(pb: POnlineInfo): BattlePlayerInfoModel {
            var info = BattlePlayerInfoModel()
            return info
        }
    }
}


class BattleUserStatus : Serializable {

    companion object {
        fun parseFromPb(pb: BUserStatus): BattleUserStatus {
            var model = BattleUserStatus()
            model.userID = pb.userID
            model.teamTag = pb.teamTag
            model.status = pb.status.value
            return model
        }
    }

    @JSONField(name = "userID")
    var userID: Int = 0
    @JSONField(name = "teamTag")
    var teamTag: String = ""
    @JSONField(name = "status")
    var status: Int = 0

}
