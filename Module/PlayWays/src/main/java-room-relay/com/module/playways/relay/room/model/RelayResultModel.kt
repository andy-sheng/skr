package com.module.playways.relay.room.model

import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField


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
}