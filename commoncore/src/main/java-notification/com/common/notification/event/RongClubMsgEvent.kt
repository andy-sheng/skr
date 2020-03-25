package com.common.notification.event

import android.text.Spannable
import com.common.core.userinfo.cache.BuddyCache
import com.common.core.userinfo.model.UserInfoModel
import com.zq.live.proto.Notification.RelayRoomEnterMsg

/**
 * 家族群聊消息分发事件
 */
class RongClubMsgEvent(var content: Spannable?, var userInfoModel: UserInfoModel, var conversationType: String) {
}