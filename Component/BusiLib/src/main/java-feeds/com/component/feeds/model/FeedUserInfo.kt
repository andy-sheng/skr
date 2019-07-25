package com.component.feeds.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class FeedUserInfo : Serializable {
    @JSONField(name = "avatar")
    var avatar: String? = null
    @JSONField(name = "nickname")
    var nickname: String? = null
    @JSONField(name = "userID")
    var userID: Int? = null
}