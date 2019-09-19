package com.module.posts.watch.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class PostsVoteItemModel : Serializable {

    @JSONField(name = "voteCnt")
    var voteCnt: Long = 0L  //投票数
    @JSONField(name = "voteItem")
    var voteItem: String = ""  //投票选项条目

    override fun toString(): String {
        return "PostsVoteItemModel(voteCnt=$voteCnt, voteItem='$voteItem')"
    }
}