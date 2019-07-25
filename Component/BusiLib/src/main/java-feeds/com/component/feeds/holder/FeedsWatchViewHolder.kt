package com.component.feeds.holder

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.component.busilib.R
import com.component.busilib.view.BitmapTextView
import com.component.feeds.listener.FeedsListener
import com.component.feeds.model.FeedsWatchModel
import com.facebook.drawee.view.SimpleDraweeView

open class FeedsWatchViewHolder(it: View, l: FeedsListener?) : FeedViewHolder(it, l) {

    private val mAvatarIv: SimpleDraweeView = itemView.findViewById(R.id.avatar_iv)
    private val mNicknameTv: TextView = itemView.findViewById(R.id.nickname_tv)
    private val mTimeTv: TextView = itemView.findViewById(R.id.time_tv)
    private val mContentTv: TextView = itemView.findViewById(R.id.content_tv)
    private val mPeopleNumTv: BitmapTextView = itemView.findViewById(R.id.people_num_tv)
    private val mHitIv: ImageView = itemView.findViewById(R.id.hit_iv)

    init {
        mHitIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener?.onClickHitListener(model)
            }
        })
    }

    override fun bindData(position: Int, watchModel: FeedsWatchModel) {
        super.bindData(position, watchModel)
        model?.user?.let {
            AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(it.avatar)
                    .setCircle(true)
                    .build())
            mNicknameTv.text = UserInfoManager.getInstance().getRemarkName(it.userID, it.nickname)
        }

        mPeopleNumTv.setText(watchModel.challengeCnt.toString())
        mTimeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(model?.song?.createdAt!!, System.currentTimeMillis())
        if (!TextUtils.isEmpty(watchModel.song?.title)) {
            mContentTv.text = watchModel.song?.title
            mContentTv.visibility = View.VISIBLE
        } else {
            mContentTv.visibility = View.GONE
        }
    }
}