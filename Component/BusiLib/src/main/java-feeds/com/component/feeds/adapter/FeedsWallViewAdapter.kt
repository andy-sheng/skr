package com.component.feeds.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.component.busilib.R
import com.component.feeds.holder.EmptyFeedWallHolder
import com.component.feeds.holder.FeedsWallViewHolder
import com.component.feeds.listener.FeedsListener
import com.component.feeds.model.FeedsWatchModel

class FeedsWallViewAdapter(var listener: FeedsListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mDataList = ArrayList<FeedsWatchModel>()

    var mCurrentModel: FeedsWatchModel? = null

    private val mEmptyItemType = 1
    private val mNormalItemType = 2

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
        super.onBindViewHolder(holder, position, payloads)
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

    companion object {
        const val REFRESH_TYPE_LIKE = 0  // 局部刷新喜欢
        const val REFRESH_TYPE_COMMENT = 1  // 局部刷新评论数
    }
}