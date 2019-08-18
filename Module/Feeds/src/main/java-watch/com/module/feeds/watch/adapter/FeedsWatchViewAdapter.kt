package com.module.feeds.watch.adapter

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.log.MyLog
import com.module.feeds.R
import com.module.feeds.watch.listener.FeedsListener
import com.module.feeds.watch.model.FeedRecommendTagModel
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.viewholder.*
import com.module.feeds.watch.watchview.BaseWatchView

class FeedsWatchViewAdapter(var listener: FeedsListener, val mType: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mDataList = ArrayList<FeedsWatchModel>()
    // 只用来做推荐顶部的
    var mRecommendList = ArrayList<FeedRecommendTagModel>()

    var mCurrentPlayPosition: Int? = null
    var mCurrentPlayModel: FeedsWatchModel? = null
    var playing = false

    private val mPersonEmptyItemType = 1
    private val mPersonNormalItemType = 2
    private val mHomeNormalItemType = 3
    private val mHomeTopType = 4      // 推荐首页顶部
    private val uiHanlder = Handler(Looper.getMainLooper())

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            MyLog.d("FeedsWatchViewAdapter", "onBindViewHolder position=$position playing=$playing")
            // 全部刷新的布局
            if (mType == BaseWatchView.TYPE_RECOMMEND) {
                if (position == 0) {
                    if (holder is FeedRecommendTagHolder) {
                        holder.bindData(mRecommendList)
                    }
                } else {
                    if (!mDataList.isNullOrEmpty() && (position - 1) < mDataList.size) {
                        bindDataFeedViewHolder(holder, position, mDataList[position - 1])
                    }
                }
            } else {
                if (!mDataList.isNullOrEmpty() && position < mDataList.size) {
                    bindDataFeedViewHolder(holder, position, mDataList[position])
                }
            }
        } else {
            // 局部刷新
            val refreshType = payloads[0] as Int
            if (refreshType == REFRESH_SHOW_COMPLETE_AREA) {
                if (holder is FeedsWatchViewHolder) {
                    holder.showCompleteArea()
                    holder.stopPlay(false)
                }
            } else if (refreshType == REFRESH_HIDE_COMPLETE_AREA) {
                if (holder is FeedsWatchViewHolder) {
                    holder.hideCompleteArea()
                }
            }

            if (holder is FeedViewHolder) {
                if (refreshType == REFRESH_TYPE_PLAY) {
                    MyLog.d("FeedsWatchViewAdapter", "notifyItemChanged startPlay position = $position type=$refreshType mCurrentPlayModel=$mCurrentPlayModel mCurrentPlayPosition=$mCurrentPlayPosition playing=$playing")
                    if (mType == BaseWatchView.TYPE_RECOMMEND) {
                        if (mDataList[position - 1] == mCurrentPlayModel && playing) {
                            holder.startPlay()
                        } else {
                            holder.stopPlay(true)
                        }
                    } else {
                        if (mDataList[position] == mCurrentPlayModel && playing) {
                            holder.startPlay()
                        } else {
                            holder.stopPlay(true)
                        }
                    }
                } else if (refreshType == REFRESH_TYPE_LIKE) {
                    holder.refreshLike()
                } else if (refreshType == REFRESH_TYPE_PLAY_NUM) {
                    holder.refreshPlayNum()
                } else if (refreshType == REFRESH_TYPE_LYRIC) {
                    if (mType == BaseWatchView.TYPE_RECOMMEND) {
                        if (position - 1 >= 0 && position - 1 < mDataList.size && mDataList[position - 1] == mCurrentPlayModel) {
                            holder.playLyric()
                        }
                    } else {
                        if (mDataList[position] == mCurrentPlayModel) {
                            holder.playLyric()
                        }
                    }
                } else if (refreshType == REFRESH_BUFFERING_STATE) {
                    if (mType == BaseWatchView.TYPE_RECOMMEND) {
                        // 真实数据对应为position-1
                        if (position - 1 >= 0 && position - 1 < mDataList.size && mDataList[position - 1].song?.lyricStatus == 0) {
                            holder.pauseWhenBuffering()
                        } else {
                            holder.resumeWhenBufferingEnd()
                        }
                    } else {
                        if (mDataList[position].song?.lyricStatus == 0) {
                            holder.pauseWhenBuffering()
                        } else {
                            holder.resumeWhenBufferingEnd()
                        }
                    }
                } else if (refreshType == REFRESH_TYPE_COLLECT) {
                    holder.refreshCollects()
                }
            }
        }
    }

    private fun bindDataFeedViewHolder(holder: RecyclerView.ViewHolder, position: Int, feedsWatchModel: FeedsWatchModel) {
        if (holder is FeedViewHolder) {
            holder.bindData(position, feedsWatchModel)
            // notifyItemChanged 和 notifyDataSetChanged间隔时间很短一起使用时候，会导致notifyItemChanged不生效
            if (feedsWatchModel == mCurrentPlayModel && playing) {
                //todo 可以把播放状态需要做的操作都在这补一下
                MyLog.d("FeedsWatchViewAdapter", "notifyDataSetChanged startPlay")
                holder.startPlay()
            } else {
                holder.stopPlay(true)
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
            mHomeTopType -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_recommend_view_layout, parent, false)
                FeedRecommendTagHolder(view,listener)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_watch_item_holder_layout, parent, false)
                FeedsWatchViewHolder(view, listener)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (mType == BaseWatchView.TYPE_RECOMMEND) {
            mDataList.size + 1
        } else if (mType == BaseWatchView.TYPE_FOLLOW) {
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
        return if (mType == BaseWatchView.TYPE_RECOMMEND) {
            if (position == 0) {
                mHomeTopType
            } else {
                mHomeNormalItemType
            }
        } else if (mType == BaseWatchView.TYPE_FOLLOW) {
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
        if (mDataList.isNotEmpty()) {
            if (mType == BaseWatchView.TYPE_RECOMMEND) {
                if (position - 1 >= 0 && position - 1 < mDataList.size && mDataList[position - 1] == model) {
                    // 位置是对的
                    notifyItemChanged(position, refreshType)
                    return
                } else {
                    update(model, refreshType)
                }
            } else {
                if (position >= 0 && position < mDataList.size && mDataList[position] == model) {
                    // 位置是对的
                    notifyItemChanged(position, refreshType)
                    return
                } else {
                    update(model, refreshType)
                }
            }
        } else {
            mCurrentPlayModel = null
            mCurrentPlayPosition = null
        }
    }

    fun update(model: FeedsWatchModel?, refreshType: Int) {
        // 位置是错的
        if (mType == BaseWatchView.TYPE_RECOMMEND) {
            for (i in 0 until mDataList.size) {
                if (mDataList[i] == model) {
                    notifyItemChanged(i + 1, refreshType)
                    return
                }
            }
        } else {
            for (i in 0 until mDataList.size) {
                if (mDataList[i] == model) {
                    notifyItemChanged(i, refreshType)
                    return
                }
            }
        }

    }

    // 将从detail中数据放到data中
    fun updateModelFromDetail(model: FeedsWatchModel) {
        if (model != null) {
            if (mCurrentPlayModel?.feedID == model.feedID && mCurrentPlayModel?.song?.songID == model.song?.songID) {
                updateProperty(mCurrentPlayModel, model)
            }
            for (i in 0 until mDataList.size) {
                if (mDataList[i].feedID == model.feedID
                        && mDataList[i].song?.songID == model.song?.songID) {
                    updateProperty(mDataList[i], model)
                    if (mType == BaseWatchView.TYPE_RECOMMEND) {
                        notifyItemChanged(i + 1)
                    } else {
                        notifyItemChanged(i)
                    }
                }
            }
        }
    }

    private fun updateProperty(model: FeedsWatchModel?, update: FeedsWatchModel) {
        // 更新一下其中属性(几个可能变得数字)
        model?.commentCnt = update.commentCnt
        model?.isLiked = update.isLiked
        model?.starCnt = update.starCnt
        model?.shareCnt = update.shareCnt
        model?.isCollected = update.isCollected
    }

    fun delete(model: FeedsWatchModel) {
        mDataList.remove(model)
        notifyDataSetChanged()
        return
    }

    /**
     * 请求播放
     */
//    fun startPlayModel(pos: Int, model: FeedsWatchModel?) {
//        if (mCurrentPlayModel != model || !playing) {
//            val lastpost = mCurrentPlayPosition
//            mCurrentPlayModel = model
//            mCurrentPlayPosition = pos
//            playing = true
//
//            notifyDataSetChanged()
//        }
//    }

    fun startPlayModel(pos: Int, model: FeedsWatchModel?) {
        if (mCurrentPlayModel != model || !playing) {
            var lastPos: Int? = null
            if (mCurrentPlayModel != model) {
                mCurrentPlayModel = model
                lastPos = mCurrentPlayPosition
                mCurrentPlayPosition = pos
            }
            playing = true
            MyLog.d("FeedsWatchViewAdapter", "now pos=$pos")
            notifyItemChanged(pos, REFRESH_TYPE_PLAY)
            lastPos?.let {
                MyLog.d("FeedsWatchViewAdapter", "last pos=$it")
                uiHanlder.post {
                    notifyItemChanged(it, REFRESH_TYPE_PLAY)
                }
            }
        }
    }

    fun pausePlayModel() {
        if (playing) {
            playing = false
            update(mCurrentPlayPosition ?: 0, mCurrentPlayModel, REFRESH_TYPE_PLAY)
        }
    }

    fun playComplete() {
        MyLog.w("FeedsWatchViewAdapter", "playComplete playing=$playing")
        if (mType == BaseWatchView.TYPE_PERSON) {
            pausePlayModel()
        } else {
            if (playing) {
                playing = false
                update(mCurrentPlayPosition ?: 0, mCurrentPlayModel, REFRESH_SHOW_COMPLETE_AREA)
            }
        }

    }

    fun resumePlayModel() {
        MyLog.d("FeedsWatchViewAdapter", "resumePlayModel playing=$playing mCurrentPlayPosition=$mCurrentPlayPosition mCurrentPlayModel=$mCurrentPlayModel")
        if (mCurrentPlayModel != null && mCurrentPlayPosition != null) {
            if (!playing) {
                playing = true
                update(mCurrentPlayPosition ?: 0, mCurrentPlayModel, REFRESH_TYPE_PLAY)
            }
        }
    }

    fun pauseWhenBuffering() {
        if (mCurrentPlayModel != null && mCurrentPlayPosition != null) {
            mCurrentPlayModel?.song?.lyricStatus = 0
        }

        update(mCurrentPlayPosition ?: 0, mCurrentPlayModel, REFRESH_BUFFERING_STATE)
    }

    fun resumeWhenBufferingEnd() {
        if (mCurrentPlayModel != null && mCurrentPlayPosition != null) {
            mCurrentPlayModel?.song?.lyricStatus = 1
        }

        update(mCurrentPlayPosition ?: 0, mCurrentPlayModel, REFRESH_BUFFERING_STATE)
    }

    /**
     * 更新播放进度
     */
    fun updatePlayProgress(curPostion: Long, totalDuration: Long) {
        // 位置是错的
        mCurrentPlayModel?.song?.let {
            it.playCurPos = curPostion.toInt()
            it.playDurMs = totalDuration.toInt()
            it.playDurMsFromPlayerForDebug = totalDuration.toInt()
            /**
             * 这里更新会导致 头像url 不停地 的下载（通过代理抓包观察），原因未知
             * 所以暂时不用 notifyItemChanged 进行刷新
             */
            //update(mCurrentPlayPosition ?: 0, mCurrentPlayModel, REFRESH_TYPE_LYRIC)
        }
    }

    // 加入屏幕
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
    }

    // 移除屏幕
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is FeedsWatchViewHolder) {
            holder.hideCompleteArea()
        } else if (holder is FeedViewHolder) {
            holder.stopPlay(false)
        }
    }


    companion object {
        const val REFRESH_TYPE_PLAY = 0  // 局部刷新播放
        const val REFRESH_TYPE_LIKE = 1  // 局部刷新喜欢
        const val REFRESH_TYPE_PLAY_NUM = 2  // 局部刷新播放次数
        const val REFRESH_TYPE_LYRIC = 3  // 局部刷新歌词
        const val REFRESH_BUFFERING_STATE = 4  // 局部刷新歌词
        const val REFRESH_SHOW_COMPLETE_AREA = 5  // 显示局部播放结束画面
        const val REFRESH_HIDE_COMPLETE_AREA = 6  // 隐藏局部播放结束画面
        const val REFRESH_TYPE_COLLECT = 7 // 刷新是否收藏状态
    }
}