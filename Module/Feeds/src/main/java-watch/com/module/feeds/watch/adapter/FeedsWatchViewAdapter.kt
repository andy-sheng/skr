package com.module.feeds.watch.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.feeds.R
import com.module.feeds.watch.viewholder.FeedsWatchViewHolder
import com.module.feeds.watch.listener.FeedsListener
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.viewholder.EmptyFeedWallHolder
import com.module.feeds.watch.viewholder.FeedViewHolder
import com.module.feeds.watch.viewholder.FeedsWallViewHolder

class FeedsWatchViewAdapter(var listener: FeedsListener, private val isHomePage: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mDataList = ArrayList<FeedsWatchModel>()

    var mCurrentPlayPosition: Int? = null
    var mCurrentPlayModel: FeedsWatchModel? = null
    var playing = false

    private val mPersonEmptyItemType = 1
    private val mPersonNormalItemType = 2
    private val mHomeNormalItemType = 3

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (holder is FeedViewHolder) {
            if (payloads.isEmpty()) {
                // 全部刷新的布局
                holder.bindData(position, mDataList[position])
                if (mDataList[position] == mCurrentPlayModel && playing) {
                    holder.startPlay()
                } else {
                    holder.stopPlay()
                }
            } else {
                // 局部刷新
                val type = payloads[0] as Int
                if (type == REFRESH_TYPE_LIKE) {
                    holder.refreshLike(position, mDataList[position])
                } else if (type == REFRESH_TYPE_COMMENT) {
                    holder.refreshComment(position, mDataList[position])
                } else if (type == REFRESH_TYPE_LYRIC) {
                    if (mDataList[position] == mCurrentPlayModel) {
                        holder.playLyric()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            mPersonEmptyItemType -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_wall_empty_holder_layout, parent, false)
                EmptyFeedWallHolder(view)
            }
            mPersonNormalItemType -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_wall_item_holder_layout, parent, false)
                FeedsWallViewHolder(view, listener)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_watch_item_holder_layout, parent, false)
                FeedsWatchViewHolder(view, listener)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isHomePage) {
            mDataList.size
        } else {
            if (mDataList.isNotEmpty()) {
                mDataList.size
            } else {
                1
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isHomePage) {
            mHomeNormalItemType
        } else {
            if (mDataList.isEmpty()) {
                mPersonEmptyItemType
            } else {
                mPersonNormalItemType
            }
        }
    }

    fun update(position: Int, model: FeedsWatchModel?, refreshType: Int) {
        if (model?.feedID == mDataList[position].feedID) {
            // 位置是对的的
            mDataList[position] = model!!
            notifyItemChanged(position, refreshType)
            return
        } else {
            // 位置是错的
            for (i in 0 until mDataList.size) {
                if (mDataList[i].feedID == model?.feedID) {
                    mDataList[i] = model!!
                    notifyItemChanged(i, refreshType)
                    return
                }
            }
        }
    }

    /**
     * 请求播放
     */
    fun startPlayModel(pos: Int, model: FeedsWatchModel?) {
        if (mCurrentPlayModel != model || !playing) {
            mCurrentPlayPosition = pos
            mCurrentPlayModel = model
            playing = true
            notifyDataSetChanged()
        }
    }

    fun pausePlayModel() {
        if (playing) {
            playing = false
            notifyDataSetChanged()
        }
    }

    fun resumePlayModel() {
        if (mCurrentPlayModel != null && mCurrentPlayPosition != null) {
            if (!playing) {
                playing = true
                notifyDataSetChanged()
            }
        }
    }

    /**
     * 更新播放进度
     */
    fun updatePlayProgress(curPostion: Long, totalDuration: Long) {
        // 位置是错的
        mCurrentPlayModel?.song?.let {
            it.playCurPos = curPostion.toInt()
            update(mCurrentPlayPosition ?: 0, mCurrentPlayModel, REFRESH_TYPE_LYRIC)
        }
    }


    companion object {
        const val REFRESH_TYPE_LIKE = 0  // 局部刷新喜欢
        const val REFRESH_TYPE_COMMENT = 1  // 局部刷新评论数
        const val REFRESH_TYPE_LYRIC = 2  // 局部刷新歌词
    }
}