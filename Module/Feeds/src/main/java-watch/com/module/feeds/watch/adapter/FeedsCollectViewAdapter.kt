package com.module.feeds.watch.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.utils.U
import com.module.feeds.R
import com.module.feeds.watch.model.FeedRecommendTagModel
import com.module.feeds.watch.model.FeedsCollectModel
import com.module.feeds.watch.viewholder.FeedsCollectViewHolder
import com.module.feeds.watch.viewholder.HeaderFeedsCollectViewHolder

class FeedsCollectViewAdapter(val listener: FeedCollectListener) : RecyclerView.Adapter<FeedsCollectViewHolder>() {
    val NORMAL_TYPE = 0
    val FIRST_TYPE = 1
    var mDataList = ArrayList<FeedsCollectModel>()
    var mCurrentPlayModel: FeedsCollectModel? = null
    // 只用来做推荐顶部的
    var mRankTagList = ArrayList<FeedRecommendTagModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedsCollectViewHolder {
        if (viewType == NORMAL_TYPE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_like_item_holder_layout, parent, false)
            return FeedsCollectViewHolder(view, listener)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.header_feed_like_item_holder_layout, parent, false)
            return HeaderFeedsCollectViewHolder(view, listener)
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: FeedsCollectViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
        if (mDataList[position] == mCurrentPlayModel) {
            holder.mSongPlayIv.isSelected = true
            holder.mSongPlayIv.setImageResource(R.drawable.list_song_pause_icon)
            holder.mSongNameTv.setTextColor(Color.parseColor("#FFC15B"))
        } else {
            holder.mSongNameTv.setTextColor(U.getColor(R.color.black_trans_80))
            holder.mSongPlayIv.isSelected = false
            holder.mSongPlayIv.setImageResource(R.drawable.list_song_play_icon)
        }

        if (holder is HeaderFeedsCollectViewHolder) {
            holder.bindData(mRankTagList, mDataList.size)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return FIRST_TYPE
        } else {
            return NORMAL_TYPE
        }
    }

    fun update(position: Int, model: FeedsCollectModel) {
        if (mDataList[position].feedID == model.feedID) {
            mDataList[position] = model
            return
        } else {
            // 位置是错的
            for (i in 0 until mDataList.size) {
                if (mDataList[i].feedID == model.feedID) {
                    mDataList[i] = model
                    return
                }
            }
        }
    }
}

interface FeedCollectListener {
    fun onClickPlayListener(model: FeedsCollectModel?, position: Int)
    fun onClickItemListener(model: FeedsCollectModel?, position: Int)
}