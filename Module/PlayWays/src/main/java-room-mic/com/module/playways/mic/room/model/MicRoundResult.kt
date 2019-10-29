package com.module.playways.mic.room.model

import com.zq.live.proto.MicRoom.MCommonRoundResult
import com.zq.live.proto.MicRoom.MScoreTipType
import java.io.Serializable


class MicRoundResult : Serializable {
    var finalScore = 1f //最终得分分值
    var finalTip = MScoreTipType.MST_UNKNOWN.value
    var finalMsg = ""

    companion object {
        internal fun parseFromInfoPB(pb: MCommonRoundResult): MicRoundResult {
            val model = MicRoundResult()
            model.finalMsg = pb.finalMsg
            model.finalScore = pb.finalScore
            model.finalTip = pb.finalTip.value
            return model
        }

    }
}

