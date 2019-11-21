package com.module.mall.model

import com.alibaba.fastjson.annotation.JSONField


data class ProductModel(
        @JSONField(name = "bgColor")
        var bgColor: String = "",
        @JSONField(name = "description")
        var description: String = "",
        @JSONField(name = "displayType")
        var displayType: String = "",
        @JSONField(name = "goodsID")
        var goodsID: Int = 0,
        @JSONField(name = "goodsName")
        var goodsName: String = "",
        @JSONField(name = "goodsURL")
        var goodsURL: String = "",
        @JSONField(name = "needShow")
        var needShow: Boolean = false,
        @JSONField(name = "price")
        var price: List<Price> = listOf(),
        @JSONField(name = "sortID")
        var sortID: Int = 0,
        @JSONField(name = "sourceURL")
        var sourceURL: String = ""
)

data class Price(
        @JSONField(name = "buyType")
        var buyType: String = "",
        @JSONField(name = "price")
        var price: String = "",
        @JSONField(name = "priceType")
        var priceType: String = "",
        @JSONField(name = "realPrice")
        var realPrice: Int = 0
)