package com.module.feeds.watch.watchview

import android.support.constraint.ConstraintLayout
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.common.base.BaseFragment
import com.common.log.MyLog
import com.common.view.ex.ExConstraintLayout
import com.facebook.drawee.view.SimpleDraweeView
import com.module.feeds.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class FeedRecommendView(val fragment: BaseFragment) : ConstraintLayout(fragment.context), CoroutineScope by MainScope() {

    val TAG = "FeedRecommendView"

    val background: SimpleDraweeView
    val recommendFilm: ImageView
    val recordCover: SimpleDraweeView
    val songNameTv: TextView
    val songDescTv: TextView
    val collectIv: ImageView
    val likeNumTv: TextView
    val playLastIv: ImageView
    val recordPlayIv: ImageView
    val playNextIv: ImageView
    val playTimeTv: TextView
    val totalTimeTv: TextView
    val seekBar: SeekBar
    val bottomArea: ExConstraintLayout
    val avatarIv: SimpleDraweeView
    val commentNumTv: TextView
    val nameTv: TextView
    val contentTv: TextView

    var isSeleted = false  // 是否选中

    init {
        View.inflate(context, R.layout.feed_recomend_view_layout, this)

        background = this.findViewById(R.id.background)
        recommendFilm = this.findViewById(R.id.recommend_film)
        recordCover = this.findViewById(R.id.record_cover)
        songNameTv = this.findViewById(R.id.song_name_tv)
        songDescTv = this.findViewById(R.id.song_desc_tv)
        collectIv = this.findViewById(R.id.collect_iv)
        likeNumTv = this.findViewById(R.id.like_num_tv)
        playLastIv = this.findViewById(R.id.play_last_iv)
        recordPlayIv = this.findViewById(R.id.record_play_iv)
        playNextIv = this.findViewById(R.id.play_next_iv)
        playTimeTv = this.findViewById(R.id.play_time_tv)
        totalTimeTv = this.findViewById(R.id.total_time_tv)
        seekBar = this.findViewById(R.id.seek_bar)
        bottomArea = this.findViewById(R.id.bottom_area)
        avatarIv = this.findViewById(R.id.avatar_iv)
        commentNumTv = this.findViewById(R.id.comment_num_tv)
        nameTv = this.findViewById(R.id.name_tv)
        contentTv = this.findViewById(R.id.content_tv)
    }


    open fun selected() {
        MyLog.d(TAG, "selected")
        isSeleted = true
    }

    open fun unselected() {
        MyLog.d(TAG, "unselected")
        isSeleted = false
    }

    fun autoRefresh() {
    }

    open fun destroy() {
        cancel()
    }

}