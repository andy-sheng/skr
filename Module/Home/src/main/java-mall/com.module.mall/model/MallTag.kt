package com.module.mall.model

import com.alibaba.fastjson.annotation.JSONField


data class MallTag(
        @JSONField(name = "displayType")
        var displayType: Int = 0,
        @JSONField(name = "displayTypeDesc")
        var displayTypeDesc: String = ""
)