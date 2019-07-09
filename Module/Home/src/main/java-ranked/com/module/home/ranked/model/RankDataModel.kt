package com.module.home.ranked.model

import java.io.Serializable

class RankDataModel : Serializable {

    companion object {
        const val USER_RANKING: Int = 1  //用户的段位
        const val BLUE_ZUAN: Int = 2     //蓝钻
        const val MEILI: Int = 3         //魅力值
    }

    /**
     * avatar : string
     * levelDesc : string
     * mainDesc : string
     * mainRanking : 0
     * maxStar : 0
     * nickname : string
     * rankSeq : 0
     * score : string
     * sex : unknown
     * starCnt : 0
     * subRanking : 0
     * userID : 0
     * vType : RUT_InvalidType
     */

    var userID: Int = 0
    var nickname: String? = null
    var avatar: String? = null
    var sex: String? = null
    var vType: Int = 0
    var rankSeq: Int = 0
    var score: String? = null
    var levelDesc: String? = null
    var mainDesc: String? = null
    var mainRanking: Int = 0
    var maxStar: Int = 0
    var starCnt: Int = 0
    var subRanking: Int = 0


    override fun toString(): String {
        return "RankDataModel(userID=$userID, nickname=$nickname, avatar=$avatar, sex=$sex, vType=$vType, rankSeq=$rankSeq, score=$score, levelDesc=$levelDesc, mainDesc=$mainDesc, mainRanking=$mainRanking, maxStar=$maxStar, starCnt=$starCnt, subRanking=$subRanking)"
    }
}
