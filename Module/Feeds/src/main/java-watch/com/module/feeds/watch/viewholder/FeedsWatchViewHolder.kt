package com.module.feeds.watch.viewholder

import android.support.constraint.Group
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.statistics.StatisticsAdapter
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.feeds.R
import com.module.feeds.statistics.FeedsPlayStatistics
import com.module.feeds.watch.listener.FeedsListener
import com.module.feeds.watch.model.FeedsWatchModel

open class FeedsWatchViewHolder(it: View, l: FeedsListener?) : FeedViewHolder(it, l) {

    private val mAvatarIv: SimpleDraweeView = itemView.findViewById(R.id.avatar_iv)
    private val mNicknameTv: TextView = itemView.findViewById(R.id.nickname_tv)
    private val mTimeTv: TextView = itemView.findViewById(R.id.time_tv)
    private val mContentTv: TextView = itemView.findViewById(R.id.content_tv)
    private val mHitIv: ImageView = itemView.findViewById(R.id.hit_iv)

    private val mCompleteGruop: Group = itemView.findViewById(R.id.complete_gruop)
    private val mCompleteAreaIv: ExImageView = itemView.findViewById(R.id.complete_area_iv)
    private val mShareTv: ExTextView = itemView.findViewById(R.id.share_tv)
    private val mCollectTv: ExTextView = itemView.findViewById(R.id.collect_tv)
    private val mPlayAgainTv: ExTextView = itemView.findViewById(R.id.play_again_tv)

    init {
        mHitIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                StatisticsAdapter.recordCountEvent("music_recommend", "challenge", null)
                listener?.onClickHitListener(model)
            }
        })

        mAvatarIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener?.onClickAvatarListener(model)
            }
        })

        mShareTv.setOnClickListener(object : AnimateClickListener() {
            override fun click(v: View?) {
                listener?.onClickShareListener(mPosition, model)
            }
        })

        mCollectTv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                listener?.onClickCollectListener(mPosition, model)
            }
        })

        mPlayAgainTv.setOnClickListener(object : AnimateClickListener() {
            override fun click(v: View?) {
                // 触发上一次打点统计
                FeedsPlayStatistics.setCurPlayMode(0)
                listener?.onClickCDListener(mPosition, model)
            }
        })
    }

    override fun bindData(position: Int, watchModel: FeedsWatchModel) {
        super.bindData(position, watchModel)
        watchModel.user?.let {
            AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(it.avatar)
                    .setCircle(true)
                    .build())
            mNicknameTv.text = UserInfoManager.getInstance().getRemarkName(it.userID
                    ?: 0, it.nickname)
        }

        if (watchModel.song?.needChallenge == true) {
            mHitIv.visibility = View.VISIBLE
        } else {
            mHitIv.visibility = View.GONE
        }

        mTimeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(watchModel.song?.createdAt
                ?: 0L, System.currentTimeMillis())
        var recomendTag = ""
        if (watchModel.song?.needRecommentTag == true) {
            recomendTag = "#小编推荐# "
        }
        var songTag = ""
        watchModel.song?.tags?.let {
            for (model in it) {
                model?.tagDesc.let { tagDesc ->
                    songTag = "$songTag#$tagDesc# "
                }
            }
        }
        val title = watchModel.song?.title ?: ""
        if (TextUtils.isEmpty(recomendTag) && TextUtils.isEmpty(songTag) && TextUtils.isEmpty(title)) {
            mContentTv.visibility = View.GONE
        } else {
            val stringBuilder = SpanUtils()
                    .append(recomendTag).setForegroundColor(U.getColor(R.color.black_trans_50))
                    .append(songTag).setForegroundColor(U.getColor(R.color.black_trans_50))
                    .append(title).setForegroundColor(U.getColor(R.color.black_trans_80))
                    .create()
            mContentTv.visibility = View.VISIBLE
            mContentTv.text = stringBuilder
        }
    }

    override fun startPlay() {
        super.startPlay()
        hideCompleteArea()
    }

    fun showCompleteArea(position: Int, watchModel: FeedsWatchModel) {
        this.mPosition = position
        this.model = watchModel

        mCompleteGruop.visibility = View.VISIBLE
        if (watchModel.isCollected) {
            mCollectTv.text = "取消收藏"
        } else {
            mCollectTv.text = "收藏"
        }
    }

    fun hideCompleteArea() {
        mCompleteGruop.visibility = View.GONE
    }

    override fun refreshCollects(position: Int, watchModel: FeedsWatchModel) {
        super.refreshCollects(position, watchModel)
        if (watchModel.isCollected) {
            mCollectTv.text = "取消收藏"
        } else {
            mCollectTv.text = "收藏"
        }
    }
}