package com.module.playways.race.room.event

import com.module.playways.race.room.model.RaceRoundInfoModel

class RaceRoundChangeEvent(val lastRound:RaceRoundInfoModel?,val thisRound:RaceRoundInfoModel?) {
    override fun toString(): String {
        return "RaceRoundChangeEvent(lastRound=$lastRound, thisRound=$thisRound)"
    }
}