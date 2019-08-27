package com.module.playways.race.room

import com.component.busilib.constans.GameModeType
import com.module.playways.BaseRoomData
import com.module.playways.race.match.model.JoinRaceRoomRspModel
import com.module.playways.race.room.model.RaceConfigModel
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.module.playways.room.prepare.model.PlayerInfoModel
import java.util.ArrayList

class RaceRoomData : BaseRoomData<RaceRoundInfoModel>() {

    var raceConfigModel:RaceConfigModel?=null
    var hasExitGame = false
    override fun getGameType(): Int {
        return GameModeType.GAME_MODE_RACE
    }

    override fun checkRoundInEachMode() {

    }

    override fun <T : PlayerInfoModel> getPlayerInfoList(): List<T>? {
        val l = ArrayList<T>()
        return l
    }

    fun  loadFromRsp(rsp: JoinRaceRoomRspModel){
        this.gameId = rsp.roomID
        this.raceConfigModel = rsp.config
        this.expectRoundInfo = rsp.currentRound
        this.setRealRoundInfo(null)
        this.isIsGameFinish = false
        this.hasExitGame = false
        //this.agoraToken = rsp.agoraToken
//        this.gameCreateTs = rsp.gameStartTimeMs
        this.gameStartTs = rsp.gameStartTimeMs
//        this.xxxx = rsp.newRoundBegin
    }
}
