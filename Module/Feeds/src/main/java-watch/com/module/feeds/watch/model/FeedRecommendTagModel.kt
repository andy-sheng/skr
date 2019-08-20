package com.module.feeds.watch.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable


class FeedRecommendTagModel : Serializable {
    @JSONField(name = "bigImgURL")
    var bigImgURL: String? = null
    @JSONField(name = "subTitle")
    var subTitle: String? = null
    @JSONField(name = "smallImgURL")
    var smallImgURL: String? = null
    @JSONField(name = "rankDesc")
    var rankDesc: String? = null
    @JSONField(name = "rankID")
    var rankID: Int = 0
    @JSONField(name = "timeMs")
    var timeMs: Long = 0L
    @JSONField(name = "isSupportCollected")
    var isSupportCollected: Boolean = false //歌单是否支持收藏
    @JSONField(name = "isCollected")
    var isCollected: Boolean = false //自己是否收藏过该歌单

}