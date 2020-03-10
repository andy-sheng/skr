package com.module.playways.party.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import java.io.Serializable

class PartyRankModel : Serializable {
    @JSONField(name = "cnt")
    var cnt: Int = 0
    @JSONField(name = "rankSeq")
    var rankSeq: Int = 0
    @JSONField(name = "user")
    var model: UserInfoModel? = null
}