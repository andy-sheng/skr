package com.module.playways.room.room.comment.model

import com.common.core.account.UserAccountManager
import com.common.utils.SpanUtils

// 房间内容或房间公告
class CommentNoticeModel : CommentModel {

    constructor(title: String, content: String) {
        commentType = TYPE_NOTICE
        userInfo = UserAccountManager.systemModel
        userInfo?.avatar = UserAccountManager.NOTICE_AVATAR
        avatarColor = AVATAR_COLOR

        stringBuilder = SpanUtils()
                .append("【$title】\n $content").setForegroundColor(GAME_NOTICE_COLOR)
                .create()
    }
}
