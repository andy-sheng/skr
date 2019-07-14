package com.module.playways.doubleplay.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.model.UserInfoModel
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.doubleplay.pbLocalModel.LocalGameItemInfo
import java.util.*

class DoubleGameSelectCardView : ConstraintLayout {
    val mGameNameTv: ExTextView
    val mIconIv1: BaseImageView
    val mIconIv2: BaseImageView

    val userInfoListMap: HashMap<Int, UserInfoModel> = HashMap()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var itemId: Int? = -1

    init {
        inflate(context, com.module.playways.R.layout.double_gameselect_card_layout, this)
        mGameNameTv = findViewById(com.module.playways.R.id.game_name_tv)
        mIconIv1 = findViewById(com.module.playways.R.id.icon_iv_1)
        mIconIv2 = findViewById(com.module.playways.R.id.icon_iv_2)
    }

    fun acceptItem(itemId: Int): Boolean {
        return itemId == -1 || this.itemId == itemId
    }

    fun setItemData(localGameItemInfo: LocalGameItemInfo) {
        reset()
        if (itemId != localGameItemInfo.itemID) {
            mGameNameTv.text = localGameItemInfo.itemDesc
            itemId = localGameItemInfo.itemID
        }
    }

    fun reset() {
        userInfoListMap.clear()
        itemId = -1
        mGameNameTv.text = ""
        mIconIv1.visibility = View.GONE
        mIconIv2.visibility = View.GONE
    }

    fun setSelectUser(userInfoModel: UserInfoModel) {
        if (userInfoListMap[userInfoModel.userId] == null) {
            when (userInfoListMap.size) {
                0 -> {
                    AvatarUtils.loadAvatarByUrl(mIconIv1,
                            AvatarUtils.newParamsBuilder(userInfoModel.getAvatar())
                                    .setBorderColor(U.getColor(R.color.white))
                                    .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                                    .setCircle(true)
                                    .build())
                    mIconIv1.visibility = View.VISIBLE
                    userInfoListMap[userInfoModel.userId] = userInfoModel
                }

                1 -> {
                    AvatarUtils.loadAvatarByUrl(mIconIv2,
                            AvatarUtils.newParamsBuilder(userInfoModel.getAvatar())
                                    .setBorderColor(U.getColor(R.color.white))
                                    .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                                    .setCircle(true)
                                    .build())
                    mIconIv2.visibility = View.VISIBLE
                    userInfoListMap[userInfoModel.userId] = userInfoModel
                }
                else -> MyLog.w("DoubleGameSelectCardView", "什么情况")
            }
        }
    }
}