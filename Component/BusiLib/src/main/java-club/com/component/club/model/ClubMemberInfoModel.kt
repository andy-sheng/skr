package com.component.club.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import java.io.Serializable

class ClubMemberInfoModel : Serializable {
    @JSONField(name = "onlineDesc")
    var onlineDesc: String = ""
    @JSONField(name = "user")
    var userInfoModel: UserInfoModel? = null
}