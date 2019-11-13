package com.module.playways.race.room.model

import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.module.playways.room.prepare.model.PlayerInfoModel
import com.zq.live.proto.Common.ESex
import com.zq.live.proto.RaceRoom.ERUserRole
import com.zq.live.proto.RaceRoom.ROnlineInfo

class RacePlayerInfoModel : PlayerInfoModel() {
    var role = ERUserRole.ERUR_UNKNOWN.value
    var fakeUserInfo: FakeUserInfoModel? = null // 蒙面信息
        get() {
            if (userInfo.userId.toLong() == MyUserInfoManager.uid) {
                field?.avatarUrl = MyUserInfoManager.avatar
            } else {
                if (userInfo.sex == ESex.SX_MALE.value) {
                    field?.avatarUrl = FakeUserInfoModel.maleAvatar
                } else {
                    field?.avatarUrl = FakeUserInfoModel.femaleAvatarUrl
                }
            }
            return field
        }

    var isFake = false  // 是否蒙面

    override fun toString(): String {
        return "${userInfo.toSimpleString()} ${fakeUserInfo}"
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

internal fun parseFromROnlineInfoPB(pb: ROnlineInfo): RacePlayerInfoModel {
    val model = RacePlayerInfoModel()
    model.userInfo = UserInfoModel.parseFromPB(pb.userInfo)
    model.userID = model.userInfo.userId
    model.isOnline = pb.isOnline
    model.role = pb.role.value
    model.fakeUserInfo = FakeUserInfoModel.parseFromPB(pb.fakeUserInfo)
    return model
}
