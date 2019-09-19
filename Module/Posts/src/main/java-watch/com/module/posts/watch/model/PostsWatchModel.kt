package com.module.posts.watch.model

import java.io.Serializable
import kotlin.random.Random
import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel

class PostsWatchModel : Serializable {
    @JSONField(name = "comment")
    var comment: PostsCommentModel? = null
    @JSONField(name = "hasFollow")
    var hasFollow: Boolean? = null
    @JSONField(name = "isLiked")
    var isLiked: Boolean? = null
    @JSONField(name = "numeric")
    var numeric: PostsNumericModel? = null
    @JSONField(name = "posts")
    var posts: PostsModel? = null
    @JSONField(name = "user")
    var user: UserInfoModel? = null

    //todo 自定义在ui上的属性
    var isExpend = false  // 文字是否展开

    fun getImageList(): List<String>? {
        return if (posts?.pictures.isNullOrEmpty()) {
            null
        } else {
            var pics = ArrayList<String>()
            posts?.pictures?.let {
                it.forEach { pic ->
                    pics.add(pic.url)
                }
            }
            pics
        }
    }
}