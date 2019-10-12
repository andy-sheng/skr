package com.module.playways.grab.special

import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField


class GrabTagDetailModel : Serializable {

    companion object {
        const val SST_LOCK = 1    //待开启
        const val SST_UNLOCK = 2  //已开启
    }

    @JSONField(name = "tagID")
    var tagID: Int = 0
    @JSONField(name = "coverURL")
    var coverURL: String? = null
    @JSONField(name = "tagName")
    var tagName: String = ""
    @JSONField(name = "starCnt")
    var starCnt: Int = 0
    @JSONField(name = "status")
    var status: Int? = null
    @JSONField(name = "onlineUserCnt")
    var onlineUserCnt: Int? = null
    @JSONField(name = "rankInfoDesc")
    var rankInfoDesc: String? = null
    @JSONField(name = "showPermissionLock")
    var showPermissionLock: Boolean? = null
}