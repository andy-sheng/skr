package com.module.playways.room.room.comment.model

import android.text.TextUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.SpanUtils
import com.module.playways.BaseRoomData
import com.module.playways.race.room.RaceRoomData
import com.module.playways.room.room.gift.model.GiftPlayModel

class CommentGiftModel(giftPlayModel: GiftPlayModel, roomData: BaseRoomData<*>) : CommentModel() {

    init {
        commentType = TYPE_GIFT
        userInfo = giftPlayModel.sender
        avatarColor = AVATAR_COLOR

        if (roomData is RaceRoomData) {
            fakeUserInfo = roomData.getFakeInfo(giftPlayModel.sender.userId)
            isFake = roomData.isFakeForMe(giftPlayModel.sender.userId)
            var nameBuilder = SpanUtils()
                    .append((if (!TextUtils.isEmpty(fakeUserInfo?.nickName))
                        fakeUserInfo?.nickName
                    else
                        giftPlayModel.sender.nicknameRemark) + " ").setForegroundColor(GRAB_NAME_COLOR)
                    .create()
            this.nameBuilder = nameBuilder

            if (giftPlayModel.receiver.userId.toLong() == MyUserInfoManager.uid) {
                var stringBuilder = SpanUtils()
                        .append("对 你 送出了").setForegroundColor(GRAB_TEXT_COLOR)
                        .append(giftPlayModel.gift.giftName).setForegroundColor(GRAB_TEXT_COLOR)
                        .create()
                this.stringBuilder = stringBuilder
            } else {
                var stringBuilder = SpanUtils()
                        .append("对").setForegroundColor(GRAB_TEXT_COLOR)
                        .append(" " + (if (!TextUtils.isEmpty(roomData.getFakeInfo(giftPlayModel.receiver.userId)?.nickName))
                            roomData.getFakeInfo(giftPlayModel.receiver.userId)?.nickName
                        else
                            giftPlayModel.receiver.nicknameRemark) + " ").setForegroundColor(GRAB_NAME_COLOR)
                        .append("送出了").setForegroundColor(GRAB_TEXT_COLOR)
                        .append(giftPlayModel.gift.giftName).setForegroundColor(GRAB_TEXT_COLOR)
                        .create()
                this.stringBuilder = stringBuilder
            }
        } else {
            var nameBuilder = SpanUtils()
                    .append(giftPlayModel.sender.nicknameRemark + " ").setForegroundColor(GRAB_NAME_COLOR)
                    .create()
            this.nameBuilder = nameBuilder

            if (giftPlayModel.receiver.userId.toLong() == MyUserInfoManager.uid) {
                var stringBuilder = SpanUtils()
                        .append("对 你 送出了").setForegroundColor(GRAB_TEXT_COLOR)
                        .append(giftPlayModel.gift.giftName).setForegroundColor(GRAB_TEXT_COLOR)
                        .create()
                this.stringBuilder = stringBuilder
            } else {
                var stringBuilder = SpanUtils()
                        .append("对").setForegroundColor(GRAB_TEXT_COLOR)
                        .append(" " + giftPlayModel.receiver.nicknameRemark + " ").setForegroundColor(GRAB_NAME_COLOR)
                        .append("送出了").setForegroundColor(GRAB_TEXT_COLOR)
                        .append(giftPlayModel.gift.giftName).setForegroundColor(GRAB_TEXT_COLOR)
                        .create()
                this.stringBuilder = stringBuilder
            }
        }

    }
}
