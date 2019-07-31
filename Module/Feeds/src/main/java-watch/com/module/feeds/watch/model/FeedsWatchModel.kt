package com.module.feeds.watch.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class FeedsWatchModel : Serializable {
    @JSONField(name = "exposure")
    var exposure: Int? = null
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
    @JSONField(name = "rankSeq")
    var rankSeq: Int? = null
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
        return "FeedsWatchModel(exposure=$exposure, commentCnt=$commentCnt, feedID=$feedID, feedType=$feedType, hasFollow=$hasFollow, isLiked=$isLiked, rankSeq=$rankSeq, rank=$rank, song=$song, starCnt=$starCnt, shareCnt=$shareCnt, user=$user, status=$status)"
    }

}