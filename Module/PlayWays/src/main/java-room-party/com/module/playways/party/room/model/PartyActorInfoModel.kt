package com.module.playways.party.room.model

import java.io.Serializable

// 竟演者
class PartyActorInfoModel : Serializable {

    var player: PartyPlayerInfoModel? = null
    var seat: PartySeatInfoModel? = null

}