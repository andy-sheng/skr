package com.common.core.userinfo.model

import com.alibaba.fastjson.annotation.JSONField
import com.zq.live.proto.Common.EHonorType
import java.io.Serializable

/**
 * 会员信息
 */
class HonorInfo : Serializable {
    @JSONField(name = "userID")
    var userID: Int = 0
    @JSONField(name = "startAt")
    var startAt: Long = 0L
    @JSONField(name = "endAt")
    var endAt: Long = 0L
    @JSONField(name = "honorType")
    var honorType: Int = 0
    @JSONField(name = "leftDays")
    var leftDays: Int = 0

    // 是否会员
    fun isHonor(): Boolean {
        if (honorType != EHT_NO_COMMON) {
            return true
        }
        return false
    }

    companion object {
        // honorType的类型
        const val EHT_NO_COMMON = 0  //不是会员
        const val EHT_COMMON = 1  //普通会员

        fun parseFromPB(userID: Int, honorInfo: com.zq.live.proto.Common.HonorInfo?): HonorInfo {
            val result = HonorInfo()
            if (honorInfo != null) {
                result.userID = userID
                result.honorType = honorInfo.honorType.value
            }
            return result
        }

        fun toHonorInfoPB(honorInfo: HonorInfo?): com.zq.live.proto.Common.HonorInfo? {
            return if (honorInfo != null) {
                com.zq.live.proto.Common.HonorInfo.Builder()
                        .setHonorType(EHonorType.fromValue(honorInfo.honorType))
                        .build()
            } else {
                null
            }

        }
    }

}