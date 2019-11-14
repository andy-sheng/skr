package com.module.playways.room.room.comment.model

import com.common.utils.SpanUtils
import com.module.playways.room.prepare.model.PlayerInfoModel

/**
 * Ai机器人消息
 */
class CommentAiModel(ai: PlayerInfoModel, text: String) : CommentModel() {

    init {
        commentType = TYPE_AI
        userInfo = ai.userInfo
        avatarColor = AVATAR_COLOR
        var nameBuilder = SpanUtils()
                .append("AI裁判 ").setForegroundColor(RANK_NAME_COLOR)
                .create()
        this.nameBuilder = nameBuilder

        var stringBuilder = SpanUtils()
                .append(text).setForegroundColor(RANK_TEXT_COLOR)
                .create()
        this.stringBuilder = stringBuilder
    }
}
