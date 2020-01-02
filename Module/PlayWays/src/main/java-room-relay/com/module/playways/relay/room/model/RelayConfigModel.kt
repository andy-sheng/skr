package com.module.playways.relay.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.zq.live.proto.Common.RelayRoomConfig

import java.io.Serializable

class RelayConfigModel : Serializable {
    @JSONField(name = "durationTimeMs")
    var durationTimeMs = 0
    @JSONField(name = "unLockWaitTimeMs")
    var unLockWaitTimeMs = 0

    companion object {

        fun parseFromPB(config: RelayRoomConfig): RelayConfigModel {
            val model = RelayConfigModel()
            model.durationTimeMs = config.durationTimeMs
            model.unLockWaitTimeMs = config.unLockWaitTimeMs
            return model
        }
    }
}
