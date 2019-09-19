package com.module.posts.watch.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

// 帖子的资源文件
class PostsResoureModel : Serializable {
    @JSONField(name = "URL")
    var url: String = ""
    @JSONField(name = "durTimeMs")
    var duration: Long = 0L

    override fun toString(): String {
        return "PostsResoureModel(url='$url', duration=$duration)"
    }
}