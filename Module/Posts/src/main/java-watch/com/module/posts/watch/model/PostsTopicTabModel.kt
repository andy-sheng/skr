package com.module.posts.watch.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class PostsTopicTabModel : Serializable {
    @JSONField(name = "tabDesc")
    var tabDesc: String = ""
    @JSONField(name = "tabType")
    var tabType: Int = 0

    override fun toString(): String {
        return "PostsTopicTabModel(tabDesc='$tabDesc', tabType=$tabType)"
    }
}