package com.module.feeds.watch.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class FeedSongModel : Serializable {
    @JSONField(name = "createdAt")
    var createdAt: Long = 0L
    @JSONField(name = "feedID")
    var feedID: Int? = null
    @JSONField(name = "needChallenge")
    var needChallenge: Boolean? = null
    @JSONField(name = "needRecommentTag")
    var needRecommentTag: Boolean? = null
    @JSONField(name = "playURL")
    var playURL: String? = null
    @JSONField(name = "songID")
    var songID: Int? = null
    @JSONField(name = "songTpl")
    var songTpl: FeedSongTpl? = null
    @JSONField(name = "tags")
    var tags: List<FeedTagModel?>? = null
    @JSONField(name = "title")
    var title: String? = null
    @JSONField(name = "userID")
    var userID: Int? = null
}