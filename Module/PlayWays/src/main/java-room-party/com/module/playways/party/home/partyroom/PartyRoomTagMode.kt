package com.module.playways.party.home.partyroom

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class PartyRoomTagMode : Serializable {

    companion object {
        const val ERM_SING_PK = 1     //K歌模式
        const val ERM_GAME_PK = 2     //游戏PK
        const val ERM_MAKE_FRIEND = 3   //相亲模式
        const val ERM_ALL = 4         //所有模式

    }

    @JSONField(name = "des")
    var des: String = ""
    @JSONField(name = "gameMode")
    var gameMode: Int = 0
}