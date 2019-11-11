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

    private UserInfoModel userInfo;                //消息发送者信息(头像，昵称，vip和id)
    private boolean isFake = false;                //消息发送者是否蒙面(默认false)
    private int avatarColor;                       //消息发送者头像颜色
    private SpannableStringBuilder mNameBuilder;   //昵称的内容
    private SpannableStringBuilder mStringBuilder; //消息的内容

    public SpannableStringBuilder getStringBuilder() {
        return mStringBuilder;
    }

    public void setStringBuilder(SpannableStringBuilder stringBuilder) {
        mStringBuilder = stringBuilder;
    }

    public SpannableStringBuilder getNameBuilder() {
        return mNameBuilder;
    }

    public void setNameBuilder(SpannableStringBuilder nameBuilder) {
        mNameBuilder = nameBuilder;
    }

    public int getCommentType() {
        return commentType;
    }

    public void setCommentType(int commentType) {
        this.commentType = commentType;
    }

    public UserInfoModel getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfoModel userInfo) {
        this.userInfo = userInfo;
    }

    public int getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(int avatarColor) {
        this.avatarColor = avatarColor;
    }

    public boolean isFake() {
        return isFake;
    }

    public void setFake(boolean fake) {
        isFake = fake;
    }
}
