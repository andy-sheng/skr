package com.module.playways.friendroom


import com.common.core.userinfo.model.UserInfoModel
import com.component.busilib.friends.SpecialModel
import com.component.busilib.friends.VoiceInfoModel

import java.io.Serializable

class GrabRecommendModel : Serializable {

    /**
     * roomInfo : {"inPlayersNum":0,"isOwner":true,"roomID":0,"roomTag":"URT_UNKNOWN","roomType":"RT_UNKNOWN","tagID":0,"totalPlayersNum":0,"userID":0}
     * tagInfo : {"bgColor":"string","introduction":"string","tagID":0,"tagName":"string"}
     * userInfo : {"avatar":"string","nickname":"string","sex":"unknown","userID":0}
     * category : 1
     */

    var roomInfo: GrabSimpleRoomInfo? = null
    var tagInfo: SpecialModel? = null
    var userInfo: UserInfoModel? = null
    var category: Int = 0
    var voiceInfo: VoiceInfoModel? = null

    override fun toString(): String {
        return "RecommendModel{" +
                "roomInfo=" + roomInfo +
                ", tagInfo=" + tagInfo +
                ", userInfo=" + userInfo +
                ", category=" + category +
                ", voiceInfo=" + voiceInfo +
                '}'.toString()
    }

    companion object {
        val TYPE_FRIEND = 1  //好友房
        val TYPE_RECOMMEND = 2  //机器推荐房间
        val TYPE_FOLLOW = 3  //关注房间
        val TYPE_OP_RECOMMEND = 4 //运营推荐房间
        val TYPE_ROOM_CITY = 5 //同城推荐房间
    }
}
