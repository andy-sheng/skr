package com.module.feeds.make.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable


class FeedsPublishTagModel : Serializable {
        @JSONField(name = "tagDesc")
        var tagDesc: String? = null
        @JSONField(name = "tagID")
        var tagID: Int? = null

}

