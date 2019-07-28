package com.module.feeds.watch.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class FeedSongModel : Serializable {
    @JSONField(name = "challengeID")
    var challengeID: Long? = null
    @JSONField(name = "createdAt")
    var createdAt: Long? = null
    @JSONField(name = "feedID")
    var feedID: Int? = null
    @JSONField(name = "needChallenge")
    var needChallenge: Boolean? = null
    @JSONField(name = "needRecommentTag")
    var needRecommentTag: Boolean? = null
    @JSONField(name = "playDurMs")
    var playDurMs: Int? = null
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

    override fun toString(): String {
        return "FeedSongModel(createdAt=$createdAt, feedID=$feedID, needChallenge=$needChallenge, needRecommentTag=$needRecommentTag, playDurMs=$playDurMs, playURL=$playURL, songID=$songID, songTpl=$songTpl, tags=$tags, title=$title, userID=$userID)"
    }

}