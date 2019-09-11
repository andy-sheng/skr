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

    companion object {
        fun parseFromPB(vipInfo: com.zq.live.proto.Common.VipInfo?): VipInfo {
            val result = VipInfo()
            if (vipInfo != null) {
                result.vipType = vipInfo.vipType.value
                result.vipDesc = vipInfo.desc
            }
            return result
        }
    }
}
