package com.module.posts.watch.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import java.io.Serializable

class PostsWatchModel : Serializable {
    @JSONField(name = "bestComment")
    var bestComment: PostsBestCommendModel? = null
    @JSONField(name = "hasFollow")
    var hasFollow: Boolean? = null
    @JSONField(name = "isLiked")
    var isLiked: Boolean = false
    @JSONField(name = "numeric")
    var numeric: PostsNumericModel? = null
    @JSONField(name = "posts")
    var posts: PostsModel? = null
    @JSONField(name = "user")
    var user: UserInfoModel? = null

    //todo 自定义在ui上的属性
    var isExpend = false  // 文字是否展开

    //！！！！只有帖子详情页用
    var relationShip: RelationShip? = null

    fun isAudit(): Boolean {
        return posts?.status == PostsModel.EPS_AUDIT_ACCEPT
    }

    override fun toString(): String {
        return "PostsWatchModel(bestComment=$bestComment, hasFollow=$hasFollow, isLiked=$isLiked, numeric=$numeric, posts=$posts, user=$user, isExpend=$isExpend)"
    }

    fun toDebugString(): String {
        return "帖子 posts=${posts?.toDebugString()}"
    }

    class RelationShip : Serializable {
        var isBlack: Boolean = false
        var isFollow: Boolean = false
        var isFriend: Boolean = false
    }
}