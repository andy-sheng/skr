package com.module.playways.room.room.comment.model

import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.SpanUtils
import com.module.playways.BaseRoomData
import com.module.playways.grab.room.dynamicmsg.DynamicModel
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.model.FakeUserInfoModel
import com.module.playways.room.msg.event.DynamicEmojiMsgEvent

class CommentDynamicModel : CommentModel() {

    var dynamicModel: DynamicModel? = null

    init {
        commentType = TYPE_DYNAMIC
    }

    companion object {

        // 动态表情消息
        fun parseFromEvent(event: DynamicEmojiMsgEvent, roomData: BaseRoomData<*>?): CommentDynamicModel {
            val commentModel = CommentDynamicModel()
            if (roomData != null) {
                val sender = roomData.getPlayerOrWaiterInfo(event.info.sender.userID)
                commentModel.avatarColor = AVATAR_COLOR
                if (sender != null) {
                    commentModel.userInfo = sender
                } else {
                    commentModel.userInfo = UserInfoModel.parseFromPB(event.info.sender)
                }
            }

            if (roomData != null && roomData is RaceRoomData) {
                commentModel.fakeUserInfo = roomData.getFakeInfo(commentModel.userInfo?.userId)
                if (commentModel.fakeUserInfo == null) {
                    // 观众，那我们构造一个fakeUserInfo
                    val fakeUserInfoModel = FakeUserInfoModel().apply {
                        nickName = "【观众】${commentModel.userInfo?.nicknameRemark}"
                    }
                    commentModel.fakeUserInfo = fakeUserInfoModel
                }
                commentModel.isFake = roomData.isFakeForMe(commentModel.userInfo?.userId)
            }

            commentModel.dynamicModel = event.mDynamicModel
            return commentModel
        }
    }

}
