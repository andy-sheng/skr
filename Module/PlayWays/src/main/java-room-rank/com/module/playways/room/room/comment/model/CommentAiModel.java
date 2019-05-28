package com.module.playways.room.room.comment.model;

import android.graphics.Color;
import android.text.SpannableStringBuilder;

import com.common.utils.SpanUtils;
import com.module.playways.room.prepare.model.PlayerInfoModel;

/**
 * Ai机器人消息
 */
public class CommentAiModel extends CommentModel {

    public CommentAiModel(PlayerInfoModel ai, String text) {
        setCommentType(CommentModel.TYPE_AI);
        setUserId(ai.getUserInfo().getUserId());
        setUserName(ai.getUserInfo().getNicknameRemark());
        setAvatar(ai.getUserInfo().getAvatar());
        setAvatarColor(Color.WHITE);
        SpannableStringBuilder stringBuilder = new SpanUtils()
                .append("AI裁判 ").setForegroundColor(TEXT_YELLOW)
                .append(text).setForegroundColor(TEXT_WHITE)
                .create();
        setStringBuilder(stringBuilder);
    }
}
