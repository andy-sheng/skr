package com.module.posts.watch.model

import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField


// 帖子数值信息
class PostsNumericModel : Serializable {

    @JSONField(name = "commentCnt")
    var commentCnt: Long = 0L //评论数
    @JSONField(name = "exposure")
    var exposure: Long = 0L  //曝光数
    @JSONField(name = "postsID")
    var postsID: Long? = 0L
    @JSONField(name = "shareCnt")
    var shareCnt: Long? = 0L //转发分享数
    @JSONField(name = "starCnt")
    var starCnt: Long? = 0L  //点赞数

    override fun toString(): String {
        return "PostsNumericModel(commentCnt=$commentCnt, exposure=$exposure, postsID=$postsID, shareCnt=$shareCnt, starCnt=$starCnt)"
    }

}