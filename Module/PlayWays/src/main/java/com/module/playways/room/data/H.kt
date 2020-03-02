package com.module.playways.room.data

import com.component.busilib.constans.GameModeType
import com.module.playways.battle.room.BattleRoomData
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.mic.room.MicRoomData
import com.module.playways.party.room.PartyRoomData
import com.module.playways.race.room.RaceRoomData
import com.module.playways.relay.room.RelayRoomData
import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.GrabRoom.EQRoundStatus
import com.zq.live.proto.MicRoom.EMRoundStatus

object H {
    //两个不同房间切换的时候导致数据被刷新问题
    fun reset(from: String) {
        /**
         * 当前数据类型
         */
        if (from == mFrom) {
            curType = GameModeType.GAME_MODE_UNKNOW
            micRoomData = null
            grabRoomData = null
            raceRoomData = null
            relayRoomData = null
            partyRoomData = null
        }
    }

    var mFrom: String = ""

    fun isGrabRoom(): Boolean {
        return curType == GameModeType.GAME_MODE_GRAB
    }

    fun isMicRoom(): Boolean {
        return curType == GameModeType.GAME_MODE_MIC
    }

    fun isRelayRoom(): Boolean {
        return curType == GameModeType.GAME_MODE_RELAY
    }

    fun isPartyRoom(): Boolean {
        return curType == GameModeType.GAME_MODE_PARTY
    }

    fun isRaceRoom(): Boolean {
        return curType == GameModeType.GAME_MODE_RACE
    }

    fun isBattleRoom(): Boolean {
        return curType == GameModeType.GAME_MODE_BATTLE
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

    fun setType(type: Int, from: String) {
        curType = type
        mFrom = from
    }

    /**
     * 当前数据类型
     */
    var curType = GameModeType.GAME_MODE_UNKNOW
    var micRoomData: MicRoomData? = null
    var grabRoomData: GrabRoomData? = null
    var raceRoomData: RaceRoomData? = null
    var relayRoomData: RelayRoomData? = null
    var partyRoomData: PartyRoomData? = null
    var battleRoomData: BattleRoomData? = null
}