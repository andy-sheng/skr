package com.module.playways.doubleplay.inter

import com.module.playways.doubleplay.DoubleRoomData
import com.common.notification.event.DoubleStartCombineRoomByMatchPushEvent

interface IMatchView {
    fun matchSuccessFromHttp(doubleRoomData: DoubleRoomData);

    fun toDoubleRoomByPush();

    fun matchSuccessFromPush(doubleStartCombineRoomByMatchPushEvent: DoubleStartCombineRoomByMatchPushEvent)

    fun finishActivity()
}