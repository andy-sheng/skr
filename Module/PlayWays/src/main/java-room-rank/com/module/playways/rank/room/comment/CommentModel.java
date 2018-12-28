package com.module.playways.rank.room.comment;

import com.common.utils.U;
import com.module.rank.R;

public class CommentModel {
    public static final int TYPE_TEXT = 1;

    private int commentType = 0;
    private int userId;
    private String avatar;
    private String text;
    private int textColor = U.getColor(R.color.white_trans_80);

    public int getCommentType() {
        return commentType;
    }

    public void setCommentType(int commentType) {
        this.commentType = commentType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
}
