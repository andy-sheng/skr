package com.module.feeds.watch.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class FeedsWatchModel : Serializable {
    @JSONField(name = "challengeCnt")
    var challengeCnt: Int? = null
    @JSONField(name = "commentCnt")
    var commentCnt: Int? = null
    @JSONField(name = "feedID")
    var feedID: Int? = null
    @JSONField(name = "feedType")
    var feedType: String? = null
    @JSONField(name = "hasFollow")
    var hasFollow: Boolean? = null
    @JSONField(name = "rank")
    var rank: FeedRankModel? = null
    @JSONField(name = "song")
    var song: FeedSongModel? = null
    @JSONField(name = "starCnt")
    var starCnt: Int? = null
    @JSONField(name = "user")
    var user: FeedUserInfo? = null
}