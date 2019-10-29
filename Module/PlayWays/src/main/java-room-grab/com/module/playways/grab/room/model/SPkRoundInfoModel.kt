package com.module.playways.grab.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.log.MyLog
import com.module.playways.grab.room.event.GrabSomeOneLightBurstEvent
import com.module.playways.grab.room.event.GrabSomeOneLightOffEvent
import com.module.playways.mic.room.model.MicRoundInfoModel
import com.zq.live.proto.GrabRoom.QBLightMsg
import com.zq.live.proto.GrabRoom.QMLightMsg
import com.zq.live.proto.GrabRoom.QSPKInnerRoundInfo
import com.zq.live.proto.MicRoom.MSPKInnerRoundInfo

import org.greenrobot.eventbus.EventBus

import java.io.Serializable
import java.util.HashSet

class SPkRoundInfoModel : Serializable {
    val TAG = "SPkRoundInfoModel"

    var userID: Int = 0
    var singBeginMs: Int = 0
    var singEndMs: Int = 0
    private var bLightInfos = HashSet<BLightInfoModel>()//已经爆灯的人, 一唱到底

    var mLightInfos = HashSet<MLightInfoModel>()//已经灭灯的人, 一唱到底

    var overReason: Int = 0 // 结束的原因
    var resultType: Int = 0 // 结果类型
    @JSONField(name = "SPKFinalScore")
    var score: Float = 0.toFloat()
    var isWin: Boolean = false

    var meiliTotal: Int = 0   //用来记录魅力值

    fun getbLightInfos(): HashSet<BLightInfoModel> {
        return bLightInfos
    }

    fun setbLightInfos(bLightInfos: HashSet<BLightInfoModel>) {
        this.bLightInfos = bLightInfos
    }

    fun tryUpdateRoundInfoModel(roundInfo: SPkRoundInfoModel?, notify: Boolean, grabRoundInfoModel: GrabRoundInfoModel) {
        if (roundInfo == null) {
            MyLog.d(TAG, "tryUpdateRoundInfoModel pkRoundInfoModel=$roundInfo")
            return
        }
        if (userID == 0) {
            userID = roundInfo.userID
        }
        if (roundInfo.userID == userID) {
            this.singBeginMs = roundInfo.singBeginMs
            this.singEndMs = roundInfo.singEndMs
            for (m in roundInfo.mLightInfos) {
                addLightOffUid(notify, m, grabRoundInfoModel)
            }
            for (m in roundInfo.getbLightInfos()) {
                addLightBurstUid(notify, m, grabRoundInfoModel)
            }

            if (roundInfo.overReason > 0) {
                this.overReason = roundInfo.overReason
            }
            if (roundInfo.resultType > 0) {
                this.resultType = roundInfo.resultType
            }
            if (roundInfo.score > 0) {
                this.score = roundInfo.score
            }
            this.isWin = roundInfo.isWin
        }
    }

    fun tryUpdateRoundInfoModel(roundInfo: SPkRoundInfoModel?) {
        if (roundInfo == null) {
            MyLog.d(TAG, "tryUpdateRoundInfoModel pkRoundInfoModel=$roundInfo")
            return
        }
        if (userID == 0) {
            userID = roundInfo.userID
        }
        if (roundInfo.userID == userID) {
            this.singBeginMs = roundInfo.singBeginMs
            this.singEndMs = roundInfo.singEndMs

            if (roundInfo.overReason > 0) {
                this.overReason = roundInfo.overReason
            }
            if (roundInfo.resultType > 0) {
                this.resultType = roundInfo.resultType
            }
            if (roundInfo.score > 0) {
                this.score = roundInfo.score
            }
            this.isWin = roundInfo.isWin
        }
    }

    fun addLightOffUid(notify: Boolean, noPassingInfo: MLightInfoModel, roundInfoModel: GrabRoundInfoModel): Boolean {
        if (!mLightInfos.contains(noPassingInfo)) {
            mLightInfos.add(noPassingInfo)
            if (notify) {
                val event = GrabSomeOneLightOffEvent(noPassingInfo.getUserID(), roundInfoModel)
                EventBus.getDefault().post(event)
            }
            return true
        }
        return false
    }

    fun addLightBurstUid(notify: Boolean, bLightInfoModel: BLightInfoModel, roundInfoModel: GrabRoundInfoModel): Boolean {
        if (!bLightInfos.contains(bLightInfoModel)) {
            bLightInfos.add(bLightInfoModel)
            if (notify) {
                val event = GrabSomeOneLightBurstEvent(bLightInfoModel.getUserID(), roundInfoModel)
                EventBus.getDefault().post(event)
            }
            return true
        }
        return false
    }

    override fun toString(): String {
        return "SPkRoundInfoModel{" +
                "userID=" + userID +
                ", singBeginMs=" + singBeginMs +
                ", singEndMs=" + singEndMs +
                ", bLightInfos=" + bLightInfos +
                ", mLightInfos=" + mLightInfos +
                ", overReason=" + overReason +
                ", resultType=" + resultType +
                ", score=" + score +
                '}'.toString()
    }

    companion object {

        fun parse(roundInfo: MSPKInnerRoundInfo): SPkRoundInfoModel {
            val roundInfoModel = SPkRoundInfoModel()
            roundInfoModel.userID = roundInfo.userID

            roundInfoModel.singBeginMs = roundInfo.singBeginMs
            roundInfoModel.singEndMs = roundInfo.singEndMs

            roundInfoModel.overReason = roundInfo.overReason.value
            //        roundInfoModel.setResultType(roundInfo.getResultType().getValue());

            roundInfoModel.score = roundInfo.spkFinalScore
            roundInfoModel.isWin = roundInfo.isWin
            return roundInfoModel
        }

        fun parse(roundInfo: QSPKInnerRoundInfo): SPkRoundInfoModel {
            val roundInfoModel = SPkRoundInfoModel()
            roundInfoModel.userID = roundInfo.userID

            roundInfoModel.singBeginMs = roundInfo.singBeginMs
            roundInfoModel.singEndMs = roundInfo.singEndMs

            roundInfoModel.overReason = roundInfo.overReason.value
            roundInfoModel.resultType = roundInfo.resultType.value

            for (m in roundInfo.bLightInfosList) {
                roundInfoModel.getbLightInfos().add(BLightInfoModel.parse(m))
            }

            for (m in roundInfo.mLightInfosList) {
                roundInfoModel.mLightInfos.add(MLightInfoModel.parse(m))
            }
            roundInfoModel.score = roundInfo.spkFinalScore
            roundInfoModel.isWin = roundInfo.isWin
            return roundInfoModel
        }
    }
}
