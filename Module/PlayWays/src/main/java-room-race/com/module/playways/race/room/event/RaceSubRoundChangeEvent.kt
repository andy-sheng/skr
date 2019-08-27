package com.module.playways.race.room.event

import com.module.playways.race.room.model.RaceRoundInfoModel

class RaceSubRoundChangeEvent(val thisRound:RaceRoundInfoModel?,val lastSubRoundSeq:Int) {

}