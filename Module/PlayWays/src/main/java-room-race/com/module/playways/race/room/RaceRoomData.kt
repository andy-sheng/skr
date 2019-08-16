package com.module.playways.race.room

import com.module.playways.BaseRoomData
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.module.playways.room.prepare.model.PlayerInfoModel
import java.util.ArrayList

class RaceRoomData : BaseRoomData<RaceRoundInfoModel>() {

    override fun getGameType(): Int {
        return 0
    }

    override fun checkRoundInEachMode() {

    }

    override fun <T : PlayerInfoModel> getPlayerInfoList(): List<T>? {
        val l = ArrayList<T>()
        return l
    }
}
