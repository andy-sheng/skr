package com.module.home.ranked.model

import java.io.Serializable

class RankHomeCardModel : Serializable {

    companion object {

        val POPULAR_RANK_TYPE = 1 // 人气榜
        val DUAN_RANK_TYPE = 2  // 段位榜
        val REWARD_RANK_TYPE = 3 // 打赏榜
    }

    var rankType: Int = 0
    var desc: String? = null
    var tabs: List<RankTagModel>? = null

    override fun toString(): String {
        return "RankHomeCardModel(rankType=$rankType, desc=$desc, tabs=$tabs)"
    }

}
