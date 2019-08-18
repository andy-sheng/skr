package com.module.playways.room.room.comment.model

import android.text.SpannableStringBuilder

import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.SpanUtils
import com.component.busilib.constans.GameModeType
import com.module.playways.BaseRoomData
import com.module.playways.room.msg.event.CommentMsgEvent

/**
 * 普通文本消息
 */
class CommentTextModel : CommentModel() {
    init {
        commentType = CommentModel.TYPE_TEXT
    }

    companion object {

        // 处理真的消息，即聊天消息
        fun parseFromEvent(event: CommentMsgEvent, roomData: BaseRoomData<*>?): CommentTextModel {
            val commentModel = CommentTextModel()
            commentModel.userId = event.info.sender.userID!!

            if (roomData != null) {
                val sender = roomData.getUserInfo(event.info.sender.userID!!)
                commentModel.userName = sender?.nicknameRemark?:""
                commentModel.avatarColor = CommentModel.AVATAR_COLOR
                if (sender != null) {
                    commentModel.avatar = sender.avatar
                    commentModel.userName = sender.nicknameRemark
                } else {
                    commentModel.avatar = event.info.sender.avatar
                    commentModel.userName = event.info.sender.nickName
                }
            }

            if (roomData != null && roomData.gameType == GameModeType.GAME_MODE_GRAB) {
                if (event.mUserInfoModelList == null || event.mUserInfoModelList.size == 0) {
                    val ssb = SpanUtils()
                            .append(commentModel.userName + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                            .append(event.text).setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                            .create()
                    commentModel.stringBuilder = ssb
                } else {
                    val ssb = SpanUtils()
                            .append(commentModel.userName + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                            .append("@ ").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                            .append(event.mUserInfoModelList[0].nicknameRemark + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                            .append(event.text).setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                            .create()
                    commentModel.stringBuilder = ssb
                }
            } else {
                val ssb = SpanUtils()
                        .append(commentModel.userName + " ").setForegroundColor(CommentModel.RANK_NAME_COLOR)
                        .append(event.text).setForegroundColor(CommentModel.RANK_TEXT_COLOR)
                        .create()
                commentModel.stringBuilder = ssb
            }
            return commentModel
        }
    }
}
