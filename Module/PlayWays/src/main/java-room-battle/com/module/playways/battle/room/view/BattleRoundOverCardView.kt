package com.module.playways.battle.room.view

import android.graphics.Color
import android.os.Handler
import android.os.Looper
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
import com.module.playways.battle.room.model.BattleRoundInfoModel
import com.module.playways.battle.room.model.BattleRoundResultModel
import com.module.playways.listener.AnimationListener
import com.module.playways.room.data.H
import com.zq.live.proto.BattleRoom.EChallengeResult
import com.zq.live.proto.BattleRoom.EChallengeTip

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

    var uiHandler = Handler(Looper.getMainLooper())

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

    fun bindData(lastRound: BattleRoundInfoModel, listener: AnimationListener?) {
        this.listener = listener
        tryInflate()

        val result = lastRound.result
        if (result?.challengeResult == EChallengeResult.ECR_SUCCESS.value) {
            bgIv.background = U.getDrawable(R.drawable.battle_round_win_icon)
            winIv.visibility = View.VISIBLE
            winTv.visibility = View.VISIBLE
            lossIv.visibility = View.GONE
            winTv.text = result.gameScore.toString()
            when {
                result.challengeTip == EChallengeTip.ECT_FEI_CHANG_BANG.value -> winIv.background = U.getDrawable(R.drawable.battle_pretty_text)
                result.challengeTip == EChallengeTip.ECT_HEN_YI_HAN.value -> winIv.background = U.getDrawable(R.drawable.battle_perfect_text)
                else -> winIv.visibility = View.GONE
            }
        } else {
            bgIv.background = U.getDrawable(R.drawable.battle_round_loss_icon)
            winIv.visibility = View.GONE
            winTv.visibility = View.GONE
            if (result?.challengeTip == EChallengeTip.ECT_HEN_YI_HAN.value) {
                lossIv.visibility = View.VISIBLE
            } else {
                lossIv.visibility = View.GONE
            }
        }

        AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(H.battleRoomData?.getPlayerInfoById(lastRound.userID)?.userInfo?.avatar)
                .setCircle(true)
                .setBorderColor(Color.WHITE)
                .setBorderWidth(1.dp().toFloat())
                .build())

        animationEnter()
        mParentView?.postDelayed({
            animationLeave()
        }, 2700)
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

            }
        })

        mParentView?.startAnimation(enterScaleAnimation)
        setVisibility(View.VISIBLE)

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
                    uiHandler.removeCallbacks(leaveAction)
                    uiHandler.postDelayed(leaveAction, 200)

                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            mParentView?.startAnimation(leaveScaleAnimation)
            val nLeaveScaleAnimation = leaveScaleAnimation?:return
            uiHandler.postDelayed(leaveAction, nLeaveScaleAnimation.duration + 200)
        } else {
            mParentView?.clearAnimation()
            setVisibility(View.GONE)
        }
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        uiHandler.removeCallbacksAndMessages(null)
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

    private  val leaveAction = Runnable {
        leaveScaleAnimation?.cancel()
        listener?.onFinish()
    }
}