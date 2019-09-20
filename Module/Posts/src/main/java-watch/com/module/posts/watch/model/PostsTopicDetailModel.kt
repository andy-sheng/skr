package com.module.posts.watch.model

import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField

class PostsTopicDetailModel : Serializable {
    @JSONField(name = "categoryID")
    var categoryID: Int = 0
    @JSONField(name = "topicDesc")
    var topicDesc: String = ""
    @JSONField(name = "topicID")
    var topicID: Int = 0
    @JSONField(name = "topicTitle")
    var topicTitle: String = ""
    @JSONField(name = "topicURL")
    var topicURL: String? = ""
}