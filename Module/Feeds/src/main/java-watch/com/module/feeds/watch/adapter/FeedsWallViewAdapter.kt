package com.module.feeds.watch.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.feeds.R
import com.module.feeds.watch.viewholder.EmptyFeedWallHolder
import com.module.feeds.watch.viewholder.FeedsWallViewHolder
import com.module.feeds.watch.listener.FeedsListener
import com.module.feeds.watch.model.FeedsWatchModel

class FeedsWallViewAdapter(var listener: FeedsListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mDataList = ArrayList<FeedsWatchModel>()

    var mCurrentModel: FeedsWatchModel? = null

    private val mEmptyItemType = 1
    private val mNormalItemType = 2

    var mCurrentPlayPosition: Int? = null
    var mCurrentPlayModel: FeedsWatchModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == mEmptyItemType) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_wall_empty_holder_layout, parent, false)
            EmptyFeedWallHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_wall_item_holder_layout, parent, false)
            FeedsWallViewHolder(view, listener)
        }
    }


    override fun getItemCount(): Int {
        return if (mDataList.isNotEmpty()) {
            mDataList.size
        } else {
            1
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mDataList.isNotEmpty()) {
            mNormalItemType
        } else {
            mEmptyItemType
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            // 全部刷新的布局
            if (holder is FeedsWallViewHolder) {
                holder.bindData(position, mDataList[position])
                if (mDataList[position] == mCurrentModel) {
                    holder.startPlay()
                } else {
                    holder.stopPlay()
                }
            }
        } else {
            // 局部刷新
            if (holder is FeedsWallViewHolder) {
                val type = payloads[0] as Int
                if (type == REFRESH_TYPE_LIKE) {
                    holder.refreshLike(position, mDataList[position])
                } else if (type == REFRESH_TYPE_COMMENT) {
                    holder.refreshComment(position, mDataList[position])
                }
            }
        }
    }

    fun update(position: Int, model: FeedsWatchModel, refreshType: Int) {
        if (model.feedID == mDataList[position].feedID) {
            // 位置是对的的
            mDataList[position] = model
            notifyItemChanged(position, refreshType)
            return
        } else {
            // 位置是错的
            for (i in 0 until mDataList.size) {
                if (mDataList[i].feedID == model.feedID) {
                    mDataList[i] = model
                    notifyItemChanged(i, refreshType)
                    return
                }
            }
        }
    }

    fun updatePlayModel(pos: Int, model: FeedsWatchModel?) {
        if (mCurrentPlayModel != model) {
            mCurrentPlayPosition = pos
            mCurrentPlayModel = model
            notifyDataSetChanged()
        }
    }

    companion object {
        const val REFRESH_TYPE_LIKE = 0  // 局部刷新喜欢
        const val REFRESH_TYPE_COMMENT = 1  // 局部刷新评论数
    }
}