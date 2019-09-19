package com.module.posts.watch.model

import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField


class PostsCommentModel : Serializable {
    @JSONField(name = "audios")
    var audios: List<PostsResoureModel?>? = null
    @JSONField(name = "commentID")
    var commentID: Long = 0L
    @JSONField(name = "content")
    var content: String = ""
    @JSONField(name = "createdAt")
    var createdAt: Long = 0L
    @JSONField(name = "likedCnt")
    var likedCnt: Long = 0L
    @JSONField(name = "pictures")
    var pictures: List<String>? = null
    @JSONField(name = "postsID")
    var postsID: Long = 0L
    @JSONField(name = "userID")
    var userID: Int = 0
    @JSONField(name = "videos")
    var videos: List<PostsResoureModel?>? = null

    override fun toString(): String {
        return "PostsCommentModel(audios=$audios, commentID=$commentID, content='$content', createdAt=$createdAt, likedCnt=$likedCnt, pictures=$pictures, postsID=$postsID, userID=$userID, videos=$videos)"
    }

}