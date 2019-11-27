package com.module.playways.relay.match.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import com.module.playways.room.song.model.SongModel
import java.io.Serializable

class RelayRecommendRoomInfo : Serializable {

    @JSONField(name = "userInfo")
    var userInfo: UserInfoModel? = null
    @JSONField(name = "songModel")
    var songModel: SongModel? = null
}