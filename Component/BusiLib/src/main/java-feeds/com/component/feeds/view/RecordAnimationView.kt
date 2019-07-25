package com.component.feeds.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.common.core.avatar.AvatarUtils
import com.common.utils.U
import com.component.busilib.R
import com.facebook.drawee.view.SimpleDraweeView

// 唱片动画的view
class RecordAnimationView : ConstraintLayout {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private val mRecordPoint: ImageView
    private val mRecordFilm: ImageView
    private val mRecordCover: SimpleDraweeView

    private var mCDRotateAnim: ObjectAnimator? = null
    private var mCoverRotateAnim: ObjectAnimator? = null
    private var mPointRotateAnim: ObjectAnimator? = null
    private var mPointAlphAnim: ObjectAnimator? = null

    private val mAnimatorSet: AnimatorSet by lazy { AnimatorSet() }

    init {
        inflate(context, R.layout.record_animation_view_layout, this)

        mRecordPoint = findViewById(R.id.record_point)
        mRecordFilm = findViewById(R.id.record_film)
        mRecordCover = findViewById(R.id.record_cover)
    }

    fun bindData(avatar: String?) {

        AvatarUtils.loadAvatarByUrl(mRecordCover, AvatarUtils.newParamsBuilder(avatar)
                .setCircle(true)
                .build())
    }

    fun startAnimation() {
        if (mCDRotateAnim == null) {
            mCDRotateAnim = ObjectAnimator.ofFloat(mRecordFilm, View.ROTATION, 0f, 360f)
            mRecordFilm.pivotX = (U.getDisplayUtils().dip2px(92f) / 2).toFloat()
            mRecordFilm.pivotY = (U.getDisplayUtils().dip2px(92f) / 2).toFloat()
            mCDRotateAnim?.duration = 3000
            mCDRotateAnim?.repeatCount = Animation.INFINITE
            mCDRotateAnim?.interpolator = LinearInterpolator()
        }

        if (mCoverRotateAnim == null) {
            mCoverRotateAnim = ObjectAnimator.ofFloat(mRecordCover, View.ROTATION, 0f, 360f)
            mRecordCover.pivotX = (U.getDisplayUtils().dip2px(62f) / 2).toFloat()
            mRecordCover.pivotY = (U.getDisplayUtils().dip2px(62f) / 2).toFloat()
            mCoverRotateAnim?.duration = 3000
            mCoverRotateAnim?.repeatCount = Animation.INFINITE
            mCoverRotateAnim?.interpolator = LinearInterpolator()
        }

        if (mPointRotateAnim == null) {
            mPointRotateAnim = ObjectAnimator.ofFloat(mRecordPoint, View.ROTATION, 0f, 45f)
            mRecordPoint.pivotX = 0f
            mRecordPoint.pivotY = 0f
            mPointRotateAnim?.duration = 3000
            mPointRotateAnim?.repeatCount = 0
            mPointRotateAnim?.interpolator = LinearInterpolator()
        }

        if (mPointAlphAnim == null) {
            mPointAlphAnim = ObjectAnimator.ofFloat(mRecordPoint, View.ALPHA, 0f, 1f)
            mPointAlphAnim?.duration = 1500
            mPointRotateAnim?.interpolator = AccelerateInterpolator()
        }

        mRecordPoint.visibility = View.VISIBLE
        mAnimatorSet.playTogether(mCDRotateAnim, mCoverRotateAnim, mPointRotateAnim, mPointAlphAnim)
        mAnimatorSet.start()
    }

    fun stopAnimation(isPlaying: Boolean) {
        if (isPlaying) {
            mRecordPoint.visibility = View.GONE
            mRecordPoint.clearAnimation()
            mAnimatorSet.removeAllListeners()
            mAnimatorSet.cancel()
        } else {
            mRecordPoint.visibility = View.GONE
            mRecordFilm.clearAnimation()
            mRecordCover.clearAnimation()
            mRecordPoint.clearAnimation()
            mAnimatorSet.removeAllListeners()
            mAnimatorSet.cancel()
        }

    }
}