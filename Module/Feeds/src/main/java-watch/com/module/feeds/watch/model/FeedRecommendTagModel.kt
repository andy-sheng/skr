package com.module.feeds.watch.model

import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField

class FeedRecommendTagModel : Serializable {
    @JSONField(name = "bigImgURL")
    var bigImgURL: String? = null
    @JSONField(name = "subTitle")
    var subTitle: String? = null
    @JSONField(name = "smallImgURL")
    var smallImgURL: String? = null
    @JSONField(name = "tagDesc")
    var tagDesc: String? = null
    @JSONField(name = "tagTypeID")
    var tagTypeID: Int = 0
    @JSONField(name = "timeMs")
    var timeMs: Long = 0L

}