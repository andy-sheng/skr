package com.module.posts.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.view.ex.ExImageView
import com.component.busilib.view.SpeakingTipsAnimationView
import com.module.posts.R

// 首页音频的view
class PostsAudioView : ConstraintLayout {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private val audioBg: ExImageView
    private val audioPlay: ImageView
    private val speakerAnimationIv: SpeakingTipsAnimationView
    private val durationTv: TextView

    var isPlaying = false

    init {
        View.inflate(context, R.layout.post_audio_view_layout, this)

        audioBg = this.findViewById(R.id.audio_bg)
        audioPlay = this.findViewById(R.id.audio_play)
        speakerAnimationIv = this.findViewById(R.id.speaker_animation_iv)
        durationTv = this.findViewById(R.id.duration_tv)
    }

    private fun bindData(){
        // todo
    }

    fun setPlay(isPlay: Boolean) {
        isPlaying = isPlay
        if (isPlay) {
            // 播放动画
            speakerAnimationIv.show(10)
        } else {
            // 停止动画
            speakerAnimationIv.hide()
        }
    }
}