package com.module.playways.rank.room.comment;

import android.graphics.Color;
import android.text.TextUtils;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.module.playways.rank.msg.event.CommentMsgEvent;
import com.module.playways.BaseRoomData;
import com.module.rank.R;
import com.zq.live.proto.Common.ESex;

public class CommentModel {
    public static final int TYPE_TEXT = 101;     // 普通文本消息
    public static final int TYPE_RANK_MIE = 102; // rank灭灯消息

    private int commentType = 0;
    private int userId;
    private String avatar;
    private String userName;
    private String content;    // 显示内容
    private String highlightContent;  // 高亮显示内容
    private int avatarColor;
    private int nameColor = Color.parseColor("#ccFFAD00");   // 昵称颜色
    private int textColor = U.getColor(R.color.white_trans_80);  // 文本内容颜色

    public static CommentModel parseFromEvent(CommentMsgEvent event, BaseRoomData roomData) {
        CommentModel commentModel = new CommentModel();
        commentModel.setUserId(event.info.getSender().getUserID());
        if (!TextUtils.isEmpty(event.info.getSender().getNickName())) {
            commentModel.setUserName(event.info.getSender().getNickName());
        } else {
            UserInfoModel userInfoModel = roomData.getUserInfo(event.info.getSender().getUserID());
            commentModel.setUserName(userInfoModel.getNickname());
        }

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
        if (commentModel.getUserId() == BaseRoomData.SYSTEM_ID) {
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

    public String getHighlightContent() {
        return highlightContent;
    }

    public void setHighlightContent(String highlightContent) {
        this.highlightContent = highlightContent;
    }
}
