package com.module.feeds.rank.holder

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
import com.module.feeds.watch.model.FeedsWatchModel

class FeedsDetailOneViewHolder(rootView: View, onClickPlayListener: ((model: FeedsWatchModel?, position: Int) -> Unit)?) : RecyclerView.ViewHolder(rootView) {

    val mSongAreaBg: SimpleDraweeView = itemView.findViewById(R.id.song_area_bg)
    val mRecordCover: SimpleDraweeView = itemView.findViewById(R.id.record_cover)
    val mSongPlayIv: ExImageView = itemView.findViewById(R.id.song_play_iv)
    val mNameTv: TextView = itemView.findViewById(R.id.name_tv)
    val mLikeNumTv: TextView = itemView.findViewById(R.id.like_num_tv)

    var mModel: FeedsWatchModel? = null
    var mPosition: Int = 0

    init {
        itemView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickPlayListener?.invoke(mModel, mPosition)
            }
        })
        mSongPlayIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickPlayListener?.invoke(mModel, mPosition)
            }
        })
    }

    fun bindData(position: Int, model: FeedsWatchModel) {
        this.mPosition = position
        this.mModel = model

        model?.user?.let {
            AvatarUtils.loadAvatarByUrl(mSongAreaBg, AvatarUtils.newParamsBuilder(it.avatar)
                    .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                    .setBlur(true)
                    .build())
            AvatarUtils.loadAvatarByUrl(mRecordCover, AvatarUtils.newParamsBuilder(it.avatar).setCircle(true).build())
            mNameTv.text = UserInfoManager.getInstance().getRemarkName(it.userID, it.nickname)
        }
        mLikeNumTv.text = model.starCnt.toString()
    }
}