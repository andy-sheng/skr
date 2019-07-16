package com.module.playways.doubleplay.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import com.common.core.userinfo.model.UserInfoModel
import com.common.view.ex.ExConstraintLayout
import com.module.playways.doubleplay.pbLocalModel.LocalGamePanelInfo


class DoubleGameCardGroupView : ExConstraintLayout, Animation.AnimationListener {
    val mCard1: DoubleGameSelectCardView
    val mCard2: DoubleGameSelectCardView
    val mCard3: DoubleGameSelectCardView
    val mCard4: DoubleGameSelectCardView

    var panelId: Int? = null

    val mUiHandler = Handler(Looper.getMainLooper())

    var mAnimate: Animation? = null

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

    override fun onAnimationEnd(animation: Animation?) {
        applyRotation(this, 90f, 0f, false)
    }

    override fun onAnimationStart(animation: Animation?) {

    }

    override fun onAnimationRepeat(animation: Animation?) {

    }

    fun setPanelInfo(localGamePanelInfo: LocalGamePanelInfo) {
        if (panelId != localGamePanelInfo.panelSeq) {
            clearAnimation()
            panelId = localGamePanelInfo.panelSeq
            applyRotation(this, 0f, -90f, true)
            mUiHandler.postDelayed({
                repeat(4) {
                    val info = localGamePanelInfo.items[it]
                    when (it) {
                        0 -> mCard1.setItemData(info)
                        1 -> mCard2.setItemData(info)
                        2 -> mCard3.setItemData(info)
                        3 -> mCard4.setItemData(info)
                    }
                }
            }, 300)

            mUiHandler.postDelayed({ clearAnimation() }, 620)
        }
    }

    /**
     * @param view
     * @param start
     * @param end
     * @param reverse
     */
    fun applyRotation(view: View, start: Float, end: Float, reverse: Boolean) {
        // 计算中心点
        val centerX = view.width / 2.0f;
        val centerY = view.height / 2.0f;
        mAnimate = Rotate3dAnimation(start, end,
                centerX, centerY, 300.0f, reverse)
        mAnimate?.duration = 300
        mAnimate?.fillAfter = true
        if (reverse)
            mAnimate?.setAnimationListener(this)
        view.startAnimation(mAnimate)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mAnimate?.cancel()
    }

}