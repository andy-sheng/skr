package com.module.playways.battle.songlist.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class BattleSongModel : Serializable {
    @JSONField(name = "hasSing")
    var hasSing: Boolean? = null
    @JSONField(name = "songName")
    var songName: String = ""
}