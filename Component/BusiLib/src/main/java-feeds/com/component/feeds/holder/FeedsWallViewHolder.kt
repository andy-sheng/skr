package com.component.feeds.holder

import android.view.View
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.U
import com.component.feeds.listener.FeedsListener
import com.component.feeds.model.FeedsWatchModel

class FeedsWallViewHolder(it: View, l: FeedsListener) : FeedViewHolder(it, l) {

    init {
        AvatarUtils.loadAvatarByUrl(mSongAreaBg, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                .setBlur(true)
                .build())
        mRecordView.bindData(MyUserInfoManager.getInstance().avatar)
    }

    override fun bindData(position: Int, watchModel: FeedsWatchModel) {
        super.bindData(position, watchModel)
        this.model = watchModel
    }
}