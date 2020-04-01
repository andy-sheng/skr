package com.module.playways.party.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.zq.live.proto.broadcast.PartyDiamondbox
import java.io.Serializable

class PartyDiamondboxModel : Serializable{

    @JSONField(name = "roomID")
    var roomID:Int? = null
    @JSONField(name = "pBeginDiamondbox")
    var pBeginDiamondbox:PBeginDiamondboxModel? = null
    companion object{

        fun parseFromPB(partyDiamondbox: PartyDiamondbox):PartyDiamondboxModel{
            val partyDiamondboxModel = PartyDiamondboxModel()
            partyDiamondboxModel.roomID = partyDiamondbox.roomID
            partyDiamondboxModel.pBeginDiamondbox = PBeginDiamondboxModel.parseFromPB(partyDiamondbox.pBeginDiamondbox)
            return partyDiamondboxModel
        }

    }
}