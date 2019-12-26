package com.module.playways.party.room.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class PartyConfigModel : Serializable {
    @JSONField(name = "SyncPullIntevalTimeMs")
    var internalTs = 10*1000

}
