package com.common.core.userinfo.model

import com.alibaba.fastjson.annotation.JSONField
import com.zq.live.proto.Common.EVIPType
import com.zq.live.proto.Common.VipInfo
import java.io.Serializable

class VerifyInfo : Serializable {
    @JSONField(name = "vipType")
    var vipType: Int = 0
    @JSONField(name = "desc")
    var vipDesc: String = ""

    override fun toString(): String {
        return "VerifyInfo(vipType=$vipType, vipDesc='$vipDesc')"
    }

    companion object {
        fun parseFromPB(vipInfo: com.zq.live.proto.Common.VipInfo?): VerifyInfo {
            val result = VerifyInfo()
            if (vipInfo != null) {
                result.vipType = vipInfo.vipType.value
                result.vipDesc = vipInfo.desc
            }
            return result
        }

        fun toVipInfoPB(verifyInfo: VerifyInfo?): VipInfo? {
            return if (verifyInfo != null) {
                VipInfo.Builder()
                        .setVipType(EVIPType.fromValue(verifyInfo.vipType))
                        .setDesc(verifyInfo.vipDesc)
                        .build()
            } else {
                null
            }

        }
    }
}
