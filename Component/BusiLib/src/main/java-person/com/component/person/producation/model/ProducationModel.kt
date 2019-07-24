package com.component.person.producation.model

import java.io.Serializable

class ProducationModel : Serializable {


    /**
     * userID : 2449970
     * worksID : 40000080
     * songID : 2624
     * worksURL : http://res-static.inframe.mobi/audios/2449970/5203acecc455a782.aac
     * playCnt : 0
     * name : 再见你好
     * artist : 黄飞鸿
     * cover : http://res-static.inframe.mobi/image/default_m_v2.png
     * category : 2
     * duration : 19456
     * nickName : 哈哈程序
     */

    var userID: Int = 0
    var worksID: Int = 0
    var songID: Int = 0
    var worksURL: String? = null
    var playCnt: Int = 0
    var name: String? = null
    var artist: String? = null
    var cover: String? = null
    var category: Int = 0
    var duration: Int = 0
    var nickName: String? = null

    companion object {
        const val TYPE_STAND_NORMAL = 1    // 一唱到底
        const val TYPE_STAND_HIGHLIGHT = 2 // 一唱到底高光时刻
        const val TYPE_PRACTICE = 3        // 练歌房
        const val TYPE_TEAM = 4            // 团队赛
    }
}
