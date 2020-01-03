package com.module.playways.relay.room.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable


class RelayResultModel : Serializable {

    companion object {
        const val GER_USER_EXIT = 1 // 用户退出
        const val GER_END_COUNTDOWN = 2  // 倒计时结束
        const val GER_USER_NO_RESPONSE = 3 // 用户无响应
    }

    @JSONField(name = "chatDurTime")
    var chatDurTime: Int = 0                // 唱聊时间（分钟）
    @JSONField(name = "exitUserID")
    var exitUserID: Int = 0                 // 退出用户id
    @JSONField(name = "gameEndReasonDesc")
    var gameEndReasonDesc: String? = null   // 原因描述
    @JSONField(name = "isFollow")
    var isFollow: Boolean? = null
    @JSONField(name = "isFriend")
    var isFriend: Boolean? = null
    @JSONField(name = "noResponseUserID")
    var noResponseUserID: Int = 0           // 没有响应掉线的用户id
    @JSONField(name = "reason")
    var reason: Int = 0
    @JSONField(name = "peerScore")
    var peerScore: Int = 0  //同伴契合度值
    @JSONField(name = "peerComment")
    var peerComment: String? = null //peerScore对应的文字
    @JSONField(name = "starCnt")
    var starCnt: Int = 0  //获得星数
    @JSONField(name = "coinCnt")
    var coinCnt: Int = 0 //获得金币数
}