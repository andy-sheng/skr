package com.module.msg.follow

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import java.io.Serializable

class SPFollowRecordModel : Serializable {
    @JSONField(name = "spFollowInfo")
    var spFollowInfo: SPFollowInfo? = null
    @JSONField(name = "userInfo")
    var userInfo: UserInfoModel? = null
}


class SPFollowInfo : Serializable {
    companion object {
        const val ELNT_POST = 1   // 发帖子
        const val ELNT_ALBUM = 2  // 传照片
        const val ELNT_LOGIN = 3  // 登陆（记录里面应该没有）
    }

    @JSONField(name = "actionDesc")
    var actionDesc: String? = null
    @JSONField(name = "content")
    var content: String? = null
    @JSONField(name = "nType")
    var nType: Int = 0
    @JSONField(name = "postsID")
    var postsID: Int = 0
    @JSONField(name = "timeMs")
    var timeMs: Long = 0L
    @JSONField(name = "userID")
    var userID: Int = 0

}