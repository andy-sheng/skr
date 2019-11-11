package com.module.playways.room.room.comment.model

import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.SpanUtils
import com.module.playways.BaseRoomData
import com.module.playways.race.room.RaceRoomData
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
            commentModel.avatarColor = AVATAR_COLOR
            if (roomData != null) {
                val sender = roomData.getPlayerOrWaiterInfo(event.mInfo.sender.userID!!)
                if (sender != null) {
                    commentModel.userInfo = sender
                } else {
                    commentModel.userInfo = UserInfoModel.parseFromPB(event.mInfo.sender)
                }
            } else {
                commentModel.userInfo = UserInfoModel.parseFromPB(event.mInfo.sender)
            }

            if (roomData != null && roomData is RaceRoomData && roomData.isFakeForMe(commentModel.userInfo.userId)) {
                val playInfoModel = roomData.getPlayerOrWaiterInfoModel(commentModel.userInfo.userId)
                commentModel.userInfo = playInfoModel?.toFakeUserInfo()
                commentModel.isFake = true
                val nameBuilder = SpanUtils()
                        .append(commentModel.userInfo.nickname + " ").setForegroundColor(GRAB_NAME_COLOR)
                        .create()
                commentModel.nameBuilder = nameBuilder
            } else {
                commentModel.isFake = false
                val nameBuilder = SpanUtils()
                        .append(commentModel.userInfo.nicknameRemark + " ").setForegroundColor(GRAB_NAME_COLOR)
                        .create()
                commentModel.nameBuilder = nameBuilder
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
