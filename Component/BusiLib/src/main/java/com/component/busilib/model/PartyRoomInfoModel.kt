package com.component.busilib.model

import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel

class PartyRoomTagMode : Serializable {

    companion object {
        const val ERM_DEFAULT_MIC = 0 //连麦模式
        const val ERM_SING_PK = 1     //K歌模式
        const val ERM_GAME_PK = 2     //游戏PK
        const val ERM_MAKE_FRIEND = 3   //相亲模式
        const val ERM_ALL = 100         //所有模式

    }

    @JSONField(name = "des")
    var des: String = ""
    @JSONField(name = "gameMode")
    var gameMode: Int = 0
}

class RecommendPartyInfoModel : Serializable {
    @JSONField(name = "category")
    var category: Int = 0   // 分类
    @JSONField(name = "roomInfo")
    var roomInfoModel: PartyRoomInfoModel? = null
    @JSONField(name = "userInfo")
    var userInfo: UserInfoModel? = null
}

class PartyRoomInfoModel : Serializable {
    @JSONField(name = "avatarUrl")
    var avatarUrl: String? = null
    @JSONField(name = "gameName")
    var gameName: String? = null
    @JSONField(name = "hostID")
    var ownerID: Int? = null
    @JSONField(name = "hostName")
    var ownerName: String? = null
    @JSONField(name = "playerNum")
    var playerNum: Int? = null
    @JSONField(name = "roomID")
    var roomID: Int? = null
    @JSONField(name = "roomName")
    var roomName: String? = null
    @JSONField(name = "topicName")
    var topicName: String? = null
    @JSONField(name = "roomtype")
    var roomtype: Int? = null
    @JSONField(name = "widgetUrl")
    var widgetUrl: String = ""
    @JSONField(name = "roomTagURL")
    var roomTagURL: String = ""
    @JSONField(name = "gameMode")
    var gameMode: Int? = null

    // 用房间id和所属id来去重
    override fun hashCode(): Int {
        return (roomID ?: 0) * 37 + (ownerID ?: 0)
    }

    override fun equals(other: Any?): Boolean {
        if (other is PartyRoomInfoModel) {
            if (roomID == other.roomID && ownerID == other.ownerID) {
                return true
            }
        }
        return false
    }

    override fun toString(): String {
        return "PartyRoomInfoModel(avatarUrl=$avatarUrl, gameName=$gameName, ownerID=$ownerID, ownerName=$ownerName, playerNum=$playerNum, roomID=$roomID, roomName=$roomName, topicName=$topicName, roomtype=$roomtype, widgetUrl='$widgetUrl')"
    }

}