package com.module.feeds.watch.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.common.core.avatar.AvatarUtils
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.player.SinglePlayer
import com.module.feeds.R

class FeedsRecordAnimationView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    var TAG = "FeedsRecordAnimationView"
    val AVATAR_ANIM = 0

    var panelDrawable: Drawable? = null
    var panelWidth: Float? = 0f
    var panelMarginTop: Float? = 0f
    var rockerDrawable: Drawable? = null
    var rockerWidth: Float? = 0f
    var rockerHeight: Float? = 0f
    var rockerXP: Float? = 0.5f
    var rockerYP: Float? = 0.1f
    var avatorMargin: Float? = 0f

    var startAngle: Float = 0f
    var endAngle: Float = 0f

    val avatarContainer: ConstraintLayout
    val panelIv: ImageView
    val avatarIv: BaseImageView
    val rockerIv: ImageView

    var avatarAnimation: ObjectAnimator? = null

    var rotateAnimationPlay: ObjectAnimator? = null
    var rotateAnimationStop: ObjectAnimator? = null

    var playing = false
    var hasLayouted = false
    var wantPlaying = false
    private var hideWhenPause = false

    val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            if (msg?.what == AVATAR_ANIM) {
                playAvatarAnim()
            }
        }
    }

    init {
        TAG += hashCode()
        View.inflate(context, R.layout.feed_record_animation_view_layout, this)

        avatarContainer = this.findViewById(R.id.avatar_container)
        panelIv = this.findViewById(R.id.panel_iv)
        avatarIv = this.findViewById(R.id.avatar_iv)
        rockerIv = this.findViewById(R.id.rocker_iv)


        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FeedsRecordAnimationView)
        hideWhenPause = typedArray.getBoolean(R.styleable.FeedsRecordAnimationView_hideWhenPause, false)
        startAngle = typedArray.getFloat(R.styleable.FeedsRecordAnimationView_startAngle, -55f)
        endAngle = typedArray.getFloat(R.styleable.FeedsRecordAnimationView_endAngle, -25f)

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

//        rotateAnimationPlay = RotateAnimation(-55f, -25f, Animation.RELATIVE_TO_SELF, rockerXP!!, Animation.RELATIVE_TO_SELF, rockerYP!!)
//        rotateAnimationPlay.duration = 800
//        rotateAnimationPlay.repeatCount = 0
//        rotateAnimationPlay.fillAfter = true

//        rotateAnimationPlay = ObjectAnimator.ofFloat(rockerIv,View.ROTATION,-55f, -25f)
//        rockerIv.pivotX = 1f
//        rockerIv.pivotY = 0f
//        rotateAnimationPlay.duration = 8000

//        rotateAnimationStop = RotateAnimation(-25f, -55f, Animation.RELATIVE_TO_SELF, rockerXP!!, Animation.RELATIVE_TO_SELF, rockerYP!!)
//        rotateAnimationStop.duration = 800
//        rotateAnimationPlay.repeatCount = 0
//        rotateAnimationStop.fillAfter = true

        avatarAnimation = ObjectAnimator.ofFloat(avatarContainer, View.ROTATION, 0f, 360f)
        avatarAnimation?.duration = 10000
        avatarAnimation?.interpolator = LinearInterpolator()
        avatarAnimation?.repeatCount = Animation.INFINITE


