package com.module.playways.party.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.zq.live.proto.PartyRoom.PunishInfo
import java.io.Serializable

class PartyPunishInfoModel : Serializable {
    @JSONField(name = "punishID")
    var punishID: Int = 0
    @JSONField(name = "punishDesc")
    var punishDesc: String = ""
    @JSONField(name = "punishType")
    var punishType: Int = 0

    companion object {
        fun toLocalModelFromPB(punishInfo: PunishInfo): PartyPunishInfoModel {
            return PartyPunishInfoModel().apply {
                punishID = punishInfo.punishID
                punishDesc = punishInfo.punishDesc
                punishType = punishInfo.punishType.value
            }
        }
    }
}
