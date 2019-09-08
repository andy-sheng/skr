package com.module.playways.room.room.comment.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.component.busilib.view.AvatarView;
import com.module.playways.room.room.comment.adapter.CommentAdapter;
import com.module.playways.room.room.comment.listener.CommentViewItemListener;
import com.module.playways.room.room.comment.model.CommentModel;
import com.module.playways.R;

public class CommentHolder extends RecyclerView.ViewHolder {
    public final String TAG = "CommentHolder";

    AvatarView mAvatarIv;
    ExTextView mCommentTv;

    CommentModel mCommentModel;
    int mPostion;

    public CommentHolder(View itemView, CommentAdapter.CommentAdapterListener mCommentItemListener) {
        super(itemView);

        mAvatarIv = itemView.findViewById(R.id.avatar_iv);
        mCommentTv = itemView.findViewById(R.id.comment_tv);
        mAvatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCommentItemListener != null) {
                    mCommentItemListener.clickAvatar(mCommentModel.getUserInfo().getUserId());
                }
            }
        });
    }

    public void bind(int position, CommentModel model) {
        mPostion = position;
        mCommentModel = model;

        mAvatarIv.bindData(model.getUserInfo());
        mCommentTv.setText(model.getStringBuilder());
    }
}
