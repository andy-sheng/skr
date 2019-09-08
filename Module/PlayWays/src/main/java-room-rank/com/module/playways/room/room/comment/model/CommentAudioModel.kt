package com.module.playways.room.room.comment.model

import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.SpanUtils
import com.module.playways.BaseRoomData
import com.module.playways.room.msg.event.AudioMsgEvent

class CommentAudioModel : CommentModel() {

    var localPath: String = ""
    var duration: Long = 0
    var msgUrl: String = ""
    var isRead = false

    // 语音消息，需要标记是否已读
    init {
        commentType = TYPE_AUDIO
    }

    companion object {
        fun parseFromEvent(event: AudioMsgEvent, roomData: BaseRoomData<*>?): CommentAudioModel {
            val commentModel = CommentAudioModel()
            if (roomData != null) {
                val sender = roomData.getUserInfo(event.mInfo.sender.userID!!)

                commentModel.avatarColor = AVATAR_COLOR
                if (sender != null) {
                    commentModel.userInfo = sender
                } else {
                    commentModel.userInfo = UserInfoModel.parseFromPB(event.mInfo.sender)
                }
            }

            if (roomData != null) {
                val ssb = SpanUtils()
                        .append(commentModel.userInfo.nicknameRemark + " ").setForegroundColor(GRAB_NAME_COLOR)
                        .create()
                commentModel.stringBuilder = ssb
            }
            commentModel.localPath = event.localPath
            commentModel.duration = event.duration
            commentModel.msgUrl = event.msgUrl
            if (event.type == AudioMsgEvent.MSG_TYPE_SEND) {
                commentModel.isRead = true
            }
            return commentModel
        }
    }
}
