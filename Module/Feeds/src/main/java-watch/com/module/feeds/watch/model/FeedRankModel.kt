package com.module.feeds.watch.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class FeedRankModel : Serializable {
    /**
     * rankCnt : 0
     * rankType : FRT_UNKNOWN
     * tagID : 0
     * tagName : string
     */
    @JSONField(name = "rankCnt")
    var rankCnt: Int? = null
    @JSONField(name = "rankType")
    var rankType: String? = null
    @JSONField(name = "tagID")
    var tagID: Int? = null
    @JSONField(name = "tagName")
    var tagName: String? = null
}
