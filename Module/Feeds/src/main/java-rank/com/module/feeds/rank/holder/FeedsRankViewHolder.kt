package com.module.feeds.rank.holder

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.utils.U
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

        AvatarUtils.loadAvatarByUrl(mCoverIv, AvatarUtils.newParamsBuilder(model.userInfo?.avatar)
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                .setBorderColor(Color.WHITE)
                .build())
        mNameTv.text = model.rankTitle
        val remarkName = UserInfoManager.getInstance().getRemarkName(model.userInfo?.userID
                ?: 0, model.userInfo?.nickname)
        mOccupyTv.text = "$remarkName 占领"
        mJoinTv.text = "${model.userCnt}人参与"
    }
}