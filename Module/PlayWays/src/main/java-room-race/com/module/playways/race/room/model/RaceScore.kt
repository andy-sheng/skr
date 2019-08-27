package com.module.playways.race.room.model

import com.module.playways.race.room.event.RaceScoreChangeEvent
import com.zq.live.proto.RaceRoom.RoundScoreInfo
import org.greenrobot.eventbus.EventBus

class RaceScore {

    var bLightCnt = 0
    var isEscape = false
    var winType = 0

    fun addBLightUser(notify: Boolean, userID: Int, bLightCnt: Int) {
        if (this.bLightCnt < bLightCnt) {
            this.bLightCnt = bLightCnt
            if (notify) {
                EventBus.getDefault().post(RaceScoreChangeEvent())
            }
        }
    }
}

internal fun parseFromRoundScoreInfoPB(pb: RoundScoreInfo): RaceScore {
    val model = RaceScore()
    model.bLightCnt = pb.bLightCnt
    model.isEscape = pb.isEscape
    model.winType = pb.winType.value
    return model
}