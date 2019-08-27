package com.module.playways.race.room.model

import com.common.core.userinfo.model.UserInfoModel
import com.module.playways.room.prepare.model.PlayerInfoModel
import com.zq.live.proto.RaceRoom.ROnlineInfo

class RacePlayerInfoModel : PlayerInfoModel() {
    var role = 0
}

internal fun parseFromROnlineInfoPB(pb: ROnlineInfo): RacePlayerInfoModel {
    val model = RacePlayerInfoModel()
    model.userInfo = UserInfoModel.parseFromPB(pb.user)
    model.userID = model.userInfo.userId
    model.isOnline = pb.isOnline
    model.role = pb.role.value
    return model
}
