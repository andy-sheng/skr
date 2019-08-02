package com.module.feeds.watch.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class FeedSongModel : Serializable {
    @JSONField(name = "challengeID")
    var challengeID: Long = 0
    @JSONField(name = "createdAt")
    var createdAt: Long = 0
    @JSONField(name = "feedID")
    var feedID: Int = 0
    @JSONField(name = "needChallenge")
    var needChallenge: Boolean = false
    @JSONField(name = "needRecommentTag")
    var needRecommentTag: Boolean = false
    @JSONField(name = "playDurMs")
    var playDurMs: Int = 0 // 服务器返回的播放总时长
        get() {
            if (playDurMsFromPlayerForDebug > 0) {
                return playDurMsFromPlayerForDebug
            }
            return field
        }

    var playDurMsFromPlayerForDebug: Int = 0 // 从播放器拿到的播放总时间， 仅仅是调试使用
    @JSONField(name = "playURL")
    var playURL: String? = null
    var playCurPos: Int = 0 // 当前播放到哪了，只在客户端用，服务器不会返回
    var lyricType: Int = 0 // 仅针对播放器在buffering的时候控制歌词滚动, 只有客户端有，每次时候的时候都应该是设置过新的状态，旧的状态不可用，0为暂定，1为播放，只在FeedViewHolder.refreshLyricType里用到，不可用别的地方
    @JSONField(name = "songID")
    var songID: Int = 0
    @JSONField(name = "songTpl")
    var songTpl: FeedSongTpl? = null
    @JSONField(name = "tags")
    var tags: List<FeedTagModel?>? = null
    @JSONField(name = "title")
    var title: String? = null
    @JSONField(name = "userID")
    var userID: Int = 0
    @JSONField(name = "workName")
    var workName: String? = null

    override fun toString(): String {
        return "FeedSongModel(challengeID=$challengeID, createdAt=$createdAt, feedID=$feedID, needChallenge=$needChallenge, needRecommentTag=$needRecommentTag, playDurMs=$playDurMs, playURL=$playURL, playCurPos=$playCurPos, songID=$songID, songTpl=$songTpl, tags=$tags, title=$title, userID=$userID, workName=$workName)"
    }
}