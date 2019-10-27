package com.module.playways.room.data

import com.component.busilib.constans.GameModeType
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.mic.room.MicRoomData
import com.module.playways.race.room.RaceRoomData
import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.GrabRoom.EQRoundStatus
import com.zq.live.proto.MicRoom.EMRoundStatus

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

    fun isGrabRoom(): Boolean {
        return curType == GameModeType.GAME_MODE_GRAB
    }

    fun isMicRoom(): Boolean {
        return curType == GameModeType.GAME_MODE_MIC
    }

    public fun getSongModel(): SongModel? {
        var cur: SongModel? = null
        if (H.isGrabRoom()) {
            cur = H.grabRoomData?.realRoundInfo?.music
            if (H.grabRoomData?.realRoundInfo?.status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
                cur = cur?.pkMusic
            }
        } else if (H.isMicRoom()) {
            cur = H.micRoomData?.realRoundInfo?.music
            if (H.micRoomData?.realRoundInfo?.status == EMRoundStatus.MRS_SPK_SECOND_PEER_SING.value) {
                cur = cur?.pkMusic
            }
        }
        return cur
    }

    /**
     * 当前数据类型
     */
    var curType = GameModeType.GAME_MODE_UNKNOW
    var micRoomData: MicRoomData? = null
    var grabRoomData: GrabRoomData? = null
    var raceRoomData: RaceRoomData? = null

}