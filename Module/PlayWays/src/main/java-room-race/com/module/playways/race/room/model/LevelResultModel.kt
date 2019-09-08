package com.module.playways.race.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.ScoreStateModel
import java.io.Serializable

class LevelResultModel : Serializable {
    @JSONField(name = "gap")
    var gap: Int = 0
    @JSONField(name = "get")
    var get: Int = 0
    @JSONField(name = "userID")
    var userID: Int = 0
    @JSONField(name = "states")
    var states: List<ScoreStateModel>? = null


    // 最新状态
    fun getLastState(): ScoreStateModel? {
        states?.let {
            return it[it.size - 1]
        }
        return null
    }
}