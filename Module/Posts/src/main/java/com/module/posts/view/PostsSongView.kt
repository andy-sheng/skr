package com.module.posts.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.component.busilib.model.FeedSongModel
import com.component.busilib.view.MarqueeTextView
import com.module.posts.R

class PostsSongView : ConstraintLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val audioBg: ExImageView
    val songPlayBg: ExImageView
    val songPlayIv: ImageView
    val songNameTv: MarqueeTextView

    var isPlaying = false

    init {
        View.inflate(context, R.layout.posts_song_view_layout, this)

        audioBg = this.findViewById(R.id.audio_bg)
        songPlayBg = this.findViewById(R.id.song_play_bg)
        songPlayIv = this.findViewById(R.id.song_play_iv)
        songNameTv = this.findViewById(R.id.song_name_tv)
    }

    fun bindData(song: FeedSongModel?) {
        songNameTv.text = song?.songTpl?.songName
    }

    fun setPlay(isPlay: Boolean) {
        isPlaying = isPlay
        if (isPlay) {
            songPlayIv.background = U.getDrawable(R.drawable.posts_song_pause_icon)
        } else {
            songPlayIv.background = U.getDrawable(R.drawable.posts_song_play_icon)
        }
    }
}