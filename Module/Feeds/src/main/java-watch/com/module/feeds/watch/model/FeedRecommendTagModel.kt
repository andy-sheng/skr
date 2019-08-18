package com.module.feeds.watch.model

import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField

class FeedRecommendTagModel : Serializable {
    @JSONField(name = "bigImgURL")
    var bigImgURL: String? = null
    @JSONField(name = "remark")
    var remark: String? = null
    @JSONField(name = "smallImgURL")
    var smallImgURL: String? = null
    @JSONField(name = "tagDesc")
    var tagDesc: String? = null
    @JSONField(name = "tagType")
    var tagType: String? = null
}