package com.module.club.rank

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import java.io.Serializable

class ClubRankModel : Serializable {
    @JSONField(name = "rankSeq")
    var rankSeq: Int = 0
    @JSONField(name = "user")
    var userInfoModel: UserInfoModel? = null
    @JSONField(name = "value")
    var value: Int = 0
}

class ClubTagModel : Serializable {
    @JSONField(name = "rType")
    var type: Int = 0
    @JSONField(name = "tabDesc")
    var tabDesc: String = ""
}