package com.module.playways.race.room.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import com.common.core.avatar.AvatarUtils
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.utils.U
import com.module.playways.R
import com.module.playways.RoomDataUtils
import com.module.playways.race.room.RaceRoomData
import com.module.playways.room.room.gift.GiftBigContinuousView
import com.module.playways.room.room.gift.model.GiftPlayModel

class RaceBigContinuousView : GiftBigContinuousView {
    var mRoomData: RaceRoomData? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun play(model: GiftPlayModel): Boolean {
        mCurGiftPlayModel = model
        AvatarUtils.loadAvatarByUrl(mSendAvatarIv, AvatarUtils.newParamsBuilder(RoomDataUtils.getRaceDisplayAvatar(mRoomData, model.sender))
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                .setBorderColor(Color.WHITE)
                .build())

        mSenderNameTv.text = model.sender.nicknameRemark
        mDescTv.text = model.action

        if (model.eGiftType == GiftPlayModel.EGiftType.GIFT) {
            FrescoWorker.loadImage(mGiftImgIv, ImageFactory.newPathImage(model.giftIconUrl)
                    .setLoadingDrawable(U.getDrawable(R.drawable.skrer_logo))
                    .setFailureDrawable(U.getDrawable(R.drawable.skrer_logo))
                    .setWidth(U.getDisplayUtils().dip2px(45f))
                    .setHeight(U.getDisplayUtils().dip2px(45f))
                    .build())

            mSenderNameTv.text = RoomDataUtils.getRaceDisplayNickName(mRoomData, model.sender)
            mDescTv.text = "送给 " + RoomDataUtils.getRaceDisplayNickName(mRoomData, model.receiver)
            mDescTv.visibility = View.VISIBLE
        }

        return true
    }

    fun bindData(roomData: RaceRoomData) {
        this.mRoomData = roomData
    }
}