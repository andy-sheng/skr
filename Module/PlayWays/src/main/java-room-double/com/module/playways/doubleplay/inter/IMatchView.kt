package com.module.playways.doubleplay.inter

import com.module.playways.doubleplay.DoubleRoomData
import com.common.notification.event.CRStartByMatchPushEvent

interface IMatchView {
    fun playBgMusic(musicUrl : String)

    fun matchSuccessFromHttp(doubleRoomData: DoubleRoomData)

    fun toDoubleRoomByPush()

    fun matchSuccessFromPush(doubleStartCombineRoomByMatchPushEvent: CRStartByMatchPushEvent)

    fun finishActivity()
}