package com.module.playways.party.room.model

import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import com.zq.live.proto.PartyRoom.QuickAnswerInfo

class PartyQuickAnswerResult : Serializable {
    @JSONField(name = "seq")
    var seq: Int = 0
    @JSONField(name = "user")
    var user: PartyPlayerInfoModel? = null

    companion object {
        fun parseFromPb(pb: QuickAnswerInfo): PartyQuickAnswerResult {
            val model = PartyQuickAnswerResult()
            model.seq = pb.seq
            model.user = PartyPlayerInfoModel.parseFromPb(pb.user)
            return model
        }
    }
}