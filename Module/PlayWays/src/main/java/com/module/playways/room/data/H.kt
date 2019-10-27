package com.module.playways.room.data

import com.component.busilib.constans.GameModeType
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.mic.room.MicRoomData
import com.module.playways.race.room.RaceRoomData

object H {
    fun reset() {
        /**
         * 当前数据类型
         */
        curType = GameModeType.GAME_MODE_UNKNOW
        micRoomData = null
        grabRoomData = null
        raceRoomData = null

    }

    /**
     * 当前数据类型
     */
    var curType = GameModeType.GAME_MODE_UNKNOW
    var micRoomData: MicRoomData? = null
    var grabRoomData: GrabRoomData? = null
    var raceRoomData: RaceRoomData? = null

}