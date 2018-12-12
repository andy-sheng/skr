package com.module.rankingmode.room.comment;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.rankingmode.R;
import com.module.rankingmode.song.model.SongModel;

public class CommentHolder extends RecyclerView.ViewHolder {
    ExTextView mCommentTv;
    CommentModel mCommentModel;
    int mPostion;
    RecyclerOnItemClickListener mRecyclerOnItemClickListener;

    public CommentHolder(View itemView) {
        super(itemView);
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
        mCommentTv.setText(model.getText());
    }

    public void setListener(RecyclerOnItemClickListener recyclerOnItemClickListener) {
        mRecyclerOnItemClickListener = recyclerOnItemClickListener;
    }
}
