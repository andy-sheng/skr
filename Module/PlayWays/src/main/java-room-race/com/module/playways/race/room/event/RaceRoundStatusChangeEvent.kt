package com.module.playways.race.room.event

import com.module.playways.race.room.model.RaceRoundInfoModel

class RaceRoundStatusChangeEvent(val thisRound:RaceRoundInfoModel?,val oldStatus:Int) {
    override fun toString(): String {
        return "RaceRoundStatusChangeEvent(thisRound=$thisRound, oldStatus=$oldStatus)"
    }
}