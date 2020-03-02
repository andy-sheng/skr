package com.module.playways.battle.room.model

import com.zq.live.proto.BattleRoom.BRoundResult
import java.io.Serializable

class BattleRoundResultModel : Serializable {


    companion object {
        fun parseFromPb(pb: BRoundResult): BattleRoundResultModel {
            var info = BattleRoundResultModel()
            return info
        }
    }
}
