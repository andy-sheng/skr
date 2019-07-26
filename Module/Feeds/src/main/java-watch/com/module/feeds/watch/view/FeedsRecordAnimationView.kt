package com.module.feeds.watch.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import com.common.core.avatar.AvatarUtils
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.module.feeds.R

class FeedsRecordAnimationView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    val TAG = "FeedsRecordAnimationView"
    var panelDrawable: Drawable? = null
    var panelWidth: Float? = 0f
    var panelMarginTop: Float? = 0f
    var rockerDrawable: Drawable? = null
    var rockerWidth: Float? = 0f
    var rockerHeight: Float? = 0f
    var rockerXP: Float? = 0.5f
    var rockerYP: Float? = 0.1f
    var avatorMargin: Float? = 0f

    val avatarContainer: ConstraintLayout
    val panelIv: ImageView
    val avatarIv: BaseImageView
    val rockerIv: ImageView

    var avatarAnimation: ObjectAnimator? = null

    val rotateAnimationPlay: RotateAnimation
    val rotateAnimationStop: RotateAnimation

    var playing = false
    var hasLayouted = false
    var wantPlaying = false

    init {
        View.inflate(context, R.layout.feed_record_animation_view_layout, this)

        avatarContainer = this.findViewById(R.id.avatar_container)
        panelIv = this.findViewById(R.id.panel_iv)
        avatarIv = this.findViewById(R.id.avatar_iv)
        rockerIv = this.findViewById(R.id.rocker_iv)


        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FeedsRecordAnimationView)
        panelDrawable = typedArray.getDrawable(R.styleable.FeedsRecordAnimationView_panelDrawable)
        panelIv.setImageDrawable(panelDrawable)
        rockerDrawable = typedArray.getDrawable(R.styleable.FeedsRecordAnimationView_rockerDrawable)
        rockerIv.setImageDrawable(rockerDrawable)

        panelMarginTop = typedArray.getDimension(R.styleable.FeedsRecordAnimationView_panelMarginTop, 0f)
        (avatarContainer?.layoutParams as LayoutParams).topMargin = panelMarginTop?.toInt() ?: 0

        val avatarWidth = typedArray.getDimension(R.styleable.FeedsRecordAnimationView_avatarWidth, 40f)
        (avatarIv?.layoutParams as LayoutParams).apply {
            width = avatarWidth.toInt()
            height = avatarWidth.toInt()
        }
        rockerXP = typedArray.getFloat(R.styleable.FeedsRecordAnimationView_rockerXP, 0.5f)
        rockerYP = typedArray.getFloat(R.styleable.FeedsRecordAnimationView_rockerYP, 0f)

        rotateAnimationPlay = RotateAnimation(-55f, -25f, Animation.RELATIVE_TO_SELF, rockerXP!!, Animation.RELATIVE_TO_SELF, rockerYP!!)
        rotateAnimationPlay.duration = 800
        rotateAnimationPlay.repeatCount = 0
        rotateAnimationPlay.fillAfter = true

        rotateAnimationStop = RotateAnimation(-25f, -55f, Animation.RELATIVE_TO_SELF, rockerXP!!, Animation.RELATIVE_TO_SELF, rockerYP!!)
        rotateAnimationStop.duration = 800
        rotateAnimationPlay.repeatCount = 0
        rotateAnimationStop.fillAfter = true

        avatarAnimation = ObjectAnimator.ofFloat(avatarContainer, View.ROTATION, 0f, 360f)
        avatarAnimation?.duration = 10000
        avatarAnimation?.interpolator = LinearInterpolator()
        avatarAnimation?.repeatCount = Animation.INFINITE
    }

    fun setAvatar(url: String) {
        AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(url)
                .setCircle(true)
                .build())
    }

    fun play() {
        MyLog.d(TAG, "play")
        wantPlaying = true
        if (playing) {
            return
        }

        if (!hasLayouted) {
            // 界面还没渲染出来，不播放
            return
        }
        playing = true
        rockerIv?.clearAnimation()
        rockerIv?.startAnimation(rotateAnimationPlay)

        avatarAnimation?.let {
            if (it.isStarted) {
                it.resume()
            } else {
                avatarContainer?.pivotX = avatarContainer.width / 2f
                avatarContainer?.pivotY = avatarContainer.height / 2f
                it.start()
            }
        }
    }

    fun pause() {
        if (!playing) {
            return
        }
        playing = false
        wantPlaying = false
        rockerIv?.clearAnimation()
        rockerIv?.startAnimation(rotateAnimationStop)
        avatarAnimation?.pause()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (!hasLayouted) {
            hasLayouted = true
            if (wantPlaying) {
                play()
            } else {
                pause()
            }
        }

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        avatarAnimation?.cancel()
        rockerIv?.clearAnimation()
    }

}