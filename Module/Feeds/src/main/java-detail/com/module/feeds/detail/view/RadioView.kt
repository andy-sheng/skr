package com.module.feeds.detail.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.INFINITE
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.image.fresco.BaseImageView

class RadioView : ConstraintLayout {
    var panelDrawable: Drawable? = null
    var panelWidth: Float? = 0f
    var panelMarginTop: Float? = 0f
    var rockerDrawable: Drawable? = null
    var rockerWidth: Float? = 0f
    var rockerHeight: Float? = 0f
    var rockerXP: Float? = 0.5f
    var rockerYP: Float? = 0.5f
    var avatorMargin: Float? = 0f
    var avator: BaseImageView? = null

    var mRockerIv: ImageView? = null
    var mPanelIv: ImageView? = null
    var avatarContainer: View? = null

    var avatorAnimation: ObjectAnimator? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        View.inflate(context, com.module.feeds.R.layout.radio_view_layout, this)
        mRockerIv = findViewById(com.module.feeds.R.id.rocker_iv)
        mPanelIv = findViewById(com.module.feeds.R.id.panel_iv)
        avator = findViewById(com.module.feeds.R.id.avatar_iv)
        avatarContainer = findViewById(com.module.feeds.R.id.avatar_container)

        val typedArray = context.obtainStyledAttributes(attrs, com.module.feeds.R.styleable.RadioView)
        typedArray.getDrawable(com.module.feeds.R.styleable.RadioView_panelDrawable)?.let {
            panelDrawable = it
        }

        panelWidth = typedArray.getDimension(com.module.feeds.R.styleable.RadioView_panelWidth, 0f)
        panelMarginTop = typedArray.getDimension(com.module.feeds.R.styleable.RadioView_panelMarginTop, 0f)

        typedArray.getDrawable(com.module.feeds.R.styleable.RadioView_rockerDrawable)?.let {
            rockerDrawable = it
        }

        avatorMargin = typedArray.getDimension(com.module.feeds.R.styleable.RadioView_userIconMargin, 0f)
        rockerWidth = typedArray.getDimension(com.module.feeds.R.styleable.RadioView_rockerWidth, 0f)
        rockerHeight = typedArray.getDimension(com.module.feeds.R.styleable.RadioView_rockerHeight, 0f)
        rockerXP = typedArray.getFloat(com.module.feeds.R.styleable.RadioView_rockerXP, 0.5f)
        rockerYP = typedArray.getFloat(com.module.feeds.R.styleable.RadioView_rockerYP, 0.5f)

        mPanelIv?.background = panelDrawable
        mRockerIv?.background = rockerDrawable

        mPanelIv?.layoutParams?.width = panelWidth!!.toInt()
        mPanelIv?.layoutParams?.height = panelWidth!!.toInt()
        (mPanelIv?.layoutParams as LayoutParams).setMargins(0, panelMarginTop!!.toInt(), 0, 0)

        avatorMargin?.toInt()?.let {
            (avator?.layoutParams as LayoutParams).setMargins(it + 1, it + 1, it, it)
        }

        mRockerIv?.layoutParams?.width = rockerWidth!!.toInt()
        mRockerIv?.layoutParams?.height = rockerHeight!!.toInt()

        val rotateAnimation = RotateAnimation(-55f, -55f, Animation.RELATIVE_TO_SELF, rockerXP!!, Animation.RELATIVE_TO_SELF, rockerYP!!)
        rotateAnimation.setDuration(0)
        rotateAnimation.fillAfter = true
        mRockerIv?.startAnimation(rotateAnimation)

        avatorAnimation = ObjectAnimator.ofFloat(avatarContainer, "rotation", 0f, 360f)
        avatorAnimation?.duration = 10000
        avatorAnimation?.interpolator = LinearInterpolator()
        avatorAnimation?.repeatCount = INFINITE
    }

    fun setAvator(url: String) {
        AvatarUtils.loadAvatarByUrl(avator, AvatarUtils.newParamsBuilder(url)
                .setCircle(true)
                .build())
    }

    fun play() {
        setAvator(MyUserInfoManager.getInstance().avatar)
        mRockerIv?.clearAnimation()
        val rotateAnimation = RotateAnimation(-55f, -25f, Animation.RELATIVE_TO_SELF, rockerXP!!, Animation.RELATIVE_TO_SELF, rockerYP!!)
        rotateAnimation.setDuration(800)
        rotateAnimation.fillAfter = true
        mRockerIv?.startAnimation(rotateAnimation)

        avatorAnimation?.let {
            if (it.isStarted) {
                it.resume()
            } else {
                avatarContainer?.pivotX = avatarContainer!!.width.toFloat() / 2
                avatarContainer?.pivotY = avatarContainer!!.height.toFloat() / 2 + panelMarginTop!! / 2
                it.start()
            }
        }
    }

    fun pause() {
        mRockerIv?.clearAnimation()
        val rotateAnimation = RotateAnimation(-25f, -55f, Animation.RELATIVE_TO_SELF, rockerXP!!, Animation.RELATIVE_TO_SELF, rockerYP!!)
        rotateAnimation.setDuration(800)
        rotateAnimation.fillAfter = true
        mRockerIv?.startAnimation(rotateAnimation)
        avatorAnimation?.pause()
    }

    fun destroy() {
        avatorAnimation?.cancel()
        mRockerIv?.clearAnimation()
    }
}