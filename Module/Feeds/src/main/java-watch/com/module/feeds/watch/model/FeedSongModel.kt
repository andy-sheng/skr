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
    var playCurPos:Int = 0 // 当前播放到哪了，只在客户端用，服务器不会返回
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
    @JSONField(name = "workName")
    var workName:String? = null

    override fun toString(): String {
        return "FeedSongModel(challengeID=$challengeID, createdAt=$createdAt, feedID=$feedID, needChallenge=$needChallenge, needRecommentTag=$needRecommentTag, playDurMs=$playDurMs, playURL=$playURL, playCurPos=$playCurPos, songID=$songID, songTpl=$songTpl, tags=$tags, title=$title, userID=$userID, workName=$workName)"
    }
}