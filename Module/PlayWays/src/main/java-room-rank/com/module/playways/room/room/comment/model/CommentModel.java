package com.module.playways.room.room.comment.model;

import android.graphics.Color;
import android.text.SpannableStringBuilder;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.module.playways.R;

/**
 * 消息的基类
 */
public abstract class CommentModel {
    public static final int TYPE_SYSTEM = 1;     // 系统消息
    public static final int TYPE_AI = 2;         // AI裁判消息
    public static final int TYPE_TEXT = 101;     // 普通文本聊天消息
    public static final int TYPE_LIGHT = 102;    // 爆灭灯消息
    public static final int TYPE_DYNAMIC = 103;  // 特殊表情消息
    public static final int TYPE_GIFT = 104;     // 礼物消息
    public static final int TYPE_AUDIO = 105;    // 语音消息

    public static final int AVATAR_COLOR = Color.WHITE;     // 头像圈的颜色

    public static final int RANK_NAME_COLOR = Color.parseColor("#FFC15B");    // 昵称颜色（排位）
    public static final int RANK_TEXT_COLOR = U.getColor(R.color.white_trans_50);          // 文本颜色 （排位）
    public static final int RANK_SYSTEM_COLOR = Color.parseColor("#FF8AB6");    // 系统文案颜色（排位）
    public static final int RANK_SYSTEM_HIGH_COLOR =  Color.parseColor("#FF8AB6"); // 系统文案的高亮颜色（排位)

    public static final int GRAB_NAME_COLOR = Color.parseColor("#FFC15B");   // 昵称颜色（抢唱）
    public static final int GRAB_TEXT_COLOR = U.getColor(R.color.white_trans_50);       // 文本颜色 （抢唱）
    public static final int GRAB_SYSTEM_COLOR = Color.parseColor("#FF8AB6");  // 系统文案颜色（抢唱）
    public static final int GRAB_SYSTEM_HIGH_COLOR = Color.parseColor("#FF8AB6");  // 系统文案的高亮颜色（抢唱)

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

    public UserInfoModel toUserInfoModel(){
        UserInfoModel model = new UserInfoModel();
        model.setUserId(userId);
        model.setNickname(userName);
        model.setAvatar(avatar);
        return model;
    }
}
