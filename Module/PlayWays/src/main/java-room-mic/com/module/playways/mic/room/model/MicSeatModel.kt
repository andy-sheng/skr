package com.module.playways.mic.room.model

import android.databinding.BindingAdapter
import com.alibaba.fastjson.annotation.JSONField
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.image.fresco.BaseImageView

class MicSeatModel {
    @JSONField(name = "music")
    var music: List<SeatMusicInfo>? = null
    @JSONField(name = "user")
    var user: UserInfoModel? = null

    data class SeatMusicInfo(val itemID: Int, val itemName: String)
}