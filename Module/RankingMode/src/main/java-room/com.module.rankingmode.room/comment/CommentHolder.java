package com.module.rankingmode.room.comment;

import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.TextPaint;
import android.view.View;
import android.widget.TextView;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.rankingmode.R;
import com.module.rankingmode.song.model.SongModel;

public class CommentHolder extends RecyclerView.ViewHolder {
    public final static String TAG = "CommentHolder";

    BaseImageView mAvatarIv;
    ExTextView mCommentTv;

    CommentModel mCommentModel;
    int mPostion;
    RecyclerOnItemClickListener mRecyclerOnItemClickListener;

    public CommentHolder(View itemView) {
        super(itemView);
        mAvatarIv = (BaseImageView) itemView.findViewById(R.id.avatar_iv);
        mCommentTv = (ExTextView) itemView.findViewById(R.id.comment_tv);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecyclerOnItemClickListener != null) {
                    mRecyclerOnItemClickListener.onItemClicked(itemView, mPostion, mCommentModel);
                }
            }
        });
    }

    public void bind(int position, CommentModel model) {
        mPostion = position;
        mCommentModel = model;

        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(model.getAvatar())
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .setBorderColor(U.app().getResources().getColor(R.color.white))
                .build());
        mCommentTv.setText(model.getText());
        mCommentTv.setTextColor(model.getTextColor());

    }


    float getTextSize(TextView view) {
        CharSequence text = view.getText();
        TextPaint paint = view.getPaint();
        float textSize = (int) Layout.getDesiredWidth(text, 0, text.length(), paint);
        return textSize;
    }

    public void setListener(RecyclerOnItemClickListener recyclerOnItemClickListener) {
        mRecyclerOnItemClickListener = recyclerOnItemClickListener;
    }
}