//        // 初始化棒棒到指定位置
//        val a = RotateAnimation(-25f, -55f, Animation.RELATIVE_TO_SELF, rockerXP!!, Animation.RELATIVE_TO_SELF, rockerYP!!)
//        a.duration = 100
//        a.repeatCount = 0
//        a.fillAfter = true
//        rockerIv?.clearAnimation()
//        rockerIv?.startAnimation(a)
    }

    fun setAvatar(url: String) {
        AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(url)
                .setCircle(true)
                .build())
    }

    fun play(isBufferingOk: Boolean) {
        MyLog.d(TAG, "play playing=$playing 动画在播 = ${avatarAnimation?.isRunning == true}, isBufferingOk is $isBufferingOk rotateAnimationPlay is $rotateAnimationPlay")
        mHandler.removeMessages(AVATAR_ANIM)
        wantPlaying = true
        if (playing && avatarAnimation?.isRunning == true) {
            return
        }

        if (!hasLayouted) {
            MyLog.d(TAG, "界面还没渲染出来，不播放")
            return
        }
        playing = true
        rotateAnimationStop?.pause()
        rotateAnimationPlay?.start()
        mHandler.sendEmptyMessageDelayed(AVATAR_ANIM, 800)
    }

    fun play() {
        play(true)
    }

    fun playAvatarAnim() {
        avatarAnimation?.let {
            if (SinglePlayer.isBufferingOk) {
                if (it.isStarted) {
                    it.resume()
                } else {
                    avatarContainer?.pivotX = avatarContainer.width / 2f
                    avatarContainer?.pivotY = avatarContainer.height / 2f
                    it.start()
                }
            }
        }
    }

    fun buffering() {
        MyLog.d(TAG,"buffering" )

        mHandler.removeMessages(AVATAR_ANIM)
        avatarAnimation?.pause()
    }

    fun bufferEnd() {
        MyLog.d(TAG,"bufferEnd" )
        if (!mHandler.hasMessages(AVATAR_ANIM)) {
            if (playing) {
                if (avatarAnimation!!.isPaused) {
                    avatarAnimation?.resume()
                } else {
                    if (!avatarAnimation!!.isRunning) {
                        avatarAnimation?.start()
                    }
                }
            }
        }
    }

    fun pause() {
        MyLog.d(TAG, "pause playing=$playing 动画在播= ${avatarAnimation?.isRunning != true}")
        mHandler.removeMessages(AVATAR_ANIM)
        if (!playing && avatarAnimation?.isRunning != true) {
            return
        }
        playing = false
        wantPlaying = false
        rotateAnimationPlay?.cancel()
        rotateAnimationStop?.start()
        avatarAnimation?.pause()
    }

    fun pauseWithNoAnimation() {
        mHandler.removeMessages(AVATAR_ANIM)
        MyLog.d(TAG, "pause playing=$playing 动画在播= ${avatarAnimation?.isRunning != true}")
        if (!playing && avatarAnimation?.isRunning != true) {
            return
        }
        playing = false
        wantPlaying = false
        rotateAnimationPlay?.cancel()
        if (rotateAnimationStop != null) {
            rotateAnimationStop?.cancel()
        } else {
            if (hideWhenPause) {
                rockerIv.visibility = View.GONE
            }
        }
        avatarAnimation?.pause()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        MyLog.d(TAG,"onLayout hasLayouted=$hasLayouted" )
        if (!hasLayouted) {
            hasLayouted = true
            rockerIv.pivotX = (rockerXP ?: 0f) * rockerIv.width.toFloat()
            rockerIv.pivotY = (rockerYP ?: 0f) * rockerIv.height.toFloat()
            if (rotateAnimationPlay == null) {
                rotateAnimationPlay = ObjectAnimator.ofFloat(rockerIv, View.ROTATION, startAngle, endAngle)
                rotateAnimationPlay?.duration = 800
                rotateAnimationPlay?.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                    }

                    override fun onAnimationCancel(animation: Animator?) {

                    }

                    override fun onAnimationStart(animation: Animator?) {
                        rockerIv.visibility = View.VISIBLE
                    }
                })
            }

            if (rotateAnimationStop == null) {
                rotateAnimationStop = ObjectAnimator.ofFloat(rockerIv, View.ROTATION, endAngle, startAngle)
                rotateAnimationStop?.duration = 800
                rotateAnimationStop?.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        if (hideWhenPause) {
                            rockerIv.visibility = View.GONE
                        }
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        if (hideWhenPause) {
                            rockerIv.visibility = View.GONE
                        }
                    }

                    override fun onAnimationStart(animation: Animator?) {
                    }
                })
            }
            MyLog.d(TAG,"onLayout wantPlaying=$wantPlaying" )
            if (wantPlaying) {
                play()
            } else {
                rockerIv.rotation = startAngle
                if (hideWhenPause) {
                    rockerIv.visibility = View.GONE
                }
                pause()
            }
        }

    }

    override fun onDetachedFromWindow() {
        MyLog.d(TAG,"onDetachedFromWindow" )
        super.onDetachedFromWindow()
        playing = false
        hasLayouted = false
        wantPlaying = false
        mHandler.removeMessages(AVATAR_ANIM)
        avatarContainer?.rotation = 0f
        avatarAnimation?.cancel()
        rotateAnimationStop?.cancel()
        rotateAnimationPlay?.cancel()
        //rockerIv.clearAnimation()
        if (hideWhenPause) {
            rockerIv.visibility = View.GONE
        }
    }

}