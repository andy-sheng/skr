package com.module.playways.room.room.comment.model

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
        commentType = TYPE_TEXT
    }

    companion object {

        // 处理真的消息，即聊天消息
        fun parseFromEvent(event: CommentMsgEvent, roomData: BaseRoomData<*>?): CommentTextModel {
            val commentModel = CommentTextModel()

            if (roomData != null) {
                val sender = roomData.getUserInfo(event.info.sender.userID!!)
                commentModel.avatarColor = AVATAR_COLOR
                if (sender != null) {
                    commentModel.userInfo = sender
                } else {
                    commentModel.userInfo = UserInfoModel.parseFromPB(event.info.sender)
                }
            }
            if (roomData != null && (roomData.gameType == GameModeType.GAME_MODE_GRAB || roomData.gameType == GameModeType.GAME_MODE_RACE)) {
                if (event.mUserInfoModelList == null || event.mUserInfoModelList.size == 0) {
                    val ssb = SpanUtils()
                            .append(commentModel.userInfo.nicknameRemark + " ").setForegroundColor(GRAB_NAME_COLOR)
                            .append(event.text).setForegroundColor(GRAB_TEXT_COLOR)
                            .create()
                    commentModel.stringBuilder = ssb
                } else {
                    val ssb = SpanUtils()
                            .append(commentModel.userInfo.nicknameRemark + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                            .append("@ ").setForegroundColor(GRAB_TEXT_COLOR)
                            .append(event.mUserInfoModelList[0].nicknameRemark + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                            .append(event.text).setForegroundColor(GRAB_TEXT_COLOR)
                            .create()
                    commentModel.stringBuilder = ssb
                }
            } else {
                val ssb = SpanUtils()
                        .append(commentModel.userInfo.nicknameRemark + " ").setForegroundColor(CommentModel.RANK_NAME_COLOR)
                        .append(event.text).setForegroundColor(RANK_TEXT_COLOR)
                        .create()
                commentModel.stringBuilder = ssb
            }
            return commentModel
        }
    }
}
