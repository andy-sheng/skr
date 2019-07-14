package com.module.playways.doubleplay.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import com.common.core.userinfo.model.UserInfoModel
import com.common.view.ex.ExConstraintLayout
import com.module.playways.doubleplay.pbLocalModel.LocalGamePanelInfo


class DoubleGameCardGroupView : ExConstraintLayout {
    val mCard1: DoubleGameSelectCardView
    val mCard2: DoubleGameSelectCardView
    val mCard3: DoubleGameSelectCardView
    val mCard4: DoubleGameSelectCardView

    var panelId: Int? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    init {
        inflate(context, com.module.playways.R.layout.double_game_cardgroup_layout, this)
        mCard1 = findViewById(com.module.playways.R.id.card_1)
        mCard2 = findViewById(com.module.playways.R.id.card_2)
        mCard3 = findViewById(com.module.playways.R.id.card_3)
        mCard4 = findViewById(com.module.playways.R.id.card_4)
    }

    fun updateSelectState(userInfoModel: UserInfoModel, panelSeq: Int, itemID: Int) {
        if (this.panelId == panelSeq) {
            if (mCard1.acceptItem(itemID)) {
                mCard1.setSelectUser(userInfoModel)
            } else if (mCard2.acceptItem(itemID)) {
                mCard2.setSelectUser(userInfoModel)
            } else if (mCard3.acceptItem(itemID)) {
                mCard3.setSelectUser(userInfoModel)
            } else if (mCard4.acceptItem(itemID)) {
                mCard4.setSelectUser(userInfoModel)
            }
        }
    }

    fun reset() {

    }

    fun setPanelInfo(localGamePanelInfo: LocalGamePanelInfo) {
        if (panelId != localGamePanelInfo.panelSeq) {
            repeat(4) {
                val info = localGamePanelInfo.items[it]
                when (it) {
                    0 -> mCard1.setItemData(info)
                    1 -> mCard2.setItemData(info)
                    2 -> mCard3.setItemData(info)
                    3 -> mCard4.setItemData(info)
                }
            }
        }
    }
}