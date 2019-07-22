package com.module.feeds.watch.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.feeds.R
import com.module.feeds.watch.model.FeedsLikeModel

class FeedsLikeViewHolder(item: View, var onClickPlayListener: ((model: FeedsLikeModel?) -> Unit)?) : RecyclerView.ViewHolder(item) {

    val mSongCoverSdv: SimpleDraweeView = item.findViewById(R.id.song_cover_sdv);
    val mSongNameTv: TextView = item.findViewById(R.id.song_name_tv)
    val mSongWriterTv: TextView = item.findViewById(R.id.song_writer_tv)
    val mSongPlayIv: ExImageView = item.findViewById(R.id.song_play_iv)

    var mModel: FeedsLikeModel? = null
    var mPosition: Int = 0

    init {
        mSongPlayIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickPlayListener?.invoke(mModel)
            }
        })
    }

    fun bindData(position: Int, likeModel: FeedsLikeModel) {
        this.mPosition = position
        this.mModel = likeModel
        AvatarUtils.loadAvatarByUrl(mSongCoverSdv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                .build())
    }
}