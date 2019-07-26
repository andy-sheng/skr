package com.module.feeds.rank.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.feeds.R
import com.module.feeds.rank.holder.FeedsRankViewHolder
import com.module.feeds.rank.model.FeedRankInfoModel

class FeedsRankAdapter : RecyclerView.Adapter<FeedsRankViewHolder>() {

    var mDataList = ArrayList<FeedRankInfoModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedsRankViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_rank_item_layout, parent, false)
        return FeedsRankViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedsRankViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

}