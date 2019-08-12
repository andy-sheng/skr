package com.module.feeds.watch.model

import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.annotation.JSONField
import com.module.feeds.watch.manager.FeedCollectDB
import java.io.Serializable

class FeedsCollectModel : Serializable {

    companion object {
        fun toFeedCollectDB(feedsCollectModel: FeedsCollectModel?): FeedCollectDB {
            val feedCollectDB = FeedCollectDB()
            feedsCollectModel?.let {
                feedCollectDB.feedID = it.feedID
                feedCollectDB.feedType = it.feedType

                val feedSong = JSONObject()
                feedSong["feedSong"] = it.song
                feedCollectDB.feedSong = feedSong.toJSONString()

                val feedUser = JSONObject()
                feedUser["user"] = it.user
                feedCollectDB.user = feedUser.toJSONString()

            }
            return feedCollectDB
        }

        fun parseFeedCollectModel(feedCollectDB: FeedCollectDB?): FeedsCollectModel {
            val feedCollectModel = FeedsCollectModel()
            feedCollectDB?.let {
                feedCollectModel.feedID = it.feedID
                feedCollectModel.feedType = it.feedType
                feedCollectModel.song = FeedSongModel.parseFeedSongModel(it.feedSong)
                feedCollectModel.user = FeedUserInfo.parseFeedUserInfo(it.user)
            }
            return feedCollectModel
        }
    }


    @JSONField(name = "feedID")
    var feedID: Int = 0
    @JSONField(name = "feedType")
    var feedType: Int = 0
    @JSONField(name = "feedSong")
    var song: FeedSongModel? = null
    @JSONField(name = "user")
    var user: FeedUserInfo? = null

    var isLiked = true   // 标记是否喜欢，默认从服务器拿的都是喜欢


}
