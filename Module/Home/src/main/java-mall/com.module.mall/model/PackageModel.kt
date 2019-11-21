package com.module.mall.model

import com.alibaba.fastjson.annotation.JSONField


data class PackageModel(
        @JSONField(name = "expireTime")
        var expireTime: Int = 0,
        @JSONField(name = "packetItemID")
        var packetItemID: String = "",
        @JSONField(name = "useStatus")
        var useStatus: String = "",
        @JSONField(name = "goodsInfo")
        var goodsInfo: ProductModel? = null
)