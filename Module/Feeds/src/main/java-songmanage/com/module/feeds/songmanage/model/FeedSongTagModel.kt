package com.module.feeds.songmanage.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class FeedSongTagModel : Serializable {
    /**
     * rankID : 0
     * title : string
     */
    @JSONField(name = "tagType")
    var tagType: Int = 0
    @JSONField(name = "tagDesc")
    var tagDesc: String = ""

    override fun toString(): String {
        return "SongTagModel(tagType=$tagType, tagDesc=$tagDesc)"
    }
}