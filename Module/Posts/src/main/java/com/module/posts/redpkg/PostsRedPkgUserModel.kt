package com.module.posts.redpkg

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import java.io.Serializable

class PostsRedPkgUserModel : Serializable {
    @JSONField(name = "redpacketDesc")
    var redpacketDesc: String = ""  // 金额描述
    @JSONField(name = "redpacketNum")
    var redpacketNum: Int = 0
    @JSONField(name = "redpacketType")
    var redpacketType: Int = 0    // 0未知 1金币红包 2钻石红包
    @JSONField(name = "user")
    var userModel: UserInfoModel? = null

    override fun toString(): String {
        return "PostsRedPkgUserModel(redpacketDesc='$redpacketDesc', redpacketNum=$redpacketNum, redpacketType=$redpacketType, userModel=$userModel)"
    }

}