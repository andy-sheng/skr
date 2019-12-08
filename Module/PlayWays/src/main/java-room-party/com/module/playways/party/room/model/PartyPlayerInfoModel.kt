package com.module.playways.party.room.model

import com.module.playways.room.prepare.model.PlayerInfoModel
import com.zq.live.proto.PartyRoom.EPUserRole

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
     * 是不是管理员
     */
    fun isAdmin():Boolean{
        for(r in role){
            if(r == EPUserRole.EPUR_ADMIN.value){
                return true
            }
        }
        return false
    }

    /**
     * 是不是主持人
     */
    fun isHost():Boolean{
        for(r in role){
            if(r == EPUserRole.EPUR_HOST.value){
                return true
            }
        }
        return false
    }

    /**
     * 是不是嘉宾
     */
    fun isGuest():Boolean{
        for(r in role){
            if(r == EPUserRole.EPUR_GUEST.value){
                return true
            }
        }
        return false
    }

    override fun toString(): String {
        return "PartyPlayerInfoModel(userInfo=${userInfo.toSimpleString()}, role=$role)"
    }


}
