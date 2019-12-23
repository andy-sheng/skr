package com.module.club.apply

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import java.io.Serializable

class ClubApplyInfoModel : Serializable {
    @JSONField(name = "applyID")
    var applyID: Int = 0
    @JSONField(name = "applyTimeMs")
    var applyTimeMs: Long = 0L
    @JSONField(name = "status")
    var status: Int = 0
    @JSONField(name = "text")
    var text: String = ""
    @JSONField(name = "user")
    var user: UserInfoModel? = null
}