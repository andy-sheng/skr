package com.module.playways.mic.room.view.normal

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ExViewStub
import com.module.playways.R
import com.module.playways.listener.SVGAListener
import com.module.playways.room.data.H

/**
 * 轮次结束 合唱和正常结束都用此板
 */
class MicNormalSingBeginTipsCardView(viewStub: ViewStub) : ExViewStub(viewStub) {
    val TAG = "MicNormalSingBeginTipsCardView"
    lateinit var cardBgIv: ImageView
    lateinit var avatarIv: BaseImageView
    lateinit var singerNameTv: TextView
    lateinit var songNameTv: TextView

    var anim: AnimatorSet? = null

    var overListener: SVGAListener? = null

    override fun init(rootView: View) {
        cardBgIv = rootView.findViewById(R.id.card_bg_iv)
        avatarIv = rootView.findViewById(R.id.avatar_iv)
        singerNameTv = rootView.findViewById(R.id.singer_name_tv)
        songNameTv = rootView.findViewById(R.id.song_name_tv)
    }

    override fun layoutDesc(): Int {
        return R.layout.mic_sing_begin_tips_card_stub_layout
    }

    fun bindData(overListener: SVGAListener) {
        this.overListener = overListener
        var b = tryInflate()
        songNameTv?.text = "演唱《${H.micRoomData?.realRoundInfo?.music?.displaySongName}》"
        var userInfo = H.micRoomData?.getPlayerOrWaiterInfo(H.micRoomData?.realRoundInfo?.userID)
        singerNameTv?.text = "${userInfo?.nicknameRemark}"

        AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(userInfo?.avatar)
                .setBorderWidth(2.dp().toFloat())
                .setCircle(true)
                .setBorderColor(R.color.white)
                .build())

        if (b) {
            startAnimation()
        } else {
            // 否则width 还是0
            this.setTranslateX(-U.getDisplayUtils().screenWidth.toFloat())
            realView?.post {
                startAnimation()
            }
        }
    }

    private fun startAnimation() {
        setVisibility(View.VISIBLE)
        MyLog.d("MicNormalSingBeginTipsCardView", "this.realView!!.width=${this.realView!!.width.toFloat()}")
        val anim1 = ObjectAnimator.ofFloat(this.realView!!, View.TRANSLATION_X, -this.realView!!.width.toFloat(), 0f)
        anim1?.duration = 500

        val anim2 = ObjectAnimator.ofFloat(this.realView!!, View.TRANSLATION_X, 0f, this.realView!!.width.toFloat())
        anim2?.startDelay = 1500
        anim2?.duration = 500

        anim = AnimatorSet()
        anim?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                overListener?.onFinished()
                overListener = null
            }

            override fun onAnimationCancel(animation: Animator?) {
                super.onAnimationCancel(animation)
//                overListener?.onFinished()
//                overListener = null
            }

            override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                super.onAnimationStart(animation, isReverse)
                setVisibility(View.VISIBLE)
            }
        })
        anim?.playSequentially(anim1, anim2)
        anim?.start()
    }


    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            anim?.removeAllListeners()
            if (anim?.isRunning == true) {
                MyLog.d(TAG, "cancel")
                anim?.cancel()
            }
        }
    }
}
