package com.module.playways.grab.room.view


import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.view.animation.TranslateAnimation
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * 转场时的歌曲信息页
 */
class VIPEnterView : ExConstraintLayout {
    val mTag = "VIPEnterView"
    val vipLevelIv: ExImageView
    val nameTv: ExTextView

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.vip_enter_view_layout, this)
        vipLevelIv = this.findViewById(R.id.vip_level_iv)
        nameTv = this.findViewById(R.id.name_tv)
    }

    fun enter(finishCall: (() -> Unit)?) {
        launch {
            val animationEnter = TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)

            animationEnter.duration = 500
            animationEnter.interpolator = OvershootInterpolator()
            startAnimation(animationEnter)

            delay(1000)

            val animationExit = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
            animationExit.interpolator = LinearInterpolator()
            animationExit.duration = 500
            startAnimation(animationExit)
            delay(500)
            finishCall?.invoke()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }
}

