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
    @JSONField(name = "rankType")
    var rankType: Int = 0  //0 未知  1全部排名  2tag排名

    override fun toString(): String {
        return "FeedRankModel(rankDesc=$rankDesc, rankType=$rankType)"
    }

}
