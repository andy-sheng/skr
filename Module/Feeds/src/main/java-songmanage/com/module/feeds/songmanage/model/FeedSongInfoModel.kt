package com.module.feeds.songmanage.model

import com.alibaba.fastjson.annotation.JSONField
import com.module.feeds.watch.model.FeedSongModel
import java.io.Serializable

class FeedSongInfoModel : Serializable {
    /**
     * rankID : 0
     * title : string
     */
    @JSONField(name = "feedID")
    var feedID: Int = 0
    @JSONField(name = "feedSong")
    var song: FeedSongModel? = null
}