package com.module.playways.grab.room.model

import com.module.playways.grab.room.event.GrabChorusUserStatusChangeEvent
import com.zq.live.proto.GrabRoom.QCHOInnerRoundInfo
import com.zq.live.proto.MicRoom.MCHOInnerRoundInfo

import org.greenrobot.eventbus.EventBus

import java.io.Serializable

class ChorusRoundInfoModel : Serializable {
    var userID: Int = 0
    var isHasGiveUp: Boolean = false// 不唱了
    var isHasExit: Boolean = false// 离线了

    fun tryUpdateRoundInfoModel(chorusRoundInfoModel2: ChorusRoundInfoModel) {
        if (chorusRoundInfoModel2.userID == userID) {
            var sendEvent = false
            if (!isHasGiveUp && chorusRoundInfoModel2.isHasGiveUp) {
                isHasGiveUp = true
                sendEvent = true
            }
            if (isHasExit != chorusRoundInfoModel2.isHasExit) {
                isHasExit = chorusRoundInfoModel2.isHasExit
                sendEvent = true
            }
            if (sendEvent) {
                EventBus.getDefault().post(GrabChorusUserStatusChangeEvent(this))
            }
        }
    }

    fun userExit() {
        if (!isHasExit) {
            isHasExit = true
            EventBus.getDefault().post(GrabChorusUserStatusChangeEvent(this))
        }
    }

    override fun toString(): String {
        return "ChorusRoundInfoModel{" +
                "userID=" + userID +
                ", hasGiveUp=" + isHasGiveUp +
                ", hasExit=" + isHasExit +
                '}'.toString()
    }

    companion object {

        fun parse(qchoInnerRoundInfo: QCHOInnerRoundInfo): ChorusRoundInfoModel {
            val chorusRoundInfoModel = ChorusRoundInfoModel()
            chorusRoundInfoModel.userID = qchoInnerRoundInfo.userID
            chorusRoundInfoModel.isHasGiveUp = qchoInnerRoundInfo.hasGiveUp
            chorusRoundInfoModel.isHasExit = qchoInnerRoundInfo.hasExit
            return chorusRoundInfoModel
        }

        fun parse(qchoInnerRoundInfo: MCHOInnerRoundInfo):ChorusRoundInfoModel {
            val chorusRoundInfoModel = ChorusRoundInfoModel()
            chorusRoundInfoModel.userID = qchoInnerRoundInfo.userID!!
            chorusRoundInfoModel.isHasGiveUp = qchoInnerRoundInfo.hasGiveUp!!
            chorusRoundInfoModel.isHasExit = qchoInnerRoundInfo.hasExit!!
            return chorusRoundInfoModel
        }
    }


}
