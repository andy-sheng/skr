package com.common.core.userinfo.model

import com.alibaba.fastjson.annotation.JSONField
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
    @JSONField(name = "status")
    var status: Int = 0
    @JSONField(name = "honorType")
    var honorType: Int = 0
    @JSONField(name = "leftDays")
    var leftDays: Int = 0

    // 是否会员
    fun isHonor(): Boolean {
        if (status == EHS_HAS_ACTIVE && honorType != EHT_NO_COMMON) {
            //开通会员服务，且是会员
            return true
        }
        return false
    }

    companion object {
        // honorType的类型
        const val EHT_NO_COMMON = 0  //不是会员
        const val EHT_COMMON = 1  //普通会员

        // status的值
        const val EHS_UN_ACTIVE = 1   //未开通会员服务
        const val EHS_HAS_ACTIVE = 2   //已开通会员

        fun parseFromPB(userID: Int, honorInfo: com.zq.live.proto.Common.HonorInfo?): HonorInfo {
            val result = HonorInfo()
            if (honorInfo != null) {
                result.userID = userID
                result.honorType = honorInfo.honorType.value
            }
            return result
        }
    }

}