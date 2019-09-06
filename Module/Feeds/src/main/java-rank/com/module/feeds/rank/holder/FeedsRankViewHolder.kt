package com.module.feeds.rank.holder

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.utils.U
import com.common.utils.dp
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.facebook.drawee.view.SimpleDraweeView
import com.module.feeds.R
import com.module.feeds.rank.adapter.FeedRankAdapter
import com.module.feeds.rank.model.FeedRankInfoModel

class FeedsRankViewHolder(item: View, val listener: FeedRankAdapter.Listener) : RecyclerView.ViewHolder(item) {

    val mCoverIv: SimpleDraweeView = item.findViewById(R.id.cover_iv)
    val mHitIv: ImageView = item.findViewById(R.id.hit_iv)
    val mNameTv: TextView = item.findViewById(R.id.name_tv)
    val mOccupyTv: TextView = item.findViewById(R.id.occupy_tv)
    val mJoinTv: TextView = item.findViewById(R.id.join_tv)

    var mModel: FeedRankInfoModel? = null
    var mPosition: Int = 0

    init {
        item.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener.onClickItem(mPosition, mModel)
            }
        })

        mHitIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(v: View?) {
                listener.onClickHit(mPosition, mModel)
            }
        })
    }

    fun bindData(position: Int, model: FeedRankInfoModel) {
        this.mPosition = position
        this.mModel = model
        if (model.userInfo != null) {
            AvatarUtils.loadAvatarByUrl(mCoverIv, AvatarUtils.newParamsBuilder(model.userInfo?.avatar)
                    .setCornerRadius(8.dp().toFloat())
                    .build())
            val remarkName = UserInfoManager.getInstance().getRemarkName(model.userInfo?.userId
                    ?: 0, model.userInfo?.nickname)
            mOccupyTv.text = "$remarkName 占领"
        } else {
            FrescoWorker.loadImage(mCoverIv,
                    ImageFactory.newResImage(R.drawable.feed_rank_empty_avatar)
                            .setCornerRadius(8.dp().toFloat())
                            .build<BaseImage>())
            mOccupyTv.text = "无人占领"
        }

        mNameTv.text = model.rankTitle
        mJoinTv.text = "${model.userCnt}人参与"
    }
}