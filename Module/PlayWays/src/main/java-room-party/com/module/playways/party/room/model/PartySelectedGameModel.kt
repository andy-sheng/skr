package com.module.playways.party.room.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class PartySelectedGameModel : Serializable {
    @JSONField(name = "desc")
    var desc: String = ""
    @JSONField(name = "name")
    var name: String = ""
    @JSONField(name = "sceneTag")
    var sceneTag: String = ""
}