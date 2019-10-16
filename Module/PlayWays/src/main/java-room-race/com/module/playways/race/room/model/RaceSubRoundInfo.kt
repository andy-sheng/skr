package com.module.playways.race.room.model

import com.zq.live.proto.RaceRoom.ERWantSingType
import com.zq.live.proto.RaceRoom.ESubRoundOverReason
import com.zq.live.proto.RaceRoom.SubRoundInfo
import java.io.Serializable

class RaceSubRoundInfo : Serializable {
    var beginMs = 0
    var endMs = 0
    //var choiceID = 0
    var overReason = ESubRoundOverReason.ESROR_UNKNOWN.value
    var subRoundSeq = 0
    var userID = 0
    var wantSingType = ERWantSingType.ERWST_DEFAULT.value
    var choiceDetail: RaceGamePlayInfo? = null

    override fun toString(): String {
        return "RaceSubRoundInfo(beginMs=$beginMs, endMs=$endMs, overReason=$overReason, subRoundSeq=$subRoundSeq, userID=$userID, wantSingType=$wantSingType choiceDetail=$choiceDetail)"
    }

    fun tryUpdateInfoModel(model: RaceSubRoundInfo?) {
        model?.let {
            if (overReason == 0) {
                overReason = it.overReason
            }
            beginMs = it.beginMs
            endMs = it.endMs
        }
    }

}

internal fun parseFromSubRoundInfoPB(pb: SubRoundInfo): RaceSubRoundInfo {
    val model = RaceSubRoundInfo()
    model.userID = pb.userID
    model.subRoundSeq = pb.subRoundSeq
    //model.choiceID = pb.choiceID
    model.beginMs = pb.beginMs
    model.endMs = pb.endMs
    model.overReason = pb.overReason.value
    model.wantSingType = pb.wantSingType.value
    model.choiceDetail = parseFromGameInfoPB(pb.choiceDetail)
    return model
}