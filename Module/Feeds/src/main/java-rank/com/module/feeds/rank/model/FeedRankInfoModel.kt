package com.module.feeds.rank.model

import com.alibaba.fastjson.annotation.JSONField
import com.module.feeds.watch.model.FeedUserInfo


class FeedRankInfoModel {
    @JSONField(name = "challengeID")
    var challengeID: Int? = null
    @JSONField(name = "rankTitle")
    var rankTitle: String? = null
    @JSONField(name = "rankType")
    var rankType: String? = null
    @JSONField(name = "userCnt")
    var userCnt: Int? = null
    @JSONField(name = "userInfo")
    var userInfo: FeedUserInfo? = null
}