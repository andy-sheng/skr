package com.module.mall.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class MallTag : Serializable {
    @JSONField(name = "displayType")
    var displayType: Int = 0
    @JSONField(name = "displayTypeDesc")
    var displayTypeDesc: String = ""

    constructor()
}