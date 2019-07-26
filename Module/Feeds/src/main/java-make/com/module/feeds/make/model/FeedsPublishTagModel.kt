package com.module.feeds.make.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable


class FeedsPublishTagModel : Serializable {
    @JSONField(name = "rankID")
    var rankID: String? = null
    @JSONField(name = "rankName")
    var rankName: String? = null
    @JSONField(name = "tags")
    var tags: List<Tag?>? = null

    class Tag : Serializable {
        @JSONField(name = "tagDesc")
        var tagDesc: String? = null
        @JSONField(name = "tagID")
        var tagID: Int? = null
    }

}

