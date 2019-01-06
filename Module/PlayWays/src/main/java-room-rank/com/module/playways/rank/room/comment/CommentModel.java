package com.module.playways.rank.room.comment;

import android.graphics.Color;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.module.playways.rank.msg.event.CommentMsgEvent;
import com.module.playways.rank.room.model.RoomData;
import com.module.rank.R;

public class CommentModel {
    public static final int TYPE_TEXT = 1;

    private int commentType = 0;
    private int userId;
    private String avatar;
    private String text;
    private int textColor = U.getColor(R.color.white_trans_80);

    public static CommentModel parseFromEvent(CommentMsgEvent event, RoomData roomData) {
        CommentModel commentModel = new CommentModel();
        commentModel.setUserId(event.info.getSender().getUserID());
        if (roomData != null) {
            UserInfoModel sender = roomData.getUserInfo(event.info.getSender().getUserID());
            if (sender != null) {
                commentModel.setAvatar(sender.getAvatar());
                if (sender.getUserId() == RoomData.SYSTEM_ID) {
                    // 系统消息
                    commentModel.setTextColor(Color.parseColor("#EF5E85"));
                }
            }
        }
        commentModel.setText(event.text);
        commentModel.setCommentType(CommentModel.TYPE_TEXT);
        return commentModel;
    }

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
