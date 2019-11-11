package com.module.playways.room.room.comment.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.component.busilib.view.AvatarView;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.room.room.comment.adapter.CommentAdapter;
import com.module.playways.room.room.comment.model.CommentDynamicModel;
import com.module.playways.R;

public class CommentDynamicHolder extends RecyclerView.ViewHolder {
    AvatarView mAvatarIv;
    SimpleDraweeView mCommentSdv;

    int position;
    CommentDynamicModel commentModel;

    public CommentDynamicHolder(View itemView, CommentAdapter.CommentAdapterListener mCommentItemListener) {
        super(itemView);

        mAvatarIv = itemView.findViewById(R.id.avatar_iv);
        mCommentSdv = itemView.findViewById(R.id.comment_sdv);

        mAvatarIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mCommentItemListener != null && !commentModel.isFake()) {
                    mCommentItemListener.clickAvatar(commentModel.getUserInfo().getUserId());
                }
            }
        });
    }


    public void bind(int position, CommentDynamicModel commentModel) {
        this.position = position;
        this.commentModel = commentModel;

        mAvatarIv.bindData(commentModel.getUserInfo());
        FrescoWorker.loadImage(mCommentSdv, ImageFactory.newPathImage(commentModel.getDynamicModel().getBigEmojiURL())
                .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                .setFitDrawable(true)
                .build());
    }
}
