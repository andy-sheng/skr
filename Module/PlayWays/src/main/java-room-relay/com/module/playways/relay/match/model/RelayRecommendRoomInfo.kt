package com.module.playways.relay.match.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import com.module.playways.room.song.model.SongModel
import java.io.Serializable

class RelayRecommendRoomInfo : Serializable {
    @JSONField(name = "user")
    var user: UserInfoModel? = null
    @JSONField(name = "item")
    var item: SongModel? = null
    @JSONField(name = "recommend")
    var recommendTag: RelayRecommendTagInfo? = null

    override fun toString(): String {
        return "RelayRecommendRoomInfo(user=$user, item=$item, recommendTag=$recommendTag)"
    }
}

class RelayRecommendTagInfo : Serializable {
    @JSONField(name = "URL")
    var url: String? = null
    @JSONField(name = "category")
    var category: Int = 0
}