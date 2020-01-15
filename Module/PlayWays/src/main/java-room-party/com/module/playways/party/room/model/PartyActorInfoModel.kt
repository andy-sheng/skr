package com.module.playways.party.room.model

import java.io.Serializable

// 嘉宾席
class PartyActorInfoModel : Serializable {
    var player: PartyPlayerInfoModel? = null
    var seat: PartySeatInfoModel? = null  // 座位信息应该不存在为空的状态
}

class QuickAnswerUiModel{
    var seq = 1
    var durationTime = 5000
}