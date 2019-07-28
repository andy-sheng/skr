package com.module.feeds.rank.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.module.feeds.watch.model.FeedsWatchModel
import com.facebook.drawee.view.SimpleDraweeView
import com.module.feeds.R

class FeedsDetailRankViewHolder(item: View, onClickPlayListener: ((model: FeedsWatchModel?, position: Int) -> Unit)?) : RecyclerView.ViewHolder(item) {

    var mPosition: Int = 0
    var mModel: FeedsWatchModel? = null

    private val mSongCoverSdv: SimpleDraweeView = item.findViewById(R.id.song_cover_sdv)
    private val mSongNameTv: TextView = item.findViewById(R.id.song_name_tv)
    private val mLikeNumTv: TextView = item.findViewById(R.id.like_num_tv)
    val mSongPlayIv: ExImageView = item.findViewById(R.id.song_play_iv)

    init {
        mSongPlayIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickPlayListener?.invoke(mModel, mPosition)
            }
        })
    }

    fun bindData(position: Int, model: FeedsWatchModel) {
        this.mPosition = position
        this.mModel = model

        mLikeNumTv.text = "${model.starCnt}"
        model.user?.let {
            AvatarUtils.loadAvatarByUrl(mSongCoverSdv, AvatarUtils.newParamsBuilder(it.avatar)
                    .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                    .build())
            mSongNameTv.text = UserInfoManager.getInstance().getRemarkName(it.userID
                    ?: 0, it.nickname)
        }
    }
}