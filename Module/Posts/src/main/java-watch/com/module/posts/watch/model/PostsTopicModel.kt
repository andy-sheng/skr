package com.module.posts.watch.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class PostsTopicModel : Serializable {
    @JSONField(name = "topicDesc")
    var topicDesc: String = ""  //话题描述
    @JSONField(name = "topicID")
    var topicID: Long = 0L

    override fun toString(): String {
        return "PostsTopicModel(topicDesc='$topicDesc', topicID=$topicID)"
    }
}