package com.module.posts.watch.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class PostsVoteModel : Serializable {

    @JSONField(name = "hasVoted")
    var hasVoted: Boolean? = null  //是否投票
    @JSONField(name = "voteID")
    var voteID: Long = 0L  //投票标识
    @JSONField(name = "voteInfo")
    var voteList: List<PostsVoteItemModel>? = null
    @JSONField(name = "voteSeq")
    var voteSeq: Int = 0  //投票的次序(1-4)

    override fun toString(): String {
        return "PostsVoteModel(hasVoted=$hasVoted, voteID=$voteID, voteList=$voteList, voteSeq=$voteSeq)"
    }

}