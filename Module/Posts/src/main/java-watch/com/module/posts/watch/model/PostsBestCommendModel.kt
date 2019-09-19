package com.module.posts.watch.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import java.io.Serializable

class PostsBestCommendModel : Serializable {
    @JSONField(name = "comment")
    var comment: PostsCommentModel? = null
    @JSONField(name = "user")
    var user: UserInfoModel? = null

    override fun toString(): String {
        return "PostsBestCommendModel(comment=$comment, user=$user)"
    }
}