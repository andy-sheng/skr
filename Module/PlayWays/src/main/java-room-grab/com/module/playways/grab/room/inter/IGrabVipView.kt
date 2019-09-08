package com.module.playways.grab.room.inter

import com.common.core.userinfo.model.UserInfoModel

interface IGrabVipView {
    fun startEnterAnimation(playerInfoModel: UserInfoModel, finishCall: (() -> Unit))
}
