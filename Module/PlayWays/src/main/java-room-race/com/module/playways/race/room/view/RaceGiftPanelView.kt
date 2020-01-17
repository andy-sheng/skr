package com.module.playways.race.room.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.U
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData
import com.module.playways.room.gift.view.GiftPanelView

class RaceGiftPanelView : GiftPanelView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setSelectArea(playerInfoModel: UserInfoModel?) {
        mAllPlayersTv.visibility = View.GONE
        if (playerInfoModel != null && playerInfoModel.userId.toLong() != MyUserInfoManager.uid) {
            mRlPlayerSelectArea.visibility = View.VISIBLE
            selectSendGiftPlayer(playerInfoModel)
        } else {
            mRlPlayerSelectArea.visibility = View.GONE
        }

        mGiftAllPlayersAdapter.dataList = playerInfoListExpectSelf
    }

    override fun bindSelectedPlayerData() {
        if (mCurMicroMan == null) {
            return
        }

        val racePlayerInfoModel = (mRoomData as RaceRoomData).getPlayerOrWaiterInfoModel(mCurMicroMan?.userId)
        if ((mRoomData as RaceRoomData)?.isFakeForMe(mCurMicroMan?.userId)) {
            mFollowTv.visibility = View.GONE
            AvatarUtils.loadAvatarByUrl(mIvSelectedIcon,
                    AvatarUtils.newParamsBuilder(racePlayerInfoModel?.fakeUserInfo?.avatarUrl)
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                            .setCircle(true)
                            .build())
        } else {
            super.bindSelectedPlayerData()
        }

        mTvSelectedName.text = racePlayerInfoModel?.fakeUserInfo?.nickName
    }
}