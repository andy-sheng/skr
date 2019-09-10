package com.common.core.userinfo.model

import com.zq.live.proto.Common.UserRanking
import java.io.Serializable

// 状态信息
class ScoreStateModel : Serializable {

    var userID: Int = 0             // 用户id
    var seq: Int = 0                // 分值状态的时间顺序, 数字越大越晚
    var mainRanking: Int = 0        // 主段位数值
    var subRanking: Int = 0         // 子段位数值
    var currStar: Int = 0           // 子段位当前星星数
    var maxStar: Int = 0            // 子段位星星数上限
    var protectBattleIndex: Int = 0 // 掉段保护所需战力分值
    var currBattleIndex: Int = 0    // 当前战力分值
    var maxBattleIndex: Int = 0     // 战力分值上限
    var totalScore: Int = 0         // 用在段位排行榜中的总分值
    var currExp: Int = 0            // 子段位当前经验值
    var maxExp: Int = 0             // 子段位经验值上限
    var rankingDesc: String? = null     // 描述段位

    override fun toString(): String {
        return "ScoreStateModel{" +
                "userID=" + userID +
                ", seq=" + seq +
                ", mainRanking=" + mainRanking +
                ", subRanking=" + subRanking +
                ", currStar=" + currStar +
                ", maxStar=" + maxStar +
                ", protectBattleIndex=" + protectBattleIndex +
                ", currBattleIndex=" + currBattleIndex +
                ", maxBattleIndex=" + maxBattleIndex +
                ", totalScore=" + totalScore +
                ", currExp=" + currExp +
                ", maxExp=" + maxExp +
                ", rankingDesc='" + rankingDesc + '\''.toString() +
                '}'.toString()
    }

    companion object {
        fun parseFromPB(userId: Int, model: UserRanking): ScoreStateModel {
            val scoreStateModel = ScoreStateModel()
            scoreStateModel.userID = userId
            scoreStateModel.mainRanking = model.mainRanking
            scoreStateModel.subRanking = model.subRanking
            scoreStateModel.currExp = model.currExp.toInt()
            scoreStateModel.maxExp = model.maxExp.toInt()
            scoreStateModel.rankingDesc = model.rankingDesc
            return scoreStateModel
        }
    }
}
