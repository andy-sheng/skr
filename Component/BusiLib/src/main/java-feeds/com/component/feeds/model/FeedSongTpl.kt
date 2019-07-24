package com.component.feeds.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class FeedSongTpl : Serializable {
    @JSONField(name = "bgm")
    var bgm: String? = null
    @JSONField(name = "bgmDurMs")
    var bgmDurMs: Long? = null
    @JSONField(name = "composer")
    var composer: String? = null
    @JSONField(name = "cover")
    var cover: String? = null
    @JSONField(name = "createdAt")
    var createdAt: String? = null
    @JSONField(name = "lrcTs")// 时间戳歌词
    var lrcTs: String? = null
    @JSONField(name = "lrcTxt")// 纯文本歌词
    var lrcTxt: String? = null
    @JSONField(name = "lyricist")
    var lyricist: String? = null
    @JSONField(name = "songName")
    var songName: String? = null
    @JSONField(name = "tplID")
    var tplID: Int? = null
    @JSONField(name = "uploadUserID")
    var uploadUserID: Int? = null
}