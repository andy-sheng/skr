package com.component.person.model

import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel

class RelationModel : Serializable {
    @JSONField(name = "relationInfo")
    var relationInfo: RelationInfo? = null
    @JSONField(name = "userInfo")
    var user: UserInfoModel? = null
}

class RelationInfo : Serializable {
    companion object {
        // relationType 的值
        const val DJK_CP = 1   // 关系 CP
        const val DJK_Ji_You = 2  // 关系基友
        const val DJK_Bai_Shi = 3  // 关系 拜师
        const val DJK_Shou_Tu = 4   // 关系 收徒
        const val DJK_Gui_Mi = 5  // 关系 闺蜜

        // displayType 的值
        const val GDT_SHENG_WEN = 1   // 声纹
        const val GDT_TOU_XIANG_KUANG = 2  // 头像框
        const val GDT_GUARD = 3      // 守护
        const val GDT_YAN_CHANG = 4  // 演唱
        const val GDT_BAO_DENG = 5   // 爆灯
        const val GDT_LI_BAO = 6     // 礼包
        const val GDT_DAO_JU_KA = 7  // 道具卡
    }

    @JSONField(name = "beginTime")
    var beginTime: Long = 0
    @JSONField(name = "displayType")
    var displayType: Int = 0
    @JSONField(name = "expireTime")
    var expireTime: Long = 0
    @JSONField(name = "relationType")
    var relationType: Int = 0
    @JSONField(name = "userID")
    var userID: Int = 0
}