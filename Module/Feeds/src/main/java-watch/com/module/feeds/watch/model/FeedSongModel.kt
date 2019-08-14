package com.module.feeds.watch.model

import android.text.TextUtils
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class FeedSongModel : Serializable {

    @JSONField(name = "challengeID")
    var challengeID: Long = 0

    //榜单名字
    var challengeDesc: String? = null

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
    var lyricStatus: Int = 0 // 仅针对播放器在buffering的时候控制歌词滚动, 只有客户端有，每次时候的时候都应该是设置过新的状态，旧的状态不可用，0为暂定，1为播放，只在FeedViewHolder.refreshLyricType里用到，不可用别的地方
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
    @JSONField(name = "songType")
    var songType: Int? = 0 //	- EST_UNKNOWN = 0 : 未知 - EST_HAS_ACCOMPANY = 1 : 带伴奏演唱 - EST_NO_ACCOMPANY = 2 : 无伴奏演唱

    override fun toString(): String {
        return "FeedSongModel(challengeID=$challengeID, createdAt=$createdAt, feedID=$feedID, needChallenge=$needChallenge, needRecommentTag=$needRecommentTag, playDurMsFromPlayerForDebug=$playDurMsFromPlayerForDebug, playURL=$playURL, playCurPos=$playCurPos, lyricStatus=$lyricStatus, songID=$songID, songTpl=$songTpl, tags=$tags, title=$title, userID=$userID, workName=$workName, songType=$songType)"
    }

    fun getDisplayName(): CharSequence? {
        if (challengeID == 0L) {
            // 快唱
            return songTpl?.getDisplaySongName()
        } else {
            if (!TextUtils.isEmpty(songTpl?.songNameChange)) {
                return songTpl?.songNameChange
            }

            if (!TextUtils.isEmpty(songTpl?.songName)) {
                return songTpl?.songName
            }

            if (!TextUtils.isEmpty(workName)) {
                return workName
            }

            if (!TextUtils.isEmpty(challengeDesc)) {
                return challengeDesc
            }
            return ""
        }
    }
}