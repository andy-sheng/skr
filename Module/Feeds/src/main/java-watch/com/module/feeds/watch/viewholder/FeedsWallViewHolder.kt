package com.module.feeds.watch.viewholder

import android.view.View
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.U
import com.module.feeds.R
import com.module.feeds.watch.listener.FeedsListener
import com.module.feeds.watch.model.FeedsWatchModel

class FeedsWallViewHolder(it: View, l: FeedsListener) : FeedViewHolder(it, l) {

    private val mCheckTv: TextView = itemView.findViewById(R.id.check_tv);

    override fun bindData(position: Int, watchModel: FeedsWatchModel) {
        super.bindData(position, watchModel)

        if (watchModel.status == 2) {
            // 审核通过
            mCommentNumTv.visibility = View.VISIBLE
            mLikeNumTv.visibility = View.VISIBLE
            mCheckTv.visibility = View.GONE
        } else {
            // 未通过
            mCommentNumTv.visibility = View.GONE
            mLikeNumTv.visibility = View.GONE
            mCheckTv.visibility = View.VISIBLE
        }
    }


}