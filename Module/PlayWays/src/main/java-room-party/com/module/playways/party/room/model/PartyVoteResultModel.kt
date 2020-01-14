package com.module.playways.party.room.model

import com.alibaba.fastjson.annotation.JSONField

class PartyVoteResultModel {
    @JSONField(name = "user")
    var user: PartyPlayerInfoModel? = null
    @JSONField(name = "voteCnt")
    var voteCnt: Int? = null
}