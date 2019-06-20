package com.module.home.ranked.model

import java.io.Serializable

class RankTagModel : Serializable {
    /**
     * rankID : 0
     * title : string
     */

    var rankID: Int = 0
    var title: String? = null

    override fun toString(): String {
        return "RankTagModel(rankID=$rankID, title=$title)"
    }
}
