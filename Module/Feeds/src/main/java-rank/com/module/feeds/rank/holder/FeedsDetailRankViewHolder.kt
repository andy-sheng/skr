package com.module.feeds.rank.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.module.feeds.watch.model.FeedsWatchModel
import com.facebook.drawee.view.SimpleDraweeView
import com.module.feeds.R

class FeedsDetailRankViewHolder(item: View, onClickPlayListener: ((model: FeedsWatchModel?, position: Int) -> Unit)?) : RecyclerView.ViewHolder(item) {

    var mPosition: Int = 0
    var mModel: FeedsWatchModel? = null

    val mSongCoverSdv: SimpleDraweeView = item.findViewById(R.id.song_cover_sdv)
    val mSongNameTv: TextView = item.findViewById(R.id.song_name_tv)
    val mLikeNumTv: TextView = item.findViewById(R.id.like_num_tv)
    val mSongPlayIv: ExImageView = item.findViewById(R.id.song_play_iv)

    init {
        mSongPlayIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickPlayListener?.invoke(mModel, mPosition)
            }
        })
    }

    fun bindData(position: Int, model: FeedsWatchModel) {
        this.mPosition = position
        this.mModel = mModel
    }
}