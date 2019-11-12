package com.module.playways.room.room.comment.holder


import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import com.common.core.myinfo.MyUserInfoManager

import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.busilib.view.AvatarView
import com.component.level.utils.LevelConfigUtils
import com.module.playways.room.room.comment.adapter.CommentAdapter
import com.module.playways.room.room.comment.model.CommentModel
import com.module.playways.R

class CommentHolder(itemView: View, mCommentItemListener: CommentAdapter.CommentAdapterListener?) : RecyclerView.ViewHolder(itemView) {
    val TAG = "CommentHolder"

    private val mAvatarIv: AvatarView = itemView.findViewById(R.id.avatar_iv)
    private val mCommentTv: ExTextView = itemView.findViewById(R.id.comment_tv)

    private var mCommentModel: CommentModel? = null
    private var mPostion: Int = 0

    init {
        mAvatarIv.setOnClickListener {
            if (mCommentModel?.isFake == false) {
                mCommentModel?.userInfo?.userId?.let {
                    mCommentItemListener?.clickAvatar(it)
                }
            }
        }
    }

    fun bind(position: Int, model: CommentModel) {
        mPostion = position
        mCommentModel = model

        if (model.isFake) {
            mAvatarIv.bindData(model.userInfo, model.fakeUserInfo?.nickName, model.fakeUserInfo?.avatarUrl)
        } else {
            mAvatarIv.bindData(model.userInfo, model.fakeUserInfo?.nickName, model.userInfo?.avatar)
        }

        // 为了保证书写从左到右
        val spanUtils = SpanUtils().append("\u202D")
        if (model.userInfo != null
                && model.userInfo!!.ranking != null
                && LevelConfigUtils.getSmallImageResoucesLevel(model.userInfo!!.ranking.mainRanking) > 0) {
            val drawable = U.getDrawable(LevelConfigUtils.getSmallImageResoucesLevel(model.userInfo!!.ranking.mainRanking))
            drawable.setBounds(0, 0, U.getDisplayUtils().dip2px(22f), U.getDisplayUtils().dip2px(19f))
            spanUtils.appendImage(drawable, SpanUtils.ALIGN_CENTER)
        }
        if (!TextUtils.isEmpty(model.nameBuilder)) {
            spanUtils.append(model.nameBuilder!!)
        }

        if (model.userInfo!!.honorInfo != null && model.userInfo!!.honorInfo.isHonor()) {
            val honorDrawable = U.getDrawable(R.drawable.person_honor_icon)
            honorDrawable.setBounds(0, 0, U.getDisplayUtils().dip2px(23f), U.getDisplayUtils().dip2px(14f))
            spanUtils.appendImage(honorDrawable, SpanUtils.ALIGN_CENTER).append(" ")
        }

        if (!TextUtils.isEmpty(model.stringBuilder)) {
            spanUtils.append(model.stringBuilder!!)
        }
        spanUtils.append("\u202C")
        mCommentTv.text = spanUtils.create()
    }
}
