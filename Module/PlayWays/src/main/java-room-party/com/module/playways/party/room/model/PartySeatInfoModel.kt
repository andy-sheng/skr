package com.module.playways.party.room.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

// 座位信息
class PartySeatInfoModel : Serializable {

    @JSONField(name = "seatSeq")
    var seatSeq: Int? = null     // 服务器席位从1开始
    @JSONField(name = "seatStatus")
    var seatStatus: Int? = null  // 座位的状态   SS_OPEN= 1;  //打开  SS_CLOSE   = 2; //关闭
    @JSONField(name = "userID")
    var userID: Int? = null      // 座位上人的id
    @JSONField(name = "micStatus")
    var micStatus: Int? = null   // 麦的状态     MS_OPEN    = 1; //开麦   MS_CLOSE   = 2; //闭麦

    override fun toString(): String {
        return "PartySeatInfoModel(seatSeq=$seatSeq, seatStatus=$seatStatus, userID=$userID, micStatus=$micStatus)"
    }
}