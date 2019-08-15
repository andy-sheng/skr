package com.module.feeds.watch.viewholder

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.log.MyLog
import com.common.player.SinglePlayer
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.common.utils.dp
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExConstraintLayout
import com.component.person.utils.StringFromatUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.feeds.R
import com.module.feeds.detail.view.AutoScrollLyricView
import com.module.feeds.detail.view.FeedsManyLyricView
import com.module.feeds.watch.listener.FeedsListener
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.view.FeedsRecordAnimationView

open class FeedViewHolder(var rootView: View, var listener: FeedsListener?) : RecyclerView.ViewHolder(rootView) {

    private val mMoreIv: ImageView = itemView.findViewById(R.id.more_iv)
    private val mTagArea: ExConstraintLayout = itemView.findViewById(R.id.tag_area)
    private val mTagTv: TextView = itemView.findViewById(R.id.tag_tv)
    //    private val mClassifySongTv: ExTextView = itemView.findViewById(R.id.classify_song_tv)
    val mSongAreaBg: SimpleDraweeView = itemView.findViewById(R.id.song_area_bg)
    val mRecordView: FeedsRecordAnimationView = itemView.findViewById(R.id.record_view)
    val mLikeNumTv: TextView = itemView.findViewById(R.id.like_num_tv)
    val mPlayNumTv: TextView = itemView.findViewById(R.id.play_num_tv)
    val mCollectIconTv: TextView = itemView.findViewById(R.id.collect_icon_tv)
    val mDebugTv: TextView = itemView.findViewById(R.id.debug_tv)
    val feedAutoScrollLyricView = AutoScrollLyricView(itemView.findViewById(R.id.auto_scroll_lyric_view_layout_viewstub))
    val feedWatchManyLyricView = FeedsManyLyricView(itemView.findViewById(R.id.feeds_watch_many_lyric_layout_viewstub))

    val mFeedsClickView: View = itemView.findViewById(R.id.feeds_click_view)

    var mPosition: Int = 0
    var model: FeedsWatchModel? = null

