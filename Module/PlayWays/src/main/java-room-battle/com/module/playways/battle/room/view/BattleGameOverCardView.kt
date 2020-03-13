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
import com.module.playways.room.data.H

// 比赛结束
class BattleGameOverCardView(viewStub: ViewStub) : ExViewStub(viewStub) {

    val mTag = "BattleGameOverCardView${hashCode()}"

    lateinit var bgIv: ImageView
    lateinit var vsIv: ImageView
    lateinit var leftAvatar1: SimpleDraweeView
    lateinit var leftAvatar2: SimpleDraweeView
    lateinit var leftScoreTv: TextView
    lateinit var rightAvatar2: SimpleDraweeView
    lateinit var rightAvatar1: SimpleDraweeView
    lateinit var rightScoreTv: TextView
    lateinit var lossIv: ImageView
    lateinit var winIv: ImageView

    private var enterScaleAnimation: ScaleAnimation? = null // 飞入的进场动画
    private var leaveScaleAnimation: ScaleAnimation? = null // 飞出的离场动画

    var listener: AnimationListener? = null

    override fun init(parentView: View) {
        U.getSoundUtils().preLoad(mTag, R.raw.battle_win, R.raw.battle_draw, R.raw.battle_loss)

        bgIv = parentView.findViewById(R.id.bg_iv)
        vsIv = parentView.findViewById(R.id.vs_iv)
        leftAvatar1 = parentView.findViewById(R.id.left_avatar_1)
        leftAvatar2 = parentView.findViewById(R.id.left_avatar_2)
        leftScoreTv = parentView.findViewById(R.id.left_score_tv)
        rightAvatar2 = parentView.findViewById(R.id.right_avatar_2)
        rightAvatar1 = parentView.findViewById(R.id.right_avatar_1)
        rightScoreTv = parentView.findViewById(R.id.right_score_tv)
        lossIv = parentView.findViewById(R.id.loss_iv)
        winIv = parentView.findViewById(R.id.win_iv)
    }

    override fun layoutDesc(): Int {
        return R.layout.battle_game_over_card_view
    }

    fun bindData(listener: AnimationListener?) {
        this.listener = listener
        tryInflate()

        if (H.battleRoomData?.myTeamScore ?: 0 >= (H.battleRoomData?.opTeamScore ?: 0)) {
            bgIv.background = U.getDrawable(R.drawable.battle_game_over_win)
            winIv.visibility = View.VISIBLE
            lossIv.visibility = View.GONE
            if (H.battleRoomData?.myTeamScore ?: 0 == (H.battleRoomData?.opTeamScore ?: 0)) {
                U.getSoundUtils().play(mTag, R.raw.battle_draw)
                winIv.background = U.getDrawable(R.drawable.battle_game_over_draw_text)
            } else {
                U.getSoundUtils().play(mTag, R.raw.battle_win)
                winIv.background = U.getDrawable(R.drawable.battle_game_over_win_text)
            }
        } else {
            U.getSoundUtils().play(mTag, R.raw.battle_loss)
            bgIv.background = U.getDrawable(R.drawable.battle_game_over_loss)
            winIv.visibility = View.GONE
            lossIv.visibility = View.VISIBLE
        }

        AvatarUtils.loadAvatarByUrl(leftAvatar1, AvatarUtils.newParamsBuilder(MyUserInfoManager.avatar)
                .setCircle(true)
                .setBorderColor(Color.WHITE)
                .setBorderWidth(1.dp().toFloat())
                .build())
        AvatarUtils.loadAvatarByUrl(leftAvatar2, AvatarUtils.newParamsBuilder(H.battleRoomData?.getFirstTeammate()?.userInfo?.avatar)
                .setCircle(true)
                .setBorderColor(Color.WHITE)
                .setBorderWidth(1.dp().toFloat())
                .build())
        if ((H.battleRoomData?.opTeamInfo?.size ?: 0) > 0) {
            AvatarUtils.loadAvatarByUrl(rightAvatar1, AvatarUtils.newParamsBuilder(H.battleRoomData?.opTeamInfo?.get(0)?.userInfo?.avatar)
                    .setCircle(true)
                    .setBorderColor(Color.WHITE)
                    .setBorderWidth(1.dp().toFloat())
                    .build())
        }
        if ((H.battleRoomData?.opTeamInfo?.size ?: 0) > 1) {
            AvatarUtils.loadAvatarByUrl(rightAvatar2, AvatarUtils.newParamsBuilder(H.battleRoomData?.opTeamInfo?.get(1)?.userInfo?.avatar)
                    .setCircle(true)
                    .setBorderColor(Color.WHITE)
                    .setBorderWidth(1.dp().toFloat())
                    .build())
        }
        leftScoreTv.text = H.battleRoomData?.myTeamScore?.toString()
        rightScoreTv.text = H.battleRoomData?.opTeamScore?.toString()

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

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        U.getSoundUtils().release(mTag)
    }
}