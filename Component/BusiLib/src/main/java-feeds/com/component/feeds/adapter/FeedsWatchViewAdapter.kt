package com.component.feeds.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.component.busilib.R
import com.component.feeds.holder.FeedsWatchViewHolder
import com.component.feeds.model.FeedsWatchModel

class FeedsWatchViewAdapter(var listener: FeedsListener) : RecyclerView.Adapter<FeedsWatchViewHolder>() {

    var mDataList = ArrayList<FeedsWatchModel>()

    var mCurrentModel: FeedsWatchModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedsWatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_watch_item_holder_layout, parent, false)
        return FeedsWatchViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: FeedsWatchViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
        if (mDataList[position] == mCurrentModel) {
            holder.startPlay()
        } else {
            holder.stopPlay()
        }
    }

    interface FeedsListener {
        fun onClickMoreListener(watchModel: FeedsWatchModel?)
        fun onClickLikeListener(watchModel: FeedsWatchModel?)
        fun onClickCommentListener(watchModel: FeedsWatchModel?)
        fun onClickHitListener(watchModel: FeedsWatchModel?)
        fun onClickDetailListener(watchModel: FeedsWatchModel?)
        fun onClickCDListener(watchModel: FeedsWatchModel?)
    }
}