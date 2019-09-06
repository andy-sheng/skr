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
import com.component.busilib.view.AvatarView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.feeds.R
import com.module.feeds.statistics.FeedPage
import com.module.feeds.statistics.FeedsPlayStatistics
import com.module.feeds.watch.listener.FeedsListener
import com.module.feeds.watch.model.FeedsWatchModel

open class FeedsWatchViewHolder(it: View, l: FeedsListener?) : FeedViewHolder(it, l) {

    private val mAvatarIv: AvatarView = itemView.findViewById(R.id.avatar_iv)
    private val mNicknameTv: TextView = itemView.findViewById(R.id.nickname_tv)
    //    private val mTimeTv: TextView = itemView.findViewById(R.id.time_tv)
    private val mHitIv: ImageView = itemView.findViewById(R.id.hit_iv)

    private val mCompleteGruop: Group = itemView.findViewById(R.id.complete_gruop)
    private val mCompleteAreaIv: ExImageView = itemView.findViewById(R.id.complete_area_iv)
    private val mShareTv: ExTextView = itemView.findViewById(R.id.share_tv)
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

        mPlayAgainTv.setOnClickListener(object : AnimateClickListener() {
            override fun click(v: View?) {
                // 触发上一次打点统计
                FeedsPlayStatistics.setCurPlayMode(0, FeedPage.UNKNOW, 0)
                listener?.onClickCDListener(mPosition, model)
            }
        })
    }

    override fun bindData(position: Int, watchModel: FeedsWatchModel) {
        super.bindData(position, watchModel)
        watchModel.user?.let {
            mAvatarIv.bindData(it)
            mNicknameTv.text = UserInfoManager.getInstance().getRemarkName(it.userId, it.nickname)
        }

        if (watchModel.song?.needShareTag == true) {
            mHitIv.visibility = View.GONE
        } else {
            if (watchModel.song?.needChallenge == true) {
                mHitIv.visibility = View.VISIBLE
            } else {
                mHitIv.visibility = View.GONE
            }
        }

//        mTimeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(watchModel.song?.createdAt
//                ?: 0L, System.currentTimeMillis())
    }

    override fun startPlay() {
        super.startPlay()
        hideCompleteArea()
    }

    fun showCompleteArea() {
        mCompleteGruop.visibility = View.VISIBLE
    }

    fun hideCompleteArea() {
        mCompleteGruop.visibility = View.GONE
    }
}