package com.common.core.userinfo.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class VipInfo : Serializable {
    @JSONField(name = "vipType")
    var vipType: Int = 0
    @JSONField(name = "desc")
    var vipDesc: String = ""

    override fun toString(): String {
        return "VipInfo(vipType=$vipType, vipDesc='$vipDesc')"
    }

}
