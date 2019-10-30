package com.module.home.game.model

import com.alibaba.fastjson.annotation.JSONField
import com.component.busilib.friends.SpecialModel
import java.io.Serializable

class GrabSpecialModel : Serializable {
    companion object {
        const val TBT_SPECIAL = 1   // 专场
        const val TBT_STANDCREATE = 2 // 创建(新版)
        const val TBT_PLAYBOOK = 3   // 歌单站
        const val TBT_RACE = 4  // 擂台赛
        const val TBT_DOUBLECHAT = 5  // 双人唱聊
        const val TBT_RACE_TAB = 6  // 跳排位赛tab(新版)
        const val TBT_GODOUBLE_TAB = 7  // 跳双人唱聊tab(新版)
        const val TBT_GOALBUM_TAB = 8  // 跳新版抢唱tab(新版)
        const val TBT_MICROOM_TAB = 9 // 跳新版小K房等入口
    }
    @JSONField(name = "bType")
    var type: Int? = null
    @JSONField(name = "block")
    var model: SpecialModel? = null
}
