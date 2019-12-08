package com.module.playways.party.room.model

import java.io.Serializable

// 嘉宾席
class PartyActorInfoModel : Serializable {

    var player: PartyPlayerInfoModel? = null
    var seat: PartySeatInfoModel? = null

}