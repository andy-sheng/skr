package com.module.playways.race.room.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.RelativeLayout
import com.common.image.fresco.BaseImageView
import com.common.view.ex.ExTextView



class RaceSelectSongItemView : RelativeLayout {
    lateinit var songNameTv: ExTextView
    lateinit var avatarIv1: BaseImageView
    lateinit var avatarIv2: BaseImageView
    lateinit var avatarIv3: BaseImageView

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        View.inflate(context, com.module.playways.R.layout.race_select_song_item_layout, this)
        songNameTv = findViewById(com.module.playways.R.id.song_name_tv) as ExTextView
        avatarIv1 = findViewById(com.module.playways.R.id.avatar_iv_1) as BaseImageView
        avatarIv2 = findViewById(com.module.playways.R.id.avatar_iv_2) as BaseImageView
        avatarIv3 = findViewById(com.module.playways.R.id.avatar_iv_3) as BaseImageView
    }

    fun bindData() {

    }

    fun startSelectedAnimation() {
        val animation = ScaleAnimation(
                1.0f, 1.1f, 1.0f, 1.1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)

        animation.duration = 500
        animation.fillAfter = true
        startAnimation(animation)
    }

    fun reset() {
        clearAnimation()
        avatarIv1.visibility = View.GONE
        avatarIv2.visibility = View.GONE
        avatarIv3.visibility = View.GONE
    }
}
