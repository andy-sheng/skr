package com.module.feeds.rank.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.feeds.R
import com.module.feeds.rank.model.FeedRankInfoModel

class FeedsRankViewHolder(item: View) : RecyclerView.ViewHolder(item) {

    val mCoverIv: SimpleDraweeView = item.findViewById(R.id.cover_iv)
    val mHitIv: ImageView = item.findViewById(R.id.hit_iv)
    val mNameTv: TextView = item.findViewById(R.id.name_tv)
    val mOccupyTv: TextView = item.findViewById(R.id.occupy_tv)
    val mJoinTv: TextView = item.findViewById(R.id.join_tv)

    var mModel: FeedRankInfoModel? = null
    var mPosition: Int = 0

    init {
        item.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 进入详细排行榜
            }

        })

        item.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 打榜去
            }

        })

    }

    fun bindData(position: Int, model: FeedRankInfoModel) {
        this.mPosition = position
        this.mModel = model

    }
}