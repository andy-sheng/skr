package com.module.playways.rank.room.comment;

import android.graphics.Color;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.module.playways.rank.msg.event.CommentMsgEvent;
import com.module.playways.RoomData;
import com.module.rank.R;
import com.zq.live.proto.Common.ESex;

public class CommentModel {
    public static final int TYPE_TEXT = 1;

    private int commentType = 0;
    private int userId;
    private String avatar;
    private String userName;
    private String content;
    private int avatarColor;
    private int nameColor = Color.parseColor("#ccFFAD00");   // 昵称颜色
    private int textColor = U.getColor(R.color.white_trans_80);  // 文本内容颜色

    public static CommentModel parseFromEvent(CommentMsgEvent event, RoomData roomData) {
        CommentModel commentModel = new CommentModel();
        commentModel.setUserId(event.info.getSender().getUserID());
        commentModel.setUserName(event.info.getSender().getNickName());
        if (roomData != null) {
            UserInfoModel sender = roomData.getUserInfo(event.info.getSender().getUserID());
            if (sender != null) {
                commentModel.setAvatar(sender.getAvatar());
                if (sender.getSex() == ESex.SX_MALE.getValue()) {
                    commentModel.setAvatarColor(Color.parseColor("#33A4E1"));
                } else if (sender.getSex() == ESex.SX_FEMALE.getValue()) {
                    commentModel.setAvatarColor(Color.parseColor("#FF75A2"));
                } else {
                    commentModel.setAvatarColor(Color.WHITE);
                }
            } else {
                commentModel.setAvatar(event.info.getSender().getAvatar());
                commentModel.setAvatarColor(Color.WHITE);
            }
        }
        if (commentModel.getUserId() == RoomData.SYSTEM_ID) {
            // 系统消息
            commentModel.setTextColor(Color.parseColor("#EF5E85"));
        }
        commentModel.setContent(event.text);
        commentModel.setCommentType(CommentModel.TYPE_TEXT);
        return commentModel;
    }

    public int getCommentType() {
        return commentType;
    }

    public void setCommentType(int commentType) {
        this.commentType = commentType;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public int getNameColor() {
        return nameColor;
    }

    public void setNameColor(int nameColor) {
        this.nameColor = nameColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(int avatarColor) {
        this.avatarColor = avatarColor;
    }
}
