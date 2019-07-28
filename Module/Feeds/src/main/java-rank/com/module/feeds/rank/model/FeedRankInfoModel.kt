package com.module.feeds.rank.model

import com.alibaba.fastjson.annotation.JSONField
import com.module.feeds.watch.model.FeedUserInfo
import java.io.Serializable


class FeedRankInfoModel : Serializable {
    @JSONField(name = "challengeID")
    var challengeID: Int? = null
    @JSONField(name = "rankTitle")
    var rankTitle: String? = null
    @JSONField(name = "rankType")
    var rankType: Int? = null
    @JSONField(name = "userCnt")
    var userCnt: Int? = null
    @JSONField(name = "userInfo")
    var userInfo: FeedUserInfo? = null
}