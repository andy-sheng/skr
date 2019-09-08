package com.module.playways.room.room.comment.model;

import android.text.SpannableStringBuilder;

import com.common.utils.SpanUtils;
import com.module.playways.room.prepare.model.PlayerInfoModel;

/**
 * Ai机器人消息
 */
public class CommentAiModel extends CommentModel {

    public CommentAiModel(PlayerInfoModel ai, String text) {
        setCommentType(CommentModel.TYPE_AI);
        setUserInfo(ai.getUserInfo());
        setAvatarColor(CommentModel.AVATAR_COLOR);
        SpannableStringBuilder stringBuilder = new SpanUtils()
                .append("AI裁判 ").setForegroundColor(CommentModel.RANK_NAME_COLOR)
                .append(text).setForegroundColor(CommentModel.RANK_TEXT_COLOR)
                .create();
        setStringBuilder(stringBuilder);
    }
}
