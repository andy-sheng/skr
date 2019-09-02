package com.component.person.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class ScoreDetailModel : Serializable {
    @JSONField(name = "ranking")
    var scoreStateModel: ScoreStateModel? = null
    @JSONField(name = "raceTicketCnt")
    var raceTicketCnt : Long = 0L
    @JSONField(name = "standLightCnt")
    var standLightCnt : Long = 0L
}