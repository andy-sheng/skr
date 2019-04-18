package com.module.playways.room.room.event;

import com.module.playways.room.room.comment.model.CommentModel;

public class PretendCommentMsgEvent {
    public CommentModel mCommentModel;

    public PretendCommentMsgEvent(CommentModel mCommentModel) {
        this.mCommentModel = mCommentModel;
    }
}
