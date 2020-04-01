package com.module.playways.party.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.zq.live.proto.Common.PBeginDiamondbox
import java.io.Serializable

class PBeginDiamondboxModel : Serializable{

    @JSONField(name = "beginTimeMs")
    var beginTimeMs: Long? = null
    @JSONField(name = "createTimeMs")
    var createTimeMs: Long? = null
    @JSONField(name = "diamondboxTag")
    var diamondboxTag: String? = null
    @JSONField(name = "endTimeMs")
    var endTimeMs: Long? = null
    @JSONField(name = "user")
    var user: POnlineInfoModel? = null
    @JSONField(name = "zsCnt")
    var zsCnt: Long? = null

    companion object{

        fun parseFromPB(pBeginDiamondbox: PBeginDiamondbox): PBeginDiamondboxModel{
            val pBeginDiamondboxModel = PBeginDiamondboxModel()
            pBeginDiamondboxModel.beginTimeMs = pBeginDiamondbox.beginTimeMs
            pBeginDiamondboxModel.createTimeMs = pBeginDiamondbox.createTimeMs
            pBeginDiamondboxModel.diamondboxTag = pBeginDiamondbox.diamondboxTag
            pBeginDiamondboxModel.endTimeMs = pBeginDiamondbox.endTimeMs
            pBeginDiamondboxModel.zsCnt = pBeginDiamondbox.zsCnt
            pBeginDiamondboxModel.user = POnlineInfoModel.parseFromPB(pBeginDiamondbox.user)
            return pBeginDiamondboxModel
        }
    }

    override fun toString(): String {
        return "PBeginDiamondboxModel(beginTimeMs=$beginTimeMs, createTimeMs=$createTimeMs, diamondboxTag=$diamondboxTag, endTimeMs=$endTimeMs, user=$user, zsCnt=$zsCnt)"
    }


}

