package com.module.feeds.feeds.model

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
    @JSONField(name = "isLiked")
    var isLiked: Boolean? = null
    @JSONField(name = "rank")
    var rank: FeedRankModel? = null
    @JSONField(name = "feedSong")
    var song: FeedSongModel? = null
    @JSONField(name = "starCnt")
    var starCnt: Int? = null
    @JSONField(name = "shareCnt")
    var shareCnt: Int? = null
    @JSONField(name = "user")
    var user: FeedUserInfo? = null
    @JSONField(name = "status")
    var status: Int = 2  //0 未知 1待审核 2审核通过

    override fun toString(): String {
        return "FeedsWatchModel(challengeCnt=$challengeCnt, commentCnt=$commentCnt, feedID=$feedID, feedType=$feedType, hasFollow=$hasFollow, isLiked=$isLiked, rank=$rank, song=$song, starCnt=$starCnt, user=$user, status=$status)"
    }
}