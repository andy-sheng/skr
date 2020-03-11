package com.common.notification.event

import android.text.Spannable
import com.common.core.userinfo.cache.BuddyCache
import com.zq.live.proto.Notification.RelayRoomEnterMsg

class RongMsgNotifyEvent(var content: Spannable?,var  buddyCacheEntry: BuddyCache.BuddyCacheEntry?) {
}