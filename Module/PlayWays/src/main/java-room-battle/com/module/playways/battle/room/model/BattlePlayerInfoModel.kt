package com.module.playways.battle.room.model

import com.module.playways.room.prepare.model.PlayerInfoModel
import com.zq.live.proto.PartyRoom.POnlineInfo

class BattlePlayerInfoModel : PlayerInfoModel() {

    companion object {
        fun parseFromPb(pb: POnlineInfo): BattlePlayerInfoModel {
            var info = BattlePlayerInfoModel()
            return info
        }
    }
}
