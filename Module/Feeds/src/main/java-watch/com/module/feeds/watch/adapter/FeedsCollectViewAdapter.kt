package com.module.feeds.watch.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.utils.U
import com.module.feeds.R
import com.module.feeds.watch.model.FeedsCollectModel
import com.module.feeds.watch.viewholder.FeedsCollectViewHolder

class FeedsCollectViewAdapter(val listener: FeedCollectListener) : RecyclerView.Adapter<FeedsCollectViewHolder>() {

    var mDataList = ArrayList<FeedsCollectModel>()
    var mCurrentPlayModel: FeedsCollectModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedsCollectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_like_item_holder_layout, parent, false)
        return FeedsCollectViewHolder(view, listener)
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

    fun findRealPosition(model: FeedsCollectModel?): Int {
        if (model != null) {
            for (i in 0 until mDataList.size) {
                if (mDataList[i].feedID == model.feedID) {
                    return i
                }
            }
        }
        return 0
    }
}

interface FeedCollectListener {
    fun onClickPlayListener(model: FeedsCollectModel?, position: Int)
    fun onClickItemListener(model: FeedsCollectModel?, position: Int)
}