package com.module.feeds.watch.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.log.MyLog
import com.module.feeds.R
import com.module.feeds.watch.listener.FeedsListener
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.viewholder.EmptyFeedWallHolder
import com.module.feeds.watch.viewholder.FeedViewHolder
import com.module.feeds.watch.viewholder.FeedsWallViewHolder
import com.module.feeds.watch.viewholder.FeedsWatchViewHolder

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
        if (holder is FeedViewHolder) {
            if (payloads.isEmpty()) {
                MyLog.d("FeedsWatchViewAdapter", "onBindViewHolder position=$position playing=$playing")
                // 全部刷新的布局
                holder.bindData(position, mDataList[position])
                if (mDataList[position].feedID == mCurrentPlayModel?.feedID
                        && mDataList[position].song?.songID == mCurrentPlayModel?.song?.songID
                        && playing) {
                    MyLog.d("FeedsWatchViewAdapter", "startPlay")
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
                        holder.playLyric(position, mDataList[position])
                    }
                } else if (type == REFRESH_LYRIC_STATE) {
                    if (mDataList[position].song?.lyricType == 0) {
                        holder.pauseLyricWhenBuffering(position, mDataList[position])
                    } else {
                        holder.resumeLyricWhenBufferingEnd(position, mDataList[position])
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
        if (position < mDataList.size && model?.feedID == mDataList[position].feedID) {
            // 位置是对的的
            mDataList[position] = model!!
            notifyItemChanged(position, refreshType)
            return
        } else {
            // 位置是错的
            update(model, refreshType)
        }
    }

    fun update(model: FeedsWatchModel?, refreshType: Int) {
        // 位置是错的
        for (i in 0 until mDataList.size) {
            if (mDataList[i].feedID == model?.feedID) {
                mDataList[i] = model!!
                notifyItemChanged(i, refreshType)
                return
            }
        }
    }

    // 将外部数据转为list中数据
    fun getModelFromDetail(model: FeedsWatchModel?): FeedsWatchModel? {
        if (model != null) {
            for (i in 0 until mDataList.size) {
                if (mDataList[i].feedID == model.feedID) {
                    if (mDataList[i] == model) {
                        return model
                    } else {
                        //更新其中变化的数据
                        mDataList[i].exposure = model.exposure
                        mDataList[i].isLiked = model.isLiked
                        mDataList[i].starCnt = model.starCnt
                        mDataList[i].shareCnt = model.shareCnt
                        mDataList[i].commentCnt = model.commentCnt
                        return mDataList[i]
                    }
                }
            }
        }
        return null
    }

    fun delete(model: FeedsWatchModel) {
        mDataList.remove(model)
        notifyDataSetChanged()
        return
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
        MyLog.d("FeedsWatchViewAdapter", "resumePlayModel playing=$playing mCurrentPlayPosition=$mCurrentPlayPosition mCurrentPlayModel=$mCurrentPlayModel")
        if (mCurrentPlayModel != null && mCurrentPlayPosition != null) {
            if (!playing) {
                playing = true
                notifyDataSetChanged()
            }
        }
    }

    fun pauseLyricWhenBuffering() {
        if (mCurrentPlayModel != null && mCurrentPlayPosition != null) {
            mCurrentPlayModel?.song?.lyricType = 0
        }

        update(mCurrentPlayPosition ?: 0, mCurrentPlayModel, REFRESH_LYRIC_STATE)
    }

    fun resumeLyricWhenBufferingEnd() {
        if (mCurrentPlayModel != null && mCurrentPlayPosition != null) {
            mCurrentPlayModel?.song?.lyricType = 1
        }

        update(mCurrentPlayPosition ?: 0, mCurrentPlayModel, REFRESH_LYRIC_STATE)
    }

    /**
     * 更新播放进度
     */
    fun updatePlayProgress(curPostion: Long, totalDuration: Long) {
        // 位置是错的
        mCurrentPlayModel?.song?.let {
            it.playCurPos = curPostion.toInt()
            it.playDurMsFromPlayerForDebug = totalDuration.toInt()
            update(mCurrentPlayPosition ?: 0, mCurrentPlayModel, REFRESH_TYPE_LYRIC)
        }
    }


    companion object {
        const val REFRESH_TYPE_LIKE = 1  // 局部刷新喜欢
        const val REFRESH_TYPE_COMMENT = 2  // 局部刷新评论数
        const val REFRESH_TYPE_LYRIC = 3  // 局部刷新歌词
        const val REFRESH_LYRIC_STATE = 4  // 局部刷新歌词
    }
}