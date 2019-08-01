package com.module.feeds.watch.model

import com.alibaba.fastjson.annotation.JSONField
import com.component.lyrics.LyricsReader
import java.io.Serializable

class FeedSongTpl : Serializable {

    @JSONField(name = "bgm")
    var bgm: String? = null
    @JSONField(name = "bgmDurMs")
    var bgmDurMs: Long = 0
    @JSONField(name = "composer")
    var composer: String? = null
    @JSONField(name = "cover")
    var cover: String? = null
    @JSONField(name = "createdAt")
    var createdAt: Long = 0
    @JSONField(name = "lrcTs")// 时间戳歌词
    var lrcTs: String? = null
    @Transient var lrcTsReader: LyricsReader? = null// 时间戳歌词 客户端缓存写入 服务器不会返回
    @JSONField(name = "lrcTxt")// 纯文本歌词
    var lrcTxt: String? = null
    var lrcTxtStr: String? = null// 纯文本歌词 客户端缓存写入 服务器不会返回

    @JSONField(name = "lrcType")// 纯文本歌词 0 1伴奏 2 纯文本
    var lrcType: Int = 0
    @JSONField(name = "lyricist")
    var lyricist: String? = null
    @JSONField(name = "songName")
    var songName: String? = null
    @JSONField(name = "tplID")
    var tplID: Int = 0
    @JSONField(name = "uploader")
    var uploader: FeedUserInfo? = null

    override fun toString(): String {
        return "FeedSongTpl(bgm=$bgm, bgmDurMs=$bgmDurMs, composer=$composer, cover=$cover, createdAt=$createdAt, lrcTs=$lrcTs, lrcTxt=$lrcTxt, lrcType=$lrcType, lyricist=$lyricist, songName=$songName, tplID=$tplID, uploader=$uploader)"
    }

}