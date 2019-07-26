package com.module.feeds.rank.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.feeds.feeds.model.FeedsWatchModel
import com.module.feeds.R
import com.module.feeds.rank.holder.FeedsDetailRankViewHolder

class FeedDetailAdapter : RecyclerView.Adapter<FeedsDetailRankViewHolder>() {

    var mDataList = ArrayList<FeedsWatchModel>()

    var mCurrentPlayModel: FeedsWatchModel? = null

    var onClickPlayListener: ((model: FeedsWatchModel?, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedsDetailRankViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feeds_rank_detail_item_layout, parent, false)
        return FeedsDetailRankViewHolder(view, onClickPlayListener)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: FeedsDetailRankViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
        holder.mSongPlayIv.isSelected = mDataList[position] == mCurrentPlayModel
    }
}