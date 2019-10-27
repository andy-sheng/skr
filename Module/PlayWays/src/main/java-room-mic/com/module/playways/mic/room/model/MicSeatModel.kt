package com.module.playways.mic.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import java.io.Serializable

class MicSeatModel : Serializable {
    @JSONField(name = "music")
    var music: List<SeatMusicInfo>? = null
    @JSONField(name = "user")
    var user: UserInfoModel? = null

    data class SeatMusicInfo(val itemID: Int, val itemName: String) : Serializable
}