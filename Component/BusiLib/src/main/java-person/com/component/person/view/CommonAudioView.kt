package com.component.person.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.common.utils.dp
import com.common.view.ex.ExImageView
import com.component.busilib.R
import com.component.busilib.view.SpeakingTipsAnimationView
import kotlin.math.roundToInt

// 评论中音频的view(也是个人主页中的)
class CommonAudioView : ConstraintLayout {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private val audioBg: ExImageView
    private val speakerAnimationIv: SpeakingTipsAnimationView
    private val durationTv: TextView

    var isPlaying = false
    var audioDuration = 0L

    private val minSize = 80.dp()    // 最小尺寸(小于10秒)
    private val maxSize = 144.dp()   // 最大尺寸(大于40秒）

    init {
        View.inflate(context, R.layout.common_audio_view_layout, this)

        audioBg = this.findViewById(R.id.audio_bg)
        speakerAnimationIv = this.findViewById(R.id.speaker_animation_iv)
        durationTv = this.findViewById(R.id.duration_tv)
    }

    fun bindData(audioMs: Long) {
        this.isPlaying = false
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
        speakerAnimationIv.reset()
    }

    fun setPlay(isPlay: Boolean) {
        isPlaying = isPlay
        if (isPlay) {
            // 播放动画
            speakerAnimationIv.show(audioDuration.toInt(), false)
        } else {
            // 停止动画
            speakerAnimationIv.hide()
        }
    }
}