package com.module.playways.party.room.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class PartyConfigModel : Serializable {
    @JSONField(name = "durationTimeMs")
    var durationTimeMs = 0

    companion object {
    }
}
