package com.module.playways.battle.room.view

import android.graphics.Color
import android.view.View
import android.view.ViewStub
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ExViewStub
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.listener.AnimationListener

// 轮次结束
class BattleRoundOverCardView(viewStub: ViewStub) : ExViewStub(viewStub) {

    lateinit var bgIv: ImageView
    lateinit var avatarIv: SimpleDraweeView
    lateinit var lossIv: ImageView
    lateinit var winIv: ImageView
    lateinit var winTv: TextView

    private var enterScaleAnimation: ScaleAnimation? = null // 飞入的进场动画
    private var leaveScaleAnimation: ScaleAnimation? = null // 飞出的离场动画

    var listener: AnimationListener? = null

    override fun init(parentView: View) {
        bgIv = parentView.findViewById(R.id.bg_iv)
        avatarIv = parentView.findViewById(R.id.avatar_iv)
        lossIv = parentView.findViewById(R.id.loss_iv)
        winIv = parentView.findViewById(R.id.win_iv)
        winTv = parentView.findViewById(R.id.win_tv)
    }

    override fun layoutDesc(): Int {
        return R.layout.battle_round_over_card_view
    }

    fun bindData(listener: AnimationListener?) {
        this.listener = listener
        tryInflate()
        // todo 待补全,填充view的信息,先给个测试信息
        val isWin = true;
        if (isWin) {
            bgIv.background = U.getDrawable(R.drawable.battle_round_win_icon)
            winIv.visibility = View.VISIBLE
            winTv.visibility = View.VISIBLE
            lossIv.visibility = View.GONE
            winTv.text = "+1 分"
//            winIv.background = U.getDrawable(R.drawable.battle_pretty_text)
//            winIv.background = U.getDrawable(R.drawable.battle_perfect_text)
        } else {
            bgIv.background = U.getDrawable(R.drawable.battle_round_loss_icon)
            winIv.visibility = View.GONE
            winTv.visibility = View.GONE
            lossIv.visibility = View.VISIBLE
        }
        AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.avatar)
                .setCircle(true)
                .setBorderColor(Color.WHITE)
                .setBorderWidth(1.dp().toFloat())
                .build())

        animationEnter()
        mParentView?.postDelayed({
            animationLeave()
        }, 1200)
    }

    // 入场动画
    private fun animationEnter() {
        if (enterScaleAnimation == null) {
            enterScaleAnimation = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f)
            enterScaleAnimation?.duration = 200
        }
        setVisibility(View.INVISIBLE)
        enterScaleAnimation?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
            }

            override fun onAnimationStart(animation: Animation?) {
                setVisibility(View.VISIBLE)
            }
        })
        mParentView?.startAnimation(enterScaleAnimation)
    }

    // 离场动画
    private fun animationLeave() {
        if (this != null && mParentView?.visibility == View.VISIBLE) {
            if (leaveScaleAnimation == null) {
                leaveScaleAnimation = ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f)
                leaveScaleAnimation?.duration = 200
            }
            leaveScaleAnimation?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    mParentView?.clearAnimation()
                    setVisibility(View.GONE)
                    listener?.onFinish()
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            mParentView?.startAnimation(leaveScaleAnimation)
        } else {
            mParentView?.clearAnimation()
            setVisibility(View.GONE)
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            enterScaleAnimation?.setAnimationListener(null)
            enterScaleAnimation?.cancel()

            leaveScaleAnimation?.setAnimationListener(null)
            leaveScaleAnimation?.cancel()
        }
    }
}