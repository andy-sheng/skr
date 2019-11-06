package com.module.playways.room.record

import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel

class GiftRecordModel : Serializable {
    @JSONField(name = "giftInfo")
    var gift: GiftRecordGiftModel? = null
    @JSONField(name = "userInfo")
    var userInfo: UserInfoModel? = null
}

class GiftRecordGiftModel : Serializable {
    @JSONField(name = "actionDesc")
    var actionDesc: String? = null
    @JSONField(name = "amount")
    var amount: Int = 0
    @JSONField(name = "giftID")
    var giftID: Int = 0
    @JSONField(name = "giftPic")
    var giftPic: String? = null
    @JSONField(name = "receiver")
    var receiver: Int = 0
    @JSONField(name = "sender")
    var sender: Int = 0
    @JSONField(name = "timeMs")
    var timeMs: Long = 0L
}



