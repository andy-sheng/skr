package com.module.playways.rank.room.quickmsg;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.rank.R;

public class QuickMsgHolder extends RecyclerView.ViewHolder {
    ExTextView mCommentTv;

    QuickMsgModel mQuickMsgModel;

    int mPostion;

    RecyclerOnItemClickListener mRecyclerOnItemClickListener;

    public QuickMsgHolder(View itemView) {
        super(itemView);
        mCommentTv = (ExTextView) itemView.findViewById(R.id.comment_tv);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecyclerOnItemClickListener != null) {
                    mRecyclerOnItemClickListener.onItemClicked(itemView, mPostion, mQuickMsgModel);
                }
            }
        });
    }

    public void bind(int position, QuickMsgModel model) {
        mPostion = position;
        mQuickMsgModel = model;
        mCommentTv.setText(model.getText());
    }

    public void setListener(RecyclerOnItemClickListener recyclerOnItemClickListener) {
        mRecyclerOnItemClickListener = recyclerOnItemClickListener;
    }
}
