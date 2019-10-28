package com.module.playways.mic.home

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import com.component.busilib.friends.VoiceInfoModel
import java.io.Serializable

// 首页推荐房的model
class RecomMicInfoModel : Serializable {

    @JSONField(name = "roomInfo")
    var roomInfo: RecomMicRoomModel? = null
    @JSONField(name = "category")
    var category: Int = 0   // 分类

    override fun toString(): String {
        return "RecomMicInfoModel(roomInfo=$roomInfo, category=$category)"
    }
}

// 首页推荐房间信息
class RecomMicRoomModel : Serializable {
    @JSONField(name = "roomID")
    var roomID: Int = 0
    @JSONField(name = "inPlayersNum")
    var inPlayersNum: Int = 0
    @JSONField(name = "totalPlayersNum")
    var totalPlayesrNum: Int = 0
    @JSONField(name = "roomName")
    var roomName: String = ""
    @JSONField(name = "roomLevel")
    var roomLevel: Int = 0
    @JSONField(name = "userlist")
    var userList: List<RecomUserInfo>? = null

    override fun toString(): String {
        return "RecomMicRoomModel(roomID=$roomID, inPlayersNum=$inPlayersNum, totalPlayesrNum=$totalPlayesrNum, roomName='$roomName', roomLevel=$roomLevel, userList=$userList)"
    }
}

// 首页推荐的用户信息
class RecomUserInfo : Serializable {
    @JSONField(name = "userInfo")
    var userInfo: UserInfoModel? = null
    @JSONField(name = "voiceInfo")
    var voiceInfo: VoiceInfoModel? = null

    override fun toString(): String {
        return "RecomUserInfo(userInfo=$userInfo, voiceInfo=$voiceInfo)"
    }

}