package com.module.posts.watch.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import java.io.Serializable

class PostsBestCommendModel : Serializable {
    @JSONField(name = "comment")
    var comment: PostsCommentModel? = null
    @JSONField(name = "commentUser")
    var user: UserInfoModel? = null
    @JSONField(name = "isLiked")
    var isLiked: Boolean = false

    override fun toString(): String {
        return "PostsBestCommendModel(comment=$comment, user=$user, isLiked=$isLiked)"
    }
}