package com.module.mall.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class PackageModel : Serializable {
    @JSONField(name = "expireTime")
    var expireTime: Long = 0
    @JSONField(name = "packetItemID")
    var packetItemID: String = ""
    @JSONField(name = "useStatus")
    var useStatus: Int = 0
    @JSONField(name = "goodsInfo")
    var goodsInfo: ProductModel? = null
}