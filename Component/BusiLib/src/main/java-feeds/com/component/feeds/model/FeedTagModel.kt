package com.component.feeds.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class FeedTagModel : Serializable{
    @JSONField(name = "tagDesc")
    var tagDesc: String? = null
    @JSONField(name = "tagID")
    var tagID: Int? = null

    override fun toString(): String {
        return "FeedTagModel(tagDesc=$tagDesc, tagID=$tagID)"
    }

}