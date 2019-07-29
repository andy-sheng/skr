package com.module.feeds.watch.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class FeedsLikeModel : Serializable {
    @JSONField(name = "feedID")
    var feedID: Int = 0
    @JSONField(name = "feedType")
    var feedType: String? = null
    @JSONField(name = "feedSong")
    var song: FeedSongModel? = null
    @JSONField(name = "user")
    var user: FeedUserInfo? = null

    var isLiked = true   // 标记是否喜欢，默认从服务器拿的都是喜欢
}
