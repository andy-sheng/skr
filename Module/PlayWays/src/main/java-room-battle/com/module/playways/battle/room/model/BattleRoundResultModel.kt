package com.module.playways.battle.room.model

import com.zq.live.proto.BattleRoom.BRoundResult
import com.zq.live.proto.BattleRoom.EChallengeResult
import com.zq.live.proto.BattleRoom.EChallengeTip
import java.io.Serializable

class BattleRoundResultModel : Serializable {
    var singScore = 0f
    var gameScore = 0 // 本局加的分
    var challengeResult = EChallengeResult.ECR_FAILED.value
    var challengeTip = EChallengeTip.ECT_HEN_YI_HAN.value
    var teamScore = ArrayList<BTeamScore>()

    companion object {
        fun parseFromPb(pb: BRoundResult): BattleRoundResultModel {
            var info = BattleRoundResultModel()
            info.singScore = pb.singScore
            info.gameScore = pb.gameScore
            info.challengeResult = pb.challengeResult.value
            info.challengeTip = pb.challengeTip.value
            pb.teamScoreList.forEach {
                var teamScore = BTeamScore()
                teamScore.teamScore = it.teamScore
                teamScore.teamTag = it.teamTag
                info.teamScore.add(teamScore)
            }
            return info
        }
    }
}

class BTeamScore {
    var teamTag = ""
    var teamScore = 0
}
