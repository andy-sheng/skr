package com.module.playways.mic.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.module.playways.R

// 右边操作区域，投票
class MicRightOpView : ConstraintLayout {

    val mTag = "RaceRightOpView"

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    val voteIv: ExImageView
    val giveUpIv: ExImageView
    val giveUpIvBg: ExImageView

    var mListener: RightOpListener? = null

    var mScaleAnimation: ScaleAnimation? = null

    init {
        View.inflate(context, R.layout.mic_right_op_view, this)

        voteIv = this.findViewById(R.id.vote_iv)
        giveUpIv = this.findViewById(R.id.give_up_iv)
        giveUpIvBg = this.findViewById(R.id.vote_bg)

        voteIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                if (voteIv.isSelected) {
                    U.getToastUtil().showShort("已经投过票了")
                } else {
                    // 投票
                    U.getSoundUtils().play(mTag, R.raw.newrank_vote)
                    mListener?.onClickVote()
                }
            }
        })

        giveUpIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                if (giveUpIv.isSelected) {
                    U.getToastUtil().showShort("已经放弃演唱了")
                } else {
                    // 放弃演唱
                    mListener?.onClickGiveUp()
                }
            }
        })

        U.getSoundUtils().preLoad(mTag, R.raw.newrank_vote)
    }

    fun showVote(isSelected: Boolean) {
        visibility = View.VISIBLE
        giveUpIv.visibility = View.GONE
        voteIv.visibility = View.VISIBLE


        if (isSelected) {
            playBgAnim {
                giveUpIvBg.visibility = View.INVISIBLE
                voteIv.isSelected = isSelected
                voteIv.isClickable = !isSelected
            }
        } else {
            voteIv.isSelected = isSelected
            voteIv.isClickable = !isSelected
        }
    }

    fun playBgAnim(call: () -> Unit) {
        giveUpIvBg.clearAnimation()
        giveUpIvBg.visibility = View.VISIBLE
        if (mScaleAnimation == null) {
            mScaleAnimation = ScaleAnimation(0.8f, 1.3f, 0.8f, 1.3f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            mScaleAnimation?.setDuration(300)
        }

        mScaleAnimation?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                call.invoke()
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })

        giveUpIvBg.startAnimation(mScaleAnimation)
    }

    fun showGiveUp(isSelected: Boolean) {
        visibility = View.VISIBLE
        voteIv.visibility = View.GONE
        giveUpIv.visibility = View.VISIBLE
        giveUpIv.isSelected = isSelected
        giveUpIv.isClickable = !isSelected
    }

    fun setListener(listener: RightOpListener) {
        mListener = listener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        U.getSoundUtils().release(mTag)
    }
}

interface RightOpListener {
    fun onClickVote()
    fun onClickGiveUp()
}