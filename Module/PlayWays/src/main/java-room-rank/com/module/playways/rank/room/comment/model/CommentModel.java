package com.module.playways.rank.room.comment.model;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.module.playways.rank.msg.event.CommentMsgEvent;
import com.module.playways.BaseRoomData;
import com.module.playways.rank.room.comment.holder.CommentHolder;
import com.module.rank.R;
import com.zq.live.proto.Common.ESex;

/**
 * 消息的基类
 */
public abstract class CommentModel {
    public static final int TYPE_SYSTEM = 1;     // 系统消息
    public static final int TYPE_AI = 2;         // AI裁判消息
    public static final int TYPE_TEXT = 101;     // 普通文本聊天消息
    public static final int TYPE_LIGHT = 102;    // 爆灭灯消息
    public static final int TYPE_DYNAMIC = 103;  // 特殊表情消息

    public static final int TEXT_WHITE = U.getColor(R.color.white_trans_80);
    public static final int TEXT_YELLOW = Color.parseColor("#ccFFB100");
    public static final int TEXT_RED = Color.parseColor("#EF5E85");
    public static final int TEXT_GRAY = Color.GRAY;
    public static final int TEXT_3B4E79 = Color.parseColor("#3B4E79");

    private int commentType = 0;                   //消息类型
    private int userId;                            //消息发送者
    private String avatar;                         //消息发送者头像
    private String userName;                       //消息发送者昵称
    private int avatarColor;                       //消息发送者头像颜色
    private SpannableStringBuilder mStringBuilder; //消息的内容

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
