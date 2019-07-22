package com.module.feeds.watch.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.feeds.R
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.viewholder.FeedsWatchViewHolder

class FeedsWatchViewAdapter : RecyclerView.Adapter<FeedsWatchViewHolder>() {

    var mDataList = ArrayList<FeedsWatchModel>()

    var onClickMoreListener: ((watchModel: FeedsWatchModel?) -> Unit)? = null
    var onClickLikeListener: ((watchModel: FeedsWatchModel?) -> Unit)? = null
    var onClickCommentListener: ((watchModel: FeedsWatchModel?) -> Unit)? = null
    var onClickHitListener: ((watchModel: FeedsWatchModel?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedsWatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_watch_item_holder_layout, parent, false)
        return FeedsWatchViewHolder(view, onClickMoreListener, onClickLikeListener, onClickCommentListener, onClickHitListener)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: FeedsWatchViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }
}