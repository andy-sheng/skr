package com.module.feeds.rank.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.feeds.R
import com.module.feeds.rank.holder.FeedDetailTagViewHolder
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.watchview.BaseWatchView

class FeedTagDetailAdapter(val listener: FeedTagListener) : RecyclerView.Adapter<FeedDetailTagViewHolder>() {

    var mDataList = ArrayList<FeedsWatchModel>()

    var mCurrentPlayPosition = -1
    var mCurrentPlayModel: FeedsWatchModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedDetailTagViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feeds_tag_detail_item_layout, parent, false)
        return FeedDetailTagViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: FeedDetailTagViewHolder, position: Int) {
    }

    override fun onBindViewHolder(holder: FeedDetailTagViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            holder.bindData(position, mDataList[position])
        } else {
            // 局部刷新
            val refreshType = payloads[0] as Int
            if (refreshType == REFRESH_TYPE_COLLECT) {
                holder.refreshCollects()
            } else if (refreshType == REFRESH_TYPE_PLAY) {

            }
        }
    }


    fun update(position: Int, model: FeedsWatchModel?, refreshType: Int) {
        if (mDataList.isNotEmpty()) {
            if (position >= 0 && position < mDataList.size && mDataList[position] == model) {
                // 位置是对的
                notifyItemChanged(position, refreshType)
                return
            } else {
                update(model, refreshType)
            }
        } else {

        }
    }

    fun update(model: FeedsWatchModel?, refreshType: Int) {
        // 位置是错的
        for (i in 0 until mDataList.size) {
            if (mDataList[i] == model) {
                notifyItemChanged(i, refreshType)
                return
            }
        }
    }

    companion object {
        const val REFRESH_TYPE_COLLECT = 1 // 刷新是否收藏状态
        const val REFRESH_TYPE_PLAY = 2  //刷新播放状态
    }
}

public interface FeedTagListener {
    fun onClickCollect(position: Int, model: FeedsWatchModel?)

    fun onClickItem(position: Int, model: FeedsWatchModel?)
}