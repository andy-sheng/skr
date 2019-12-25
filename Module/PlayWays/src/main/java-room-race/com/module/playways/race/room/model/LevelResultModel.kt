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
    @JSONField(name = "simpleSaveStates")
    var simpleSaveStates: List<SaveRankModel>? = null
    @JSONField(name = "vipSaveStates")
    var vipSaveStates: List<SaveRankModel>? = null
    @JSONField(name = "moneySaveState")
    var moneySaveState: SaveRankModel? = null  // 钻石保段状态

    // 最新状态
    fun getLastState(): ScoreStateModel? {
        states?.let {
            return it[it.size - 1]
        }
        return null
    }
}

class SaveRankModel : Serializable {
    companion object {
        const val ESRS_DISABLE = 1 //未启用
        const val ESRS_ENABLE = 2  //已启用
        const val ESRS_USED = 3    //已使用
    }

    @JSONField(name = "status")
    var status = 0  // 状态
    @JSONField(name = "curBar")
    var curBar = 0
    @JSONField(name = "maxBar")
    var maxBar = 0
    @JSONField(name = "zsAmount")
    var zsAmount = 0
}