package com.module.playways.mic.room.model

import com.common.core.userinfo.model.UserInfoModel
import com.module.playways.room.prepare.model.PlayerInfoModel
import com.zq.live.proto.MicRoom.EMUserRole
import com.zq.live.proto.MicRoom.MOnlineInfo

class MicPlayerInfoModel : PlayerInfoModel() {

    /**
     * 用户角色
     */
    var role: Int = EMUserRole.MRUR_UNKNOWN.value

    /**
     * 当前在唱
     */
    var isCurSing: Boolean = false

    /**
     * 下首在唱
     */
    var isNextSing: Boolean = false

    override fun toString(): String {
        return "${userInfo.toSimpleString()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as MicPlayerInfoModel
//
        if (role != other.role) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
//        result = 31 * result + role
        return result
    }

}

internal fun parseFromROnlineInfoPB(pb: MOnlineInfo): MicPlayerInfoModel {
    val model = MicPlayerInfoModel()
    model.userInfo = UserInfoModel.parseFromPB(pb.userInfo)
    model.userID = model.userInfo.userId
    model.isOnline = pb.isOnline
    model.role = pb.role.value
    model.isCurSing = pb.isCurSing
    model.isNextSing = pb.isNextSing
    return model
}
