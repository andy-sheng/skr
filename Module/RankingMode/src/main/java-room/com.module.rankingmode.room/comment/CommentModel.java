package com.module.rankingmode.room.comment;

public class CommentModel {
    public static final int TYPE_TEXT = 1;

    private int commentType = 0;
    private String text;

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
}
