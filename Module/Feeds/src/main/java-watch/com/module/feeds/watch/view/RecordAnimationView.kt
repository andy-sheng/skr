package com.module.feeds.watch.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.widget.ImageView
import com.common.core.avatar.AvatarUtils
import com.module.feeds.R
import com.facebook.drawee.view.SimpleDraweeView

// 唱片动画的view
class RecordAnimationView : ConstraintLayout {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}


    private val mRecordPoint: ImageView
    private val mRecordFilm: ImageView
    private val mAvatarIv: SimpleDraweeView

    init {
        inflate(context, R.layout.record_animation_view_layout, this)

        mRecordPoint = findViewById(R.id.record_point)
        mRecordFilm = findViewById(R.id.record_film)
        mAvatarIv = findViewById(R.id.avatar_iv)
    }

    fun bindData(avatar: String) {
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(avatar)
                .setCircle(true)
                .build())
    }

    fun startAnimation() {

    }

    fun stopAnimation() {

    }
}