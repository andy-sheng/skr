package com.module.playways.party.room.model

import com.common.core.userinfo.model.UserInfoModel
import com.module.playways.room.prepare.model.PlayerInfoModel
import com.zq.live.proto.PartyRoom.EPUserRole
import com.zq.live.proto.PartyRoom.POnlineInfo
import com.zq.live.proto.PartyRoom.SeatInfo

class PartyPlayerInfoModel : PlayerInfoModel() {
    //    enum EPUserRole {
//        EPUR_UNKNOWN  = 0; //未知角色
//        EPUR_HOST     = 1; //主持人
//        EPUR_ADMIN    = 2; //管理员
//        EPUR_GUEST    = 3; //嘉宾
//        EPUR_AUDIENCE = 4; //观众
//    }
    var role = ArrayList<Int>() // 角色
    var popularity = 0 // 人气

    /**
     * 是否是指定的某些角色
     */
    fun isRole(vararg roles: Int): Boolean {
        for (r in role) {
            for (r2 in roles) {
                if (r == r2) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 是不是管理员
     */
    fun isAdmin(): Boolean {
        for (r in role) {
            if (r == EPUserRole.EPUR_ADMIN.value) {
                return true
            }
        }
        return false
    }

    /**
     * 是不是主持人
     */
    fun isHost(): Boolean {
        for (r in role) {
            if (r == EPUserRole.EPUR_HOST.value) {
                return true
            }
        }
        return false
    }

    /**
     * 是不是嘉宾
     */
    fun isGuest(): Boolean {
        for (r in role) {
            if (r == EPUserRole.EPUR_GUEST.value) {
                return true
            }
        }
        return false
    }

    override fun toString(): String {
        return "PartyPlayerInfoModel(userInfo=${userInfo.toSimpleString()}, role=$role)"
    }

    companion object{
        fun parseFromPb(pb: POnlineInfo):PartyPlayerInfoModel{
            var info = PartyPlayerInfoModel()
            info.popularity = pb.popularity
            for(r in pb.roleList){
                info.role.add(r.value)
            }
            info.userInfo = UserInfoModel.parseFromPB(pb.userInfo)
            return info
        }

        fun parseFromPb(pbs:List<POnlineInfo>):ArrayList<PartyPlayerInfoModel>{
            var infos = ArrayList<PartyPlayerInfoModel>()
            for(pb in pbs){
                infos.add(parseFromPb(pb))
            }
            return infos
        }
    }
}
