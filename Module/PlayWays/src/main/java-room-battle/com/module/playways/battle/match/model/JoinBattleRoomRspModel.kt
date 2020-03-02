package com.module.playways.battle.match.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.ClubInfo
import com.module.playways.battle.room.model.BattleRoomConfig
import com.module.playways.battle.room.model.BattleRoundInfoModel
import com.module.playways.battle.room.model.BattleTeamInfoModel
import com.module.playways.party.room.model.PartyConfigModel
import com.module.playways.party.room.model.PartyPlayerInfoModel
import com.module.playways.party.room.model.PartyRoundInfoModel
import com.module.playways.party.room.model.PartySeatInfoModel
import java.io.Serializable

class JoinBattleRoomRspModel : Serializable {

    var roomID = 0

    var createdTimeMs = 0L

    var teams = ArrayList<BattleTeamInfoModel>()

    var config = BattleRoomConfig()

    var agoraToken: String? = null

    var currentRound: BattleRoundInfoModel? = null

    //repeated Common.BackgroundShowInfo showInfos = 7; //背景效果
}