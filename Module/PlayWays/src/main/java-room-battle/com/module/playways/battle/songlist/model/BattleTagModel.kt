package com.module.playways.battle.songlist.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class BattleTagModel : Serializable {

    companion object {
        const val SST_LOCK = 1    //待开启
        const val SST_UNLOCK = 2  //已开启
    }

    @JSONField(name = "coverURL")
    var coverURL: String = ""
    @JSONField(name = "starCnt")
    var starCnt: Int = 0
    @JSONField(name = "status")
    var status: Int = 0
    @JSONField(name = "tagID")
    var tagID: Int = 0
    @JSONField(name = "tagName")
    var tagName: String = ""
}