package com.module.playways.mic.room.model

import com.common.core.userinfo.model.UserInfoModel
import com.module.playways.room.prepare.model.PlayerInfoModel
import com.zq.live.proto.MicRoom.EMUserRole
import com.zq.live.proto.MicRoom.MOnlineInfo

class MicPlayerInfoModel : PlayerInfoModel() {

    var role = EMUserRole.MRUR_UNKNOWN.value

    override fun toString(): String {
        return "${userInfo.toSimpleString()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

//        other as RacePlayerInfoModel
//
//        if (role != other.role) return false

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
    return model
}
