package com.module.mall.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class ProductModel : Serializable {
    @JSONField(name = "bgColor")
    var bgColor: String = ""
    @JSONField(name = "description")
    var description: String = ""
    @JSONField(name = "displayType")
    var displayType: Int = 0
    @JSONField(name = "isBuy")
    var isBuy: Boolean = false
    @JSONField(name = "goodsID")
    var goodsID: Int = 0
    @JSONField(name = "goodsName")
    var goodsName: String = ""
    @JSONField(name = "goodsURL")
    var goodsURL: String = ""
    @JSONField(name = "needShow")
    var needShow: Boolean = false
    @JSONField(name = "price")
    var price: List<Price> = listOf()
    @JSONField(name = "sortID")
    var sortID: Int = 0
    @JSONField(name = "sourceURL")
    var sourceURL: String = ""

    constructor()
}

class Price : Serializable {
    @JSONField(name = "buyType")
    var buyType: Int = 0
    @JSONField(name = "price")
    var price: String = ""
    @JSONField(name = "priceType")
    var priceType: Int = 0
    @JSONField(name = "realPrice")
    var realPrice: Int = 0
    @JSONField(name = "buyTypeDes")
    var buyTypeDes: String = ""

    constructor()
}