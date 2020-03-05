package com.module.playways.battle.match.model

import com.alibaba.fastjson.annotation.JSONField
import com.component.busilib.model.GameBackgroundEffectModel
import com.module.playways.battle.room.model.BattleRoomConfig
import com.module.playways.battle.room.model.BattleRoundInfoModel
import com.module.playways.battle.room.model.BattleTeamInfoModel
import com.module.playways.doubleplay.pbLocalModel.LocalAgoraTokenInfo
import com.zq.live.proto.BattleRoom.BUserEnterMsg
import java.io.Serializable

class JoinBattleRoomRspModel : Serializable {
    @JSONField(name = "config")
    var config: BattleRoomConfig? = null
    @JSONField(name = "createdTimeMs")
    var createdTimeMs = 0L
    @JSONField(name = "currentRound")
    var currentRound: BattleRoundInfoModel? = null
    @JSONField(name = "roomID")
    var roomID = 0
    @JSONField(name = "showInfos")
    var showInfos = ArrayList<GameBackgroundEffectModel>()
    @JSONField(name = "teams")
    var teams = ArrayList<BattleTeamInfoModel>()
    @JSONField(name = "tokens")
    var tokens: List<LocalAgoraTokenInfo>? = null    // 声网

    companion object {
        fun parseFromPB(msg: BUserEnterMsg): JoinBattleRoomRspModel {
            val result = JoinBattleRoomRspModel()
            result.config = BattleRoomConfig.parseFromPB(msg.config)
            result.createdTimeMs = msg.createdTimeMs
            result.currentRound = BattleRoundInfoModel.parseFromRoundInfo(msg.currentRound)
            result.roomID = msg.roomID
            result.showInfos.addAll(GameBackgroundEffectModel.parseToList(msg.showInfosList))
            result.teams = BattleTeamInfoModel.parseToList(msg.teamsList)
            result.tokens = LocalAgoraTokenInfo.toLocalAgoraTokenInfo(msg.tokensList)
            return result
        }
    }
}
