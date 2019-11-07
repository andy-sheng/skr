package com.module.msg.follow

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class LastNewsModel : Serializable {

    companion object {
        const val TYPE_SP_FOLLOW = 1
        const val TYPE_LAST_FOLLOW = 2
        const val TYPE_POSTS_COMMENT_LIKE = 3
        const val TYPE_GIFT = 4
    }

    @JSONField(name = "latestNews")
    var latestNews: String? = null
    @JSONField(name = "timeMs")
    var timeMs: Long = 0
    @JSONField(name = "listType")
    var listType: Int = 0
}