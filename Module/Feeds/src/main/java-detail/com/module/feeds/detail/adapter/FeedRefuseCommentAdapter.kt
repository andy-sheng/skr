package com.module.feeds.detail.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.core.avatar.AvatarUtils
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.common.view.recyclerview.DiffAdapter
import com.module.feeds.R
import com.module.feeds.detail.model.RefuseCommentModel


class FeedRefuseCommentAdapter : DiffAdapter<RefuseCommentModel, FeedRefuseCommentAdapter.FeedRefuseCommentHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedRefuseCommentHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.refuse_comment_item_layout, parent, false)
        return FeedRefuseCommentHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: FeedRefuseCommentHolder, position: Int) {
        holder.bindData(mDataList[position])
    }

    inner class FeedRefuseCommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mAvatarIv: BaseImageView
        val mTitleTv: ExTextView
        val mSubTitleTv: ExTextView
        val mCommentTv: ExTextView
        val mTimeTv: ExTextView
        var model: RefuseCommentModel? = null

        init {
            mAvatarIv = itemView.findViewById(R.id.avatar_iv)
            mTitleTv = itemView.findViewById(R.id.title_tv)
            mSubTitleTv = itemView.findViewById(R.id.sub_title_tv)
            mCommentTv = itemView.findViewById(R.id.comment_tv)
            mTimeTv = itemView.findViewById(R.id.time_tv)
        }

        fun bindData(model: RefuseCommentModel) {
            this.model = model
            AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(model.avatar)
                    .setBorderColor(Color.WHITE)
                    .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                    .setCircle(true)
                    .build())

            mTitleTv.text = model.nickname
            mSubTitleTv.text = model.actionDesc
            mCommentTv.text = model.content
            mTimeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(model.timeMs, System.currentTimeMillis())
        }
    }
}