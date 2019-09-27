package com.module.posts.detail.model

import com.common.core.userinfo.model.UserInfoModel
import com.component.busilib.model.FeedSongModel
import com.module.posts.watch.model.PostsResoureModel

import java.io.Serializable

class PostFirstLevelCommentModel : Serializable {

    /**
     * comment : {"audios":[{"URL":"string","durTimeMs":0}],"auditStatus":"ECAS_UNKNOWN","auditStatusDesc":"string","commentID":0,"commentType":"ECT_UNKNOWN","content":"string","createdAt":"string","likedCnt":0,"parentCommentID":0,"pictures":["string"],"postsID":0,"replyType":"ET_UNKNOWN","replyedUserID":0,"status":"ECS_UNKNOWN","statusDesc":"string","subCommentCnt":0,"userID":0,"videos":[{"URL":"string","durTimeMs":0}]}
     * commentUser : {"avatar":"string","hasRedpacket":true,"nickname":"string","userID":0,"vipInfo":{"desc":"string","vipType":"EVT_UNKNOWN"}}
     * isLiked : true
     * replyUser : {"avatar":"string","nickname":"string","userID":0,"vipInfo":{"desc":"string","vipType":"EVT_UNKNOWN"}}
     */

    var comment: FirstLevelCommentBean? = null
    var commentUser: UserInfoModel? = null
    var isLiked: Boolean = false
    var isHasRedpacket: Boolean = false
    var replyUser: UserInfoModel? = null
    var secondLevelComments: MutableList<PostsSecondLevelCommentModel>? = null

    //todo 自定义在ui上的属性
    var isExpend = false  // 文字是否展开

    fun isIsLiked(): Boolean {
        return isLiked
    }

    fun setIsLiked(isLiked: Boolean) {
        this.isLiked = isLiked
    }

    class FirstLevelCommentBean : Serializable {
        /**
         * audios : [{"URL":"string","durTimeMs":0}]
         * auditStatus : ECAS_UNKNOWN
         * auditStatusDesc : string
         * commentID : 0
         * commentType : ECT_UNKNOWN
         * content : string
         * createdAt : string
         * likedCnt : 0
         * parentCommentID : 0
         * pictures : ["string"]
         * postsID : 0
         * replyType : ET_UNKNOWN
         * replyedUserID : 0
         * status : ECS_UNKNOWN
         * statusDesc : string
         * subCommentCnt : 0
         * userID : 0
         * videos : [{"URL":"string","durTimeMs":0}]
         */

        var auditStatus: String? = null
        var auditStatusDesc: String? = null
        var commentID: Int = 0
        var commentType: String? = null
        var content: String? = null
        var createdAt: Long = 0
        var likedCnt: Int = 0
        var parentCommentID: Int = 0
        var postsID: Int = 0
        var replyType: String? = null
        var replyedUserID: Int = 0
        var status: String? = null
        var statusDesc: String? = null
        var subCommentCnt: Int = 0
        var userID: Int = 0
        var audios: List<PostsResoureModel>? = null
        var pictures: List<String>? = null
        var videos: List<PostsResoureModel>? = null
        var songInfo: FeedSongModel? = null
    }
}
