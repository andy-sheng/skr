package com.module.posts.publish.topic

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class Category : Serializable {
    @JSONField(name = "categoryDesc")
    var categoryDesc: String? = null
    @JSONField(name = "categoryID")
    var categoryID: Int? = null
}


class Topic : Serializable {
    @JSONField(name = "categoryID")
    var categoryID: Int? = null
    @JSONField(name = "topicDesc")
    var topicDesc: String? = null
    @JSONField(name = "topicID")
    var topicID: Int? = null
    @JSONField(name = "topicTitle")
    var topicTitle: String? = null
    @JSONField(name = "topicURL")
    var topicURL: String? = null
}
