package com.module.playways.doubleplay.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.ScaleAnimation
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.pbLocalModel.LocalGameItemInfo
import com.zq.live.proto.Common.EGameType


class DoubleGameSelectCardView : ExConstraintLayout {
    val mGameNameTv: ExTextView
    val mGameTypeTv: ExTextView
    val mIconIv1: BaseImageView
    val mIconIv2: BaseImageView
    var mRoomData: DoubleRoomData? = null
    val userInfoListMap: LinkedHashMap<Int, UserInfoModel> = LinkedHashMap()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var itemId: Int? = -1

    init {
        inflate(context, com.module.playways.R.layout.double_gameselect_card_layout, this)
        mGameNameTv = findViewById(com.module.playways.R.id.game_name_tv)
        mIconIv1 = findViewById(com.module.playways.R.id.icon_iv_1)
        mIconIv2 = findViewById(com.module.playways.R.id.icon_iv_2)
        mGameTypeTv = findViewById(com.module.playways.R.id.gameType_tv)
    }

    fun acceptItem(itemId: Int): Boolean {
        return itemId == -1 || this.itemId == itemId
    }

    fun setItemData(localGameItemInfo: LocalGameItemInfo) {
        reset()
        if (itemId != localGameItemInfo.itemID) {
            mGameNameTv.text = localGameItemInfo.desc
            itemId = localGameItemInfo.itemID
            mGameTypeTv.text = if (localGameItemInfo.gameType == EGameType.GT_Music.value) "唱歌" else "问答"
        }
    }

    fun reset() {
        userInfoListMap.clear()
        itemId = -1
        mGameNameTv.text = ""
        mIconIv1.visibility = View.GONE
        mIconIv2.visibility = View.GONE
    }

    fun updateLockState() {
        updateLockState(false)
    }

    fun updateLockState(animate: Boolean) {
        var index = 0
        mIconIv1.visibility = View.GONE
        mIconIv2.visibility = View.GONE
        val maxIndex = userInfoListMap.size - 1
        for (userInfoModel in userInfoListMap) {
            if (mRoomData != null) {
                when (index) {
                    0 -> {
                        AvatarUtils.loadAvatarByUrl(mIconIv1,
                                AvatarUtils.newParamsBuilder(mRoomData!!.getAvatarById(userInfoModel.key))
                                        .setBorderColor(U.getColor(com.module.playways.R.color.white))
                                        .setBorderWidth(U.getDisplayUtils().dip2px(1f).toFloat())
                                        .setCircle(true)
                                        .build())
                        mIconIv1.visibility = View.VISIBLE
                        if (maxIndex == 0) {
                            startAnimation(mIconIv1)
                        }
                    }

                    1 -> {
                        AvatarUtils.loadAvatarByUrl(mIconIv2,
                                AvatarUtils.newParamsBuilder(mRoomData!!.getAvatarById(userInfoModel.key))
                                        .setBorderColor(U.getColor(com.module.playways.R.color.white))
                                        .setBorderWidth(U.getDisplayUtils().dip2px(1f).toFloat())
                                        .setCircle(true)
                                        .build())
                        mIconIv2.visibility = View.VISIBLE
                        if (maxIndex == 1) {
                            startAnimation(mIconIv2)
                        }
                    }
                }
            }
            index++
        }
    }

    private fun startAnimation(view: View) {
        val animation = ScaleAnimation(1.2f, 1.0f, 1.2f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        animation?.setDuration(300)
        animation.interpolator = OvershootInterpolator()
        view.startAnimation(animation)
    }

    fun getSelect(): Boolean {
        return userInfoListMap[MyUserInfoManager.getInstance().uid.toInt()] == null
    }

    fun setSelectUser(userInfoModel: UserInfoModel, roomData: DoubleRoomData, isChoiced: Boolean) {
        mRoomData = roomData
        if (isChoiced) {
            if (userInfoListMap[userInfoModel.userId] == null) {
                userInfoListMap[userInfoModel.userId] = userInfoModel
                updateLockState(true)
            }
        } else {
            if (userInfoListMap[userInfoModel.userId] != null) {
                userInfoListMap.remove(userInfoModel.userId)
                updateLockState(false)
            }
        }
    }
}