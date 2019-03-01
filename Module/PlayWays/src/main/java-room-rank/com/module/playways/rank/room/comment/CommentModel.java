package com.module.playways.rank.room.comment;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.module.playways.rank.msg.event.CommentMsgEvent;
import com.module.playways.BaseRoomData;
import com.module.rank.R;
import com.zq.live.proto.Common.ESex;

public class CommentModel {
    public static final int TYPE_TRICK = 0;      // 假消息
    public static final int TYPE_TEXT = 101;     // 普通文本消息

    public static final int TEXT_WHITE = U.getColor(R.color.white_trans_80);
    public static final int TEXT_YELLOW = Color.parseColor("#ccFFB100");
    public static final int TEXT_RED = Color.parseColor("#EF5E85");
    public static final int TEXT_GRAY = Color.GRAY;

    private int commentType = 0;
    private int userId;
    private String avatar;     // 头像
    private String userName;   // 昵称
    private int avatarColor;   // 头像颜色
    private SpannableStringBuilder mStringBuilder; //需要设置给text的内容

    // 处理真的消息，即聊天消息
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
                    commentModel.setAvatarColor(U.getColor(R.color.color_man_stroke_color));
                } else if (sender.getSex() == ESex.SX_FEMALE.getValue()) {
                    commentModel.setAvatarColor(U.getColor(R.color.color_woman_stroke_color));
                } else {
                    commentModel.setAvatarColor(Color.WHITE);
                }
            } else {
                commentModel.setAvatar(event.info.getSender().getAvatar());
                commentModel.setAvatarColor(Color.WHITE);
            }
        }

        SpannableStringBuilder ssb = new SpanUtils()
                .append(commentModel.getUserName() + " ").setForegroundColor(TEXT_YELLOW)
                .append(event.text).setForegroundColor(TEXT_WHITE)
                .create();
        commentModel.setStringBuilder(ssb);
        commentModel.setCommentType(TYPE_TEXT);
        return commentModel;
    }

    public SpannableStringBuilder getStringBuilder() {
        return mStringBuilder;
    }

    public void setStringBuilder(SpannableStringBuilder stringBuilder) {
        mStringBuilder = stringBuilder;
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

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(int avatarColor) {
        this.avatarColor = avatarColor;
    }
}
