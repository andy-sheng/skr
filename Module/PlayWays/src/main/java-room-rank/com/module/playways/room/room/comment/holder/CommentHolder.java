package com.module.playways.room.room.comment.holder;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserLevelType;
import com.common.image.fresco.BaseImageView;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.component.busilib.view.AvatarView;
import com.component.level.utils.LevelConfigUtils;
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
                if (mCommentItemListener != null && mCommentModel != null && mCommentModel.getFakeUserInfo() == null) {
                    mCommentItemListener.clickAvatar(mCommentModel.getUserInfo().getUserId());
                }
            }
        });
    }

    public void bind(int position, CommentModel model) {
        mPostion = position;
        mCommentModel = model;

        mAvatarIv.bindData(model.getUserInfo());

        // 为了保证书写从左到右
        SpanUtils spanUtils = new SpanUtils().append("\u202D");
        if (model.getUserInfo() != null
                && model.getUserInfo().getRanking() != null
                && LevelConfigUtils.getSmallImageResoucesLevel(model.getUserInfo().getRanking().getMainRanking()) > 0) {
            Drawable drawable = U.getDrawable(LevelConfigUtils.getSmallImageResoucesLevel(model.getUserInfo().getRanking().getMainRanking()));
            drawable.setBounds(0, 0, U.getDisplayUtils().dip2px(22), U.getDisplayUtils().dip2px(19));
            spanUtils.appendImage(drawable, SpanUtils.ALIGN_CENTER);
        }
        if (!TextUtils.isEmpty(model.getNameBuilder())) {
            spanUtils.append(model.getNameBuilder());
        }

        if (model.getUserInfo().getHonorInfo() != null && model.getUserInfo().getHonorInfo().isHonor()) {
            Drawable honorDrawable = U.getDrawable(R.drawable.person_honor_icon);
            honorDrawable.setBounds(0, 0, U.getDisplayUtils().dip2px(23), U.getDisplayUtils().dip2px(14));
            spanUtils.appendImage(honorDrawable, SpanUtils.ALIGN_CENTER).append(" ");
        }

        if (!TextUtils.isEmpty(model.getStringBuilder())) {
            spanUtils.append(model.getStringBuilder());
        }
        spanUtils.append("\u202C");
        mCommentTv.setText(spanUtils.create());
    }
}
