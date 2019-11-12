package com.module.playways.room.room.comment.model

import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.SpanUtils
import com.component.busilib.constans.GameModeType
import com.module.playways.BaseRoomData
import com.module.playways.race.room.RaceRoomData
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
                val sender = roomData.getPlayerOrWaiterInfo(event.info.sender.userID!!)
                commentModel.avatarColor = AVATAR_COLOR
                if (sender != null) {
                    commentModel.userInfo = sender
                } else {
                    commentModel.userInfo = UserInfoModel.parseFromPB(event.info.sender)
                }

                if (roomData is RaceRoomData) {
                    commentModel.fakeUserInfo = roomData.getFakeInfo(commentModel.userInfo?.userId)
                    commentModel.isFake = roomData.isFakeForMe(commentModel.userInfo?.userId)
                }

                if (event.mUserInfoModelList == null || event.mUserInfoModelList.size == 0) {
                    // 普通消息
                    val nameSsb = SpanUtils()
                            .append(commentModel.fakeUserInfo?.nickName
                                    ?: commentModel.userInfo?.nicknameRemark
                                    + " ").setForegroundColor(GRAB_NAME_COLOR)
                            .create()
                    commentModel.nameBuilder = nameSsb

                    val ssb = SpanUtils()
                            .append(event.text).setForegroundColor(GRAB_TEXT_COLOR)
                            .create()
                    commentModel.stringBuilder = ssb

                } else {
                    // @消息
                    val nameSsb = SpanUtils()
                            .append(commentModel.userInfo?.nicknameRemark + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                            .create()
                    commentModel.nameBuilder = nameSsb

                    val ssb = SpanUtils()
                            .append("@ ").setForegroundColor(GRAB_TEXT_COLOR)
                            .append(event.mUserInfoModelList[0].nicknameRemark + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                            .append(event.text).setForegroundColor(GRAB_TEXT_COLOR)
                            .create()
                    commentModel.stringBuilder = ssb
                }
            }
            return commentModel
        }
    }
}
