package com.module.posts.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.common.utils.dp
import com.common.view.ex.ExImageView
import com.component.busilib.view.SpeakingTipsAnimationView
import com.module.posts.R
import com.module.posts.watch.model.PostsResoureModel
import kotlin.math.roundToInt

// 评论中音频的view
class PostsCommentAudioView : ConstraintLayout {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private val audioBg: ExImageView
    private val speakerAnimationIv: SpeakingTipsAnimationView
    private val durationTv: TextView

    var isPlaying = false
    var audioModel: PostsResoureModel? = null

    private val minSize = 80.dp()    // 最小尺寸(小于10秒)
    private val maxSize = 144.dp()   // 最大尺寸(大于40秒）

    init {
        View.inflate(context, R.layout.post_comment_audio_view_layout, this)

        audioBg = this.findViewById(R.id.audio_bg)
        speakerAnimationIv = this.findViewById(R.id.speaker_animation_iv)
        durationTv = this.findViewById(R.id.duration_tv)
    }

    fun bindData(audios: List<PostsResoureModel>) {
        audioModel = audios[0]
        var duration = (audios[0].duration.toFloat() / 1000.toFloat()).toDouble().roundToInt()
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
    }

    fun setPlay(isPlay: Boolean) {
        isPlaying = isPlay
        if (isPlay) {
            // 播放动画
            speakerAnimationIv.show((audioModel?.duration ?: 0).toInt())
        } else {
            // 停止动画
            speakerAnimationIv.hide()
        }
    }
}