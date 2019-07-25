package com.component.feeds.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.component.busilib.R
import com.component.feeds.holder.FeedsWallViewHolder
import com.component.feeds.listener.FeedsListener
import com.component.feeds.model.FeedsWatchModel
import com.component.feeds.presenter.FeedWatchViewPresenter

class FeedsWallViewAdapter(var listener: FeedsListener) : RecyclerView.Adapter<FeedsWallViewHolder>() {

    var mDataList = ArrayList<FeedsWatchModel>()

    var mCurrentModel: FeedsWatchModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedsWallViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_wall_item_holder_layout, parent, false)
        return FeedsWallViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: FeedsWallViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
        if (mCurrentModel == mDataList[position]) {
            holder.startPlay()
        } else {
            holder.stopPlay()
        }
    }
}