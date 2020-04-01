package com.module.playways.party.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import com.zq.live.proto.Common.POnlineInfo
import java.io.Serializable

class POnlineInfoModel : Serializable{
    @JSONField(name = "popularity")
    var popularity: Int? = null
    @JSONField(name = "role")
    var role: List<Int?>? = null
    @JSONField(name = "userInfo")
    var userInfo: UserInfoModel? = null
    companion object{

        fun parseFromPB(pOnlineInfo: POnlineInfo):POnlineInfoModel{
            val pOnlineInfoModel = POnlineInfoModel()
            pOnlineInfoModel.userInfo = UserInfoModel.parseFromPB(pOnlineInfo.userInfo)
            pOnlineInfoModel.popularity = pOnlineInfo.popularity
            pOnlineInfoModel.role = pOnlineInfo.roleList.map {
                it.value
            }
            return pOnlineInfoModel
        }
    }

    override fun toString(): String {
        return "POnlineInfoModel(popularity=$popularity, role=$role, userInfo=$userInfo)"
    }


}