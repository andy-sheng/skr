package com.module.feeds.watch.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.feeds.R
import com.module.feeds.watch.adapter.FeedCollectListener
import com.module.feeds.watch.model.FeedsCollectModel

open class FeedsCollectViewHolder(item: View, val listener: FeedCollectListener) : RecyclerView.ViewHolder(item) {

    private val mSongCoverSdv: SimpleDraweeView = item.findViewById(R.id.song_cover_sdv)
    val mSongNameTv: TextView = item.findViewById(R.id.song_name_tv)
    private val mSongWriterTv: TextView = item.findViewById(R.id.song_writer_tv)
    val mSongPlayIv: ExImageView = item.findViewById(R.id.song_play_iv)

    var mModel: FeedsCollectModel? = null
    var mPosition: Int = 0

    init {
        //TODO 为什么itemView的点击事件覆盖不了ExImageView
        mSongPlayIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener.onClickPlayListener(mModel, mPosition)
            }
        })

        itemView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener.onClickItemListener(mModel, mPosition)
            }
        })
    }

    fun bindData(position: Int, likeModel: FeedsCollectModel) {
        this.mPosition = position
        if (likeModel != mModel) {
            this.mModel = likeModel

            mSongNameTv.text = likeModel.song?.workName
            mModel?.user?.let {
                AvatarUtils.loadAvatarByUrl(mSongCoverSdv, AvatarUtils.newParamsBuilder(it.avatar)
                        .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                        .build())
                mSongWriterTv.text = UserInfoManager.getInstance().getRemarkName(it.userID
                        ?: 0, it.nickname)
            }
        } else {
            // do nothing
        }

    }
}