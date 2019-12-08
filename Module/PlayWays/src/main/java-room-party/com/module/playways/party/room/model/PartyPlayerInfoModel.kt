package com.module.playways.party.room.model

import com.module.playways.room.prepare.model.PlayerInfoModel

class PartyPlayerInfoModel : PlayerInfoModel() {
    var role = ArrayList<Integer>() // 角色
    var popularity = 0 // 人气
    override fun toString(): String {
        return "PartyPlayerInfoModel(userInfo=${userInfo.toSimpleString()}, role=$role)"
    }


}
