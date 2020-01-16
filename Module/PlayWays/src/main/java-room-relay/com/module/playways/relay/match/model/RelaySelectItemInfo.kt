package com.module.playways.relay.match.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import com.component.busilib.friends.VoiceInfoModel
import com.module.playways.room.song.model.SongModel
import java.io.Serializable

class RelaySelectItemInfo : Serializable {

    companion object {
        const val ST_MATCH_ITEM = 1   // 匹配可选择的条目
        const val ST_REDPACKET_ITEM = 2  // 开启红包合唱铃的条目
    }

    @JSONField(name = "matchItem")
    var matchItem: RelayMatchItemInfo? = null
    @JSONField(name = "redpacketItem")
    var redpacketItem: RelayRedPacketItemInfo? = null
    @JSONField(name = "type")
    var type: Int = 0    // 1 : 匹配可选择的条目  2 : 开启红包合唱铃的条目
}

class RelayRedPacketItemInfo : Serializable {
    @JSONField(name = "costZS")
    var costZS: Int = 0
    @JSONField(name = "user")
    var user: UserInfoModel? = null
    @JSONField(name = "voiceInfo")
    var voiceInfo: VoiceInfoModel? = null

    override fun toString(): String {
        return "RelayRedPacketItemInfo(costZS=$costZS, user=$user, voiceInfo=$voiceInfo)"
    }
}

class RelayMatchItemInfo : Serializable {
    @JSONField(name = "user")
    var user: UserInfoModel? = null
    @JSONField(name = "item")
    var item: SongModel? = null
    @JSONField(name = "recommend")
    var recommendTag: RelayRecommendTagInfo? = null

    override fun toString(): String {
        return "RelayMatchRoomInfo(user=$user, item=$item, recommendTag=$recommendTag)"
    }
}

class RelayRecommendTagInfo : Serializable {
    @JSONField(name = "URL")
    var url: String? = null
    @JSONField(name = "category")
    var category: Int = 0
}