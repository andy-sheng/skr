package com.module.feeds.watch.model

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import com.module.feeds.watch.manager.FeedCollectDB
import java.io.Serializable

class FeedsCollectModel : Serializable {

    companion object {
        fun toFeedCollectDB(feedsCollectModel: FeedsCollectModel?): FeedCollectDB {
            val feedCollectDB = FeedCollectDB()
            feedsCollectModel?.let {
                feedCollectDB.feedID = it.feedID.toLong()
                feedCollectDB.feedType = it.feedType.toLong()
                feedCollectDB.timeMs = it.timeMs
                feedCollectDB.feedSong = JSON.toJSONString(it.song)
                feedCollectDB.user = JSON.toJSONString(it.user)

            }
            return feedCollectDB
        }

        fun parseFeedCollectModel(feedCollectDB: FeedCollectDB?): FeedsCollectModel {
            val feedCollectModel = FeedsCollectModel()
            feedCollectDB?.let {
                feedCollectModel.feedID = it.feedID.toInt()
                feedCollectModel.feedType = it.feedType.toInt()
                feedCollectModel.timeMs = it.timeMs
                feedCollectModel.song = JSON.parseObject(it.feedSong, FeedSongModel::class.java)
                feedCollectModel.user = JSON.parseObject(it.user, UserInfoModel::class.java)
            }
            return feedCollectModel
        }
    }


    @JSONField(name = "feedID")
    var feedID: Int = 0
    @JSONField(name = "feedType")
    var feedType: Int = 0
    @JSONField(name = "timeMs")
    var timeMs: Long = 0
    @JSONField(name = "feedSong")
    var song: FeedSongModel? = null
    @JSONField(name = "user")
    var user: UserInfoModel? = null

    var isLiked = true   // 标记是否喜欢，默认从服务器拿的都是喜欢


}
