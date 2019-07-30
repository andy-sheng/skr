package com.module.feeds.watch.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class FeedRankModel : Serializable {
    /**
     * rankDesc : ""
     * rankType : FRT_UNKNOWN
     */
    @JSONField(name = "rankDesc")
    var rankDesc: String? = null
    @JSONField(name = "rankTitle")
    var rankTitle: String? = null

    override fun toString(): String {
        return "FeedRankModel(rankDesc=$rankDesc, rankTitle=$rankTitle)"
    }
}
