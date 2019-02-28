package com.module.playways.rank.room.event;

import com.module.playways.rank.room.comment.CommentModel;

public class PretendCommentMsgEvent {
    public CommentModel mCommentModel;

    public PretendCommentMsgEvent(CommentModel mCommentModel) {
        this.mCommentModel = mCommentModel;
    }
}
