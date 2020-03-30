package com.module.club.work

import java.io.Serializable

class WorkModel : Serializable {

    var userID: Int = 0
    var worksID: Int = 0
    var worksURL: String? = null

    var duration: Int = 0
    var nickName: String? = null

    var songName: String? = null
    var familyID: Int = 0
    var avatar: String? = null
    var artist: String? = null
    var auditing: Boolean = false

    companion object {
        const val TYPE_STAND_NORMAL = 1    // 一唱到底
        const val TYPE_STAND_HIGHLIGHT = 2 // 一唱到底高光时刻
        const val TYPE_PRACTICE = 3        // 练歌房
        const val TYPE_TEAM = 4            // 团队赛
    }
}
