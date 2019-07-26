package com.module.feeds.rank.model

import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField

/**
 * 榜单标签
 */
class FeedRankTagModel : Serializable {
    @JSONField(name = "tagDesc")
    var tagDesc: String? = null
    @JSONField(name = "tagType")
    var tagType: Int? = null
}

