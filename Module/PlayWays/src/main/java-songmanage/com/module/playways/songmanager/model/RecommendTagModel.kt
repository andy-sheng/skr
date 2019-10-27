package com.module.playways.songmanager.model

import java.io.Serializable

class RecommendTagModel : Serializable {

    // todo 测试先改一下，让服务器改回来
    /**
     * type : 123
     * name : 热门排行1
     */
    var type: Int = 0
    var name: String? = null

    // todo 排麦房临时这么用
    var tabDesc: String? = null
    set(value) {
        name = value
    }
    var tabType: Int = 0
    set(value) {
        type = value
    }
}
