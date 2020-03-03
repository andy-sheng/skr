package com.module.playways.battle.room.view

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.utils.dp
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.listener.AnimationListener

// 对战开始 出现，从底部中心点放大加抖动  隐藏，直接收缩回到底部中心点
class BattleBeginTipsView : ConstraintLayout {

    val TAG = "BattleBeginTipsView"

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val leftAvatar1: SimpleDraweeView
    private val leftAvatar2: SimpleDraweeView
    private val rightAvatar2: SimpleDraweeView
    private val rightAvatar1: SimpleDraweeView

    private var enterScaleAnimation: ScaleAnimation? = null // 飞入的进场动画
    private var leaveScaleAnimation: ScaleAnimation? = null // 飞出的离场动画

    var listener: AnimationListener? = null

    init {
        View.inflate(context, R.layout.battle_begin_tips_view, this);

        leftAvatar1 = this.findViewById(R.id.left_avatar_1)
        leftAvatar2 = this.findViewById(R.id.left_avatar_2)
        rightAvatar2 = this.findViewById(R.id.right_avatar_2)
        rightAvatar1 = this.findViewById(R.id.right_avatar_1)
    }

    fun showAnimation(listener: AnimationListener?) {
        this.listener = listener
        bindData()
        animationEnter()
        leftAvatar1.postDelayed({
            animationLeave()
        }, 1200)
    }

    private fun bindData() {
        // todo 测试而已
        AvatarUtils.loadAvatarByUrl(leftAvatar1, AvatarUtils.newParamsBuilder(MyUserInfoManager.avatar)
                .setCircle(true)
                .setBorderColor(Color.WHITE)
                .setBorderWidth(1.dp().toFloat())
                .build())
        AvatarUtils.loadAvatarByUrl(leftAvatar2, AvatarUtils.newParamsBuilder(MyUserInfoManager.avatar)
                .setCircle(true)
                .setBorderColor(Color.WHITE)
                .setBorderWidth(1.dp().toFloat())
                .build())
        AvatarUtils.loadAvatarByUrl(rightAvatar1, AvatarUtils.newParamsBuilder(MyUserInfoManager.avatar)
                .setCircle(true)
                .setBorderColor(Color.WHITE)
                .setBorderWidth(1.dp().toFloat())
                .build())
        AvatarUtils.loadAvatarByUrl(rightAvatar2, AvatarUtils.newParamsBuilder(MyUserInfoManager.avatar)
                .setCircle(true)
                .setBorderColor(Color.WHITE)
                .setBorderWidth(1.dp().toFloat())
                .build())
    }

    // 入场动画
    private fun animationEnter() {
        if (enterScaleAnimation == null) {
            enterScaleAnimation = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f)
            enterScaleAnimation?.duration = 200
        }
//        enterScaleAnimation?.interpolator = BounceInterpolator()
        visibility = View.INVISIBLE
        enterScaleAnimation?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
            }

            override fun onAnimationStart(animation: Animation?) {
                visibility = View.VISIBLE
            }
        })
        this.startAnimation(enterScaleAnimation)
    }

    // 离场动画
    private fun animationLeave() {
        if (this != null && this.visibility == View.VISIBLE) {
            if (leaveScaleAnimation == null) {
                leaveScaleAnimation = ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f)
                leaveScaleAnimation?.duration = 200
            }
            leaveScaleAnimation?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    clearAnimation()
                    visibility = View.GONE
                    listener?.onFinish()
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            this.startAnimation(leaveScaleAnimation)
        } else {
            clearAnimation()
            visibility = View.GONE
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        enterScaleAnimation?.setAnimationListener(null)
        enterScaleAnimation?.cancel()

        leaveScaleAnimation?.setAnimationListener(null)
        leaveScaleAnimation?.cancel()
    }
}