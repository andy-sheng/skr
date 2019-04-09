package com.module.playways.rank.room.comment.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.DiffAdapter;
import com.component.busilib.constans.GameModeType;
import com.module.playways.rank.room.comment.holder.CommentHolder;
import com.module.playways.rank.room.comment.listener.CommentItemListener;
import com.module.playways.rank.room.comment.model.CommentModel;
import com.module.rank.R;

public class CommentAdapter extends DiffAdapter<CommentModel, RecyclerView.ViewHolder> {

    public static final int VIEW_HOLDER_TYPE_NORMAL = 1;  // 普通消息(即头像加上文字的普通消息)
    public int mGameType = 0;

    CommentItemListener mCommentItemListener;

    public CommentAdapter(CommentItemListener mCommentItemListener) {
        this.mCommentItemListener = mCommentItemListener;
    }

    public void setGameType(int gameType) {
        mGameType = gameType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (mGameType == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_view_holder_text_item, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grab_comment_view_holder_item, parent, false);
        }
        CommentHolder commentHolder = new CommentHolder(view);
        return commentHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CommentModel model = mDataList.get(position);
        if (holder instanceof CommentHolder) {
            CommentHolder commentHolder = (CommentHolder) holder;
            commentHolder.bind(position, model);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_HOLDER_TYPE_NORMAL;
    }
}
