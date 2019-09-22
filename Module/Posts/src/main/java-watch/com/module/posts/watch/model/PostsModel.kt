package com.module.posts.watch.model

import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField
import com.component.busilib.model.FeedSongModel


// 帖子信息
class PostsModel : Serializable {

    companion object {
        const val EPS_UNAUDIT = 1       //未审核
        const val EPS_AUDIT_REJECT = 2  //审核拒绝(失败）
        const val EPS_AUDIT_ACCEPT = 3  //审核接受（成功）
    }

    @JSONField(name = "audios")
    var audios: List<PostsResoureModel>? = null
    @JSONField(name = "createdAt")
    var createdAt: Long = 0L
    @JSONField(name = "pictures")
    var pictures: List<String>? = null
    @JSONField(name = "postsID")
    var postsID: Long = 0L
    @JSONField(name = "redpacketInfo")
    var redpacketInfo: PostsRedPkgModel? = null
    @JSONField(name = "songInfo")
    var song: FeedSongModel? = null
    @JSONField(name = "status")
    var status: Int = 0
    @JSONField(name = "title")
    var title: String = ""
    @JSONField(name = "topicInfo")
    var topicInfo: PostsTopicModel? = null
    @JSONField(name = "userID")
    var userID: Int = 0
    @JSONField(name = "videos")
    var videos: List<PostsResoureModel?>? = null
    @JSONField(name = "voteInfo")
    var voteInfo: PostsVoteModel? = null

    override fun toString(): String {
        return "PostsModel(audios=$audios, createdAt=$createdAt, pictures=$pictures, postsID=$postsID, redpacketInfo=$redpacketInfo, title='$title', topicInfo=$topicInfo, videos=$videos, voteInfo=$voteInfo)"
    }
}