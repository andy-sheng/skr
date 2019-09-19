package com.module.posts.watch.model

import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField


// 帖子信息
class PostsModel : Serializable {
    @JSONField(name = "audios")
    var audios: List<PostsResoureModel>? = null
    @JSONField(name = "createdAt")
    var createdAt: Long = 0L
    @JSONField(name = "pictures")
    var pictures: List<PostsResoureModel>? = null
    @JSONField(name = "postsID")
    var postsID: Long = 0L
    @JSONField(name = "redpacketInfo")
    var redpacketInfo: PostsRedPkgModel? = null
    @JSONField(name = "title")
    var title: String = ""
    @JSONField(name = "topicInfo")
    var topicInfo: PostsTopicModel? = null
    @JSONField(name = "videos")
    var videos: List<PostsResoureModel?>? = null
    @JSONField(name = "voteInfo")
    var voteInfo: PostsVoteModel? = null

    override fun toString(): String {
        return "PostsModel(audios=$audios, createdAt=$createdAt, pictures=$pictures, postsID=$postsID, redpacketInfo=$redpacketInfo, title='$title', topicInfo=$topicInfo, videos=$videos, voteInfo=$voteInfo)"
    }
}