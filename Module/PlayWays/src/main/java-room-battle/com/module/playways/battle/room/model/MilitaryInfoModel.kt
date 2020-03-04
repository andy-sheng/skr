package com.module.playways.battle.room.model

import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField


// 这一期只做展示，评价体系
class MilitaryInfoModel : Serializable {

    @JSONField(name = "currBar")
    var currBar: Int? = null
    @JSONField(name = "maxBar")
    var maxBar: Int? = null
    @JSONField(name = "nextTitle")
    var nextTitle: String? = null
    @JSONField(name = "title")
    var title: String? = null
    @JSONField(name = "titleIndex")
    var titleIndex: Int? = null
    @JSONField(name = "totalScore")
    var totalScore: Int? = null

}