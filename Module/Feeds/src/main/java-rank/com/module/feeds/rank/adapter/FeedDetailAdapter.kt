package com.module.feeds.rank.adapter

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.R
import com.module.feeds.rank.holder.FeedsDetailOneViewHolder
import com.module.feeds.rank.holder.FeedsDetailRankViewHolder
import com.module.feeds.rank.holder.FeedsDetailTopViewHolder

class FeedDetailAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var mDataList = ArrayList<FeedsWatchModel>()

    var mCurrentPlayModel: FeedsWatchModel? = null

    var onClickPlayListener: ((model: FeedsWatchModel?, position: Int) -> Unit)? = null

    private val type_ONE = 1
    private val type_TOP = 2
    private val type_NORMAL = 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            type_ONE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.feeds_rank_detail_one_layout, parent, false)
                FeedsDetailOneViewHolder(view, onClickPlayListener)
            }
            type_TOP -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.feeds_rank_detail_top_item_layout, parent, false)
                FeedsDetailTopViewHolder(view, onClickPlayListener)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.feeds_rank_detail_item_layout, parent, false)
                FeedsDetailRankViewHolder(view, onClickPlayListener)
            }
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FeedsDetailOneViewHolder -> {
                holder.bindData(position, mDataList[position])
                holder.mSongPlayIv.isSelected = mCurrentPlayModel?.feedID == mDataList[position].feedID
            }
            is FeedsDetailTopViewHolder -> {
                // 有数据
                holder.bindData(position, mDataList[position])
                holder.mSongPlayIv.isSelected = mCurrentPlayModel?.feedID == mDataList[position].feedID
            }
            is FeedsDetailRankViewHolder -> {
                holder.bindData(position, mDataList[position])
                holder.mSongPlayIv.isSelected = mCurrentPlayModel?.feedID == mDataList[position].feedID
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> type_ONE
            position <= 3 -> type_TOP
            else -> type_NORMAL
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        val manger = recyclerView.layoutManager
        if (manger is GridLayoutManager) {
            manger.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (getItemViewType(position)) {
                        // 总数是3:表示当前holder在3中占几个
                        type_ONE -> 3
                        type_TOP -> 1
                        else -> 3
                    }
                }
            }
        }
    }
}