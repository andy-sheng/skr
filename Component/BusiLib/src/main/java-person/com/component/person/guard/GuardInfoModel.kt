package com.component.person.guard

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import java.io.Serializable

class GuardInfoModel : Serializable {
    @JSONField(name = "userInfo")
    var userInfoModel: UserInfoModel? = null
    @JSONField(name = "expireTimeMs")
    var expireTimeMs: Long = 0L
}

class GuardTimeInfo : Serializable {
    @JSONField(name = "expireTimeMs")
    var expireTimeMs: Long = 0L
    @JSONField(name = "nowTime")
    var nowTime: Long = 0L
    @JSONField(name = "userID")
    var userID: Int = 0
}