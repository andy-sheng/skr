package com.module.playways.rank.room.comment.model;

import android.graphics.Color;
import android.text.SpannableStringBuilder;

import com.common.core.account.UserAccountManager;
import com.common.utils.SpanUtils;
import com.component.busilib.constans.GameModeType;

/**
 * 系统消息
 */
public class CommentSysModel extends CommentModel {

    // 普通系统消息
    public CommentSysModel(int gameType, String text) {
        setCommentType(CommentModel.TYPE_SYSTEM);
        setUserId(UserAccountManager.SYSTEM_ID);
        setAvatar(UserAccountManager.SYSTEM_AVATAR);
        setUserName("系统消息");
        setAvatarColor(Color.WHITE);

        if (gameType == GameModeType.GAME_MODE_GRAB) {
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(text).setForegroundColor(Color.parseColor("#EA596B"))
                    .create();
            setStringBuilder(stringBuilder);
        } else {
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(text).setForegroundColor(CommentModel.TEXT_RED)
                    .create();
            setStringBuilder(stringBuilder);
        }
    }

    // 离开系统消息
    public CommentSysModel(String nickName, String leaveText) {
        setCommentType(CommentModel.TYPE_SYSTEM);
        setUserId(UserAccountManager.SYSTEM_ID);
        setAvatar(UserAccountManager.SYSTEM_AVATAR);
        setUserName("系统消息");
        setAvatarColor(Color.WHITE);

        SpannableStringBuilder stringBuilder = new SpanUtils()
                .append(nickName + " ").setForegroundColor(CommentModel.TEXT_GRAY)
                .append(leaveText).setForegroundColor(CommentModel.TEXT_GRAY)
                .create();
        setStringBuilder(stringBuilder);
    }
}
