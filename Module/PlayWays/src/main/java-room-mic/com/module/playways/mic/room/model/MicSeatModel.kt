package com.module.playways.mic.room.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class MicSeatModel : Serializable {
    @JSONField(name = "music")
    var music: List<SeatMusicInfo> = ArrayList()
    @JSONField(name = "user")
    var user: MicPlayerInfoModel? = null

    class SeatMusicInfo : Serializable {
        @JSONField(name = "itemID")
        var itemID: Int? = null
        @JSONField(name = "itemName")
        var itemName: String? = null
    }
}