package com.module.playways.rank.room.comment.model;

import android.graphics.Color;
import android.text.SpannableStringBuilder;

import com.common.utils.SpanUtils;
import com.module.playways.rank.prepare.model.PlayerInfoModel;

/**
 * Ai机器人消息
 */
public class CommentAiModel extends CommentModel {

    public CommentAiModel(PlayerInfoModel ai, String text) {
        setCommentType(CommentModel.TYPE_AI);
        setUserId(ai.getUserInfo().getUserId());
        setUserName(ai.getUserInfo().getNickname());
        setAvatar(ai.getUserInfo().getAvatar());
        setAvatarColor(Color.WHITE);
        SpannableStringBuilder stringBuilder = new SpanUtils()
                .append("AI裁判 ").setForegroundColor(TEXT_YELLOW)
                .append(text).setForegroundColor(TEXT_WHITE)
                .create();
        setStringBuilder(stringBuilder);
    }
}
