package com.module.playways.mic.home

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import java.io.Serializable

// 首页推荐房的model
class RecomMicInfoModel : Serializable {

    @JSONField(name = "roomInfo")
    var roomInfo: RecomMicRoomModel? = null
    @JSONField(name = "category")
    var category: Int = 0   // 分类
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
    @JSONField(name = "userInfo")
    var userList: List<UserInfoModel>? = null

    override fun toString(): String {
        return "RecomMicRoomModel(roomID=$roomID, inPlayersNum=$inPlayersNum, totalPlayesrNum=$totalPlayesrNum, roomName='$roomName', roomLevel=$roomLevel, userList=$userList)"
    }
}