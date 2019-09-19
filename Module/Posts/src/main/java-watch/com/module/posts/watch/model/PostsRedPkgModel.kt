package com.module.posts.watch.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

// 帖子红包
class PostsRedPkgModel : Serializable {

    companion object {
        const val ROS_UN_OPEN = 1   //未打开
        const val ROS_HAS_OPEN = 2   // 已打开
    }

    @JSONField(name = "openStatus")
    var openStatus: Int = 0    // 红包的状态
    @JSONField(name = "redpacketID")
    var redpacketID: Long = 0L

    override fun toString(): String {
        return "PostsRedPkgModel(openStatus=$openStatus, redpacketID=$redpacketID)"
    }

}