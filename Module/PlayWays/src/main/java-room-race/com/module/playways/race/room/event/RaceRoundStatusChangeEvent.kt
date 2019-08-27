package com.module.playways.race.room.event

import com.module.playways.race.room.model.RaceRoundInfoModel

class RaceRoundStatusChangeEvent(val thisRound:RaceRoundInfoModel?,val oldStatus:Int) {

}