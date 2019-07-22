package com.module.playways.doubleplay.inter

import com.module.playways.doubleplay.DoubleRoomData
import com.common.notification.event.CRStartByMatchPushEvent
import com.module.playways.doubleplay.pbLocalModel.LocalEnterRoomModel

interface IMatchView {
    fun playBgMusic(musicUrl : String)

    fun matchSuccessFromHttp(doubleRoomData: DoubleRoomData)

    fun toDoubleRoomByPush()

    fun matchSuccessFromPush(localEnterRoomModel: LocalEnterRoomModel)

    fun finishActivity()
}