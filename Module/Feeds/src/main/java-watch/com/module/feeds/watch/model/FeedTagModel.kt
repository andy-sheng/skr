package com.module.feeds.watch.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class FeedTagModel : Serializable{
    @JSONField(name = "tagDesc")
    var tagDesc: String? = null
    @JSONField(name = "tagID")
    var tagID: Int? = null
}