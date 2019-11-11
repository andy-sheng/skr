package com.module.playways.room.room.comment.model

import android.text.SpannableStringBuilder

import com.common.core.account.UserAccountManager
import com.common.utils.SpanUtils
import com.component.busilib.constans.GameModeType

/**
 * 系统消息
 */
class CommentSysModel : CommentModel {

    // 普通系统消息
    constructor(gameType: Int, text: String) {
        commentType = TYPE_SYSTEM
        userInfo = UserAccountManager.systemModel
        avatarColor = AVATAR_COLOR

        if (gameType == GameModeType.GAME_MODE_GRAB) {
            stringBuilder = SpanUtils()
                    .append(text).setForegroundColor(GRAB_SYSTEM_COLOR)
                    .create()
        } else {
            stringBuilder = SpanUtils()
                    .append(text).setForegroundColor(RANK_SYSTEM_COLOR)
                    .create()
        }
    }

    // 普通系统消息
    constructor(gameType: Int, stringBuilder: SpannableStringBuilder) {
        commentType = TYPE_SYSTEM
        userInfo = UserAccountManager.systemModel
        avatarColor = AVATAR_COLOR
        this.stringBuilder = stringBuilder
    }

    // 进入房间消息 一唱到底
    constructor(roomName: String, type: Int) {
        commentType = TYPE_SYSTEM
        avatarColor = AVATAR_COLOR
        userInfo = UserAccountManager.systemModel
        var stringBuilder = SpannableStringBuilder()
        if (type == TYPE_ENTER_ROOM) {
            stringBuilder = SpanUtils()
                    .append("欢迎加入 ").setForegroundColor(CommentModel.GRAB_SYSTEM_COLOR)
                    .append(roomName + "").setForegroundColor(CommentModel.GRAB_SYSTEM_HIGH_COLOR)
                    .append("房间 撕歌倡导文明游戏，遇到恶意玩家们可以发起投票将ta踢出房间哦～").setForegroundColor(CommentModel.GRAB_SYSTEM_COLOR)
                    .create()
        } else if (type == TYPE_MIC_ENTER_ROOM) {
            stringBuilder = SpanUtils()
                    .append("欢迎来到撕歌小k房，请文明演唱、文明互动，发现违规用户记得点击头像进行举报哦～\n").setForegroundColor(CommentModel.GRAB_SYSTEM_COLOR)
                    .create()
        } else if (type == TYPE_ENTER_ROOM_PLAYBOOK) {
            stringBuilder = SpanUtils()
                    .append("欢迎加入撕歌歌单挑战赛，撕歌倡导文明游戏，若遇到恶意玩家请点击头像进行举报").setForegroundColor(CommentModel.GRAB_SYSTEM_COLOR)
                    .create()
        } else if (type == TYPE_MODIFY_ROOM_NAME) {
            stringBuilder = SpanUtils()
                    .append("房主已将房间名称修改为 ").setForegroundColor(GRAB_SYSTEM_COLOR)
                    .append(roomName + "").setForegroundColor(GRAB_SYSTEM_HIGH_COLOR)
                    .create()
        }

        this.stringBuilder = stringBuilder
    }

    // 离开系统消息
    constructor(nickName: String, leaveText: String) {
        commentType = TYPE_SYSTEM
        userInfo = UserAccountManager.systemModel
        avatarColor = AVATAR_COLOR

        var stringBuilder = SpanUtils()
                .append("$nickName ").setForegroundColor(RANK_NAME_COLOR)
                .append(leaveText).setForegroundColor(RANK_SYSTEM_COLOR)
                .create()
        this.stringBuilder = stringBuilder
    }

    companion object {
        val TYPE_ENTER_ROOM = 1
        val TYPE_MODIFY_ROOM_NAME = 2
        val TYPE_ENTER_ROOM_PLAYBOOK = 3
        val TYPE_MIC_ENTER_ROOM = 4
    }
}
