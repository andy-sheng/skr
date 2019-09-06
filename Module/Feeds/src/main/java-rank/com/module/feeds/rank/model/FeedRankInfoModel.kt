package com.module.feeds.rank.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import java.io.Serializable

class FeedRankInfoModel : Serializable {
    @JSONField(name = "challengeID")
    var challengeID: Long? = null
    @JSONField(name = "rankTitle")
    var rankTitle: String? = null
    @JSONField(name = "userCnt")
    var userCnt: Int? = null
    @JSONField(name = "userInfo")
    var userInfo: UserInfoModel? = null

    override fun toString(): String {
        return "FeedRankInfoModel(challengeID=$challengeID, rankTitle=$rankTitle, userCnt=$userCnt, userInfo=$userInfo)"
    }
}