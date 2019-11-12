package com.module.playways.room.room.comment.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.common.core.myinfo.MyUserInfoManager

import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.view.DebounceViewClickListener
import com.component.busilib.view.AvatarView
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.room.room.comment.adapter.CommentAdapter
import com.module.playways.room.room.comment.model.CommentDynamicModel
import com.module.playways.R

class CommentDynamicHolder(itemView: View, mCommentItemListener: CommentAdapter.CommentAdapterListener?) : RecyclerView.ViewHolder(itemView) {

    private val mAvatarIv: AvatarView = itemView.findViewById(R.id.avatar_iv)
    private val mCommentSdv: SimpleDraweeView = itemView.findViewById(R.id.comment_sdv)

    internal var position: Int = 0
    internal var commentModel: CommentDynamicModel? = null

    init {
        mAvatarIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (commentModel?.isFake == false) {
                    mCommentItemListener?.clickAvatar(commentModel?.userInfo?.userId ?: 0)
                }
            }
        })
    }


    fun bind(position: Int, commentModel: CommentDynamicModel) {
        this.position = position
        this.commentModel = commentModel

        if (commentModel.isFake) {
            mAvatarIv.bindData(commentModel.userInfo, commentModel.fakeUserInfo?.nickName, commentModel.fakeUserInfo?.avatarUrl)
        } else {
            mAvatarIv.bindData(commentModel.userInfo, commentModel.fakeUserInfo?.nickName, commentModel.userInfo?.avatar)
        }
        FrescoWorker.loadImage(mCommentSdv, ImageFactory.newPathImage(commentModel.dynamicModel!!.bigEmojiURL)
                .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                .setFitDrawable(true)
                .build())
    }
}
