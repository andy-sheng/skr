package com.module.playways.race.room.view.actor

import com.component.person.model.ScoreStateModel
import com.module.playways.race.room.model.RacePlayerInfoModel

class RaceActorInfoModel(var plyer: RacePlayerInfoModel) {
    //0表示未知状态，1表示演唱中，2表示等待中
    var scoreState : ScoreStateModel? = null
}