    init {
        mMoreIv.setOnClickListener(object : DebounceViewClickListener(){
            override fun clickValid(view: View?) {
                listener?.onClickMoreListener(mPosition, model)
            }
        })

        mLikeNumTv.setOnClickListener(object : AnimateClickListener() {
            override fun click(v: View?) {
                listener?.onClickLikeListener(mPosition, model)
            }
        })

        mRecordView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener?.onClickCDListener(mPosition, model)
            }
        })

        mTagArea.setOnClickListener(object : AnimateClickListener() {
            override fun click(v: View?) {
                StatisticsAdapter.recordCountEvent("music_recommend", "rank", null)
                listener?.onclickRankListener(model)
            }
        })

        itemView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener?.onClickDetailListener(mPosition, model)
            }
        })

        mFeedsClickView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener?.onClickDetailListener(mPosition, model)
            }
        })

        mCollectIconTv.setOnClickListener(object : AnimateClickListener() {
            override fun click(v: View?) {
                listener?.onClickCollectListener(mPosition, model)
            }
        })

        if (MyLog.isDebugLogOpen()) {
            mDebugTv.visibility = View.VISIBLE
        } else {
            mDebugTv.visibility = View.GONE
        }
    }

    open fun bindData(position: Int, watchModel: FeedsWatchModel) {
        this.mPosition = position
        this.model = watchModel

        watchModel.user?.let {
            AvatarUtils.loadAvatarByUrl(mSongAreaBg, AvatarUtils.newParamsBuilder(it.avatar)
                    .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                    .setBlur(true)
                    .build())
            mRecordView.setAvatar(it.avatar ?: "")
        }

        if (watchModel.rank != null) {
            if (TextUtils.isEmpty(watchModel.rank?.rankDesc)) {
                mTagArea.visibility = View.GONE
            } else {
                mTagTv.text = watchModel.rank?.rankDesc
                mTagArea.visibility = View.VISIBLE
            }
        } else {
            mTagArea.visibility = View.GONE
        }

        refreshPlayNum(position, watchModel)
        refreshLike(position, watchModel)
        refreshCollects(position, watchModel)

        // 加载带时间戳的歌词
        watchModel.song?.let {
            feedWatchManyLyricView.setSongModel(it, -1)
            feedAutoScrollLyricView.setSongModel(it, -1)
        }
        // 加载歌词
        if (!TextUtils.isEmpty(model?.song?.songTpl?.lrcTs) && model?.song?.songType == 1) {
            feedAutoScrollLyricView.visibility = View.GONE
            feedWatchManyLyricView.loadLyric()
        } else {
            feedAutoScrollLyricView.loadLyric()
            feedWatchManyLyricView.visibility = View.GONE
        }
        tryBindDebugView()
    }

    // 刷新喜欢图标和数字
    fun refreshLike(position: Int, watchModel: FeedsWatchModel) {
        this.mPosition = position
        this.model = watchModel

        var drawble = U.getDrawable(R.drawable.feed_like_normal_icon)
        if (watchModel.isLiked) {
            drawble = U.getDrawable(R.drawable.feed_like_selected_icon)
        }
        drawble.setBounds(0, 0, 20.dp(), 18.dp())
        mLikeNumTv.setCompoundDrawables(drawble, null, null, null)
        mLikeNumTv.text = StringFromatUtils.formatTenThousand(watchModel.starCnt)
    }

    // 刷新播放次数
    fun refreshPlayNum(position: Int, watchModel: FeedsWatchModel) {
        this.mPosition = position
        this.model = watchModel
        mPlayNumTv.text = "${StringFromatUtils.formatTenThousand(watchModel.exposure)} 播放"
    }

    // 刷新收藏状态
    open fun refreshCollects(position: Int, watchModel: FeedsWatchModel) {
        this.mPosition = position
        this.model = watchModel

        var drawble = U.getDrawable(R.drawable.feed_not_collection)
        if (watchModel.isCollected) {
            drawble = U.getDrawable(R.drawable.feed_collect_selected_icon)
        }
        drawble.setBounds(0, 0, 20.dp(), 18.dp())
        mCollectIconTv.setCompoundDrawables(drawble, null, null, null)
    }


    open fun startPlay() {
        mRecordView.play(SinglePlayer.isBufferingOk)
    }

    fun playLyric(position: Int, watchModel: FeedsWatchModel) {
        this.mPosition = position
        this.model = watchModel
        // 播放歌词 不一定是从头开始播
        // 有可能从头播 也有可能继续播
        if (!TextUtils.isEmpty(model?.song?.songTpl?.lrcTs) && model?.song?.songType == 1) {
            feedAutoScrollLyricView.visibility = View.GONE
            feedWatchManyLyricView.seekTo(model?.song?.playCurPos ?: 0)
            feedWatchManyLyricView.resume()
        } else {
            feedAutoScrollLyricView.seekTo(model?.song?.playCurPos ?: 0)
            feedAutoScrollLyricView.resume()
            feedWatchManyLyricView.visibility = View.GONE
        }
        tryBindDebugView()
    }

    private fun tryBindDebugView() {
        if (MyLog.isDebugLogOpen()) {
            mDebugTv.text = "uid:${model?.user?.userID}\n" +
                    "feedId:${model?.feedID}\n" +
                    "songId:${model?.song?.songID}\n" +
                    "songtype:${model?.song?.songType}\n" +
                    "playDurMs:${model?.song?.playDurMs} \n" +
                    "${model?.song?.playCurPos}/${model?.song?.playDurMsFromPlayerForDebug}\n" +
                    "${model?.song?.playURL}"
        }
    }

    fun pauseWhenBuffering(position: Int, watchModel: FeedsWatchModel) {
        this.mPosition = position
        this.model = watchModel
        if (!TextUtils.isEmpty(model?.song?.songTpl?.lrcTs) && model?.song?.songType == 1) {
            feedWatchManyLyricView.pause()
        } else {
            feedAutoScrollLyricView.pause()
        }
        mRecordView.buffering()
    }

    fun resumeWhenBufferingEnd(position: Int, watchModel: FeedsWatchModel) {
        this.mPosition = position
        this.model = watchModel
        if (!TextUtils.isEmpty(model?.song?.songTpl?.lrcTs) && model?.song?.songType == 1) {
            feedWatchManyLyricView.resume()
        } else {
            feedAutoScrollLyricView.resume()
        }
        mRecordView.bufferEnd()
    }

    fun stopPlay(useAnimation: Boolean) {
        if (useAnimation) {
            mRecordView.pause()
        } else {
            mRecordView.pauseWithNoAnimation()
        }
        feedAutoScrollLyricView.pause()
        feedWatchManyLyricView.pause()
    }
}