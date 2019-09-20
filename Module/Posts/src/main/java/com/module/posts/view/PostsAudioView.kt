package com.module.posts.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExImageView
import com.component.busilib.view.SpeakingTipsAnimationView
import com.module.posts.R
import com.module.posts.watch.model.PostsResoureModel
import kotlin.math.roundToInt

// 首页音频的view
class PostsAudioView : ConstraintLayout {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private val audioBg: ExImageView
    private val audioPlay: ImageView
    private val speakerAnimationIv: SpeakingTipsAnimationView
    private val durationTv: TextView

    private val minSize = 120.dp()    // 最小尺寸(小于10秒)
    private val maxSize = 280.dp()   // 最大尺寸(大于40秒）

    var isPlaying = false
    var audioDuration = 0L

    init {
        View.inflate(context, R.layout.post_audio_view_layout, this)

        audioBg = this.findViewById(R.id.audio_bg)
        audioPlay = this.findViewById(R.id.audio_play)
        speakerAnimationIv = this.findViewById(R.id.speaker_animation_iv)
        durationTv = this.findViewById(R.id.duration_tv)
    }

    fun bindData(audioMs: Long) {
        this.audioDuration = audioMs
        var duration = (audioDuration.toFloat() / 1000.toFloat()).toDouble().roundToInt()
        if (duration > 60) {
            duration = 60
        }
        val width = when (duration) {
            in 0..10 -> minSize
            in 11..40 -> minSize + (maxSize - minSize) / (40 - 10) * (duration - 10)
            else -> maxSize
        }
        var lp = audioBg.layoutParams
        lp.width = width
        audioBg.layoutParams = lp

        durationTv.text = "${duration}s"
    }

    fun setPlay(isPlay: Boolean) {
        isPlaying = isPlay
        if (isPlay) {
            // 播放动画
            speakerAnimationIv.show((audioDuration).toInt())
        } else {
            // 停止动画
            speakerAnimationIv.hide()
        }
    }
}