package com.module.playways.grab.special

import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField
import com.component.busilib.friends.TagImageModel
import com.module.playways.battle.room.model.MilitaryInfoModel


class GrabTagDetailModel : Serializable {

    companion object {
        const val SST_LOCK = 1    //待开启
        const val SST_UNLOCK = 2  //已开启

        const val TYPE_AUDIO = 1  //音频专场
        const val TYPE_VIDEO = 2  //视频专场

        const val TAG_TYPE_GRAB = 1    // 抢唱专场
        const val TAG_TYPE_BATTLE = 2  // 2V2专场
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
    var onlineUserCnt: Int = 0
    @JSONField(name = "rankInfoDesc")
    var rankInfoDesc: String? = null
    @JSONField(name = "showPermissionLock")
    var showPermissionLock: Boolean = false
    @JSONField(name = "tagType")
    var tagType: Int = 0

    @JSONField(name = "cardTitle")
    var cardTitle: TagImageModel? = null  //卡片的文字
    @JSONField(name = "cardBg")
    var cardBg: TagImageModel? = null // 卡片的背景
    @JSONField(name = "icon")
    var icon: TagImageModel? = null // 右边的图片

    @JSONField(name = "militaryTitleState")
    var militaryModel: MilitaryInfoModel? = null
    @JSONField(name = "tagDetailType")
    var tagDetailType: Int = 0
}