package com.module.playways.friendroom

import java.io.Serializable

/**
 * 房间简易数据
 */
class GrabSimpleRoomInfo : Serializable {
    /**
     * roomID : 20369723
     * inPlayersNum : 1
     * totalPlayersNum : 0
     * roomName : 视频专场
     * roomTagURL : http://res-static.inframe.mobi/recommend/friend.png
     * mediaType : 2
     * mediaTagURL : http://res-static.inframe.mobi/recommend/vedio-room.png
     * roomType : 2
     * tagID : 12
     */

    var roomID: Int = 0
    var inPlayersNum: Int = 0
    var totalPlayersNum: Int = 0
    var roomName: String? = null
    var roomTagURL: String? = null
    var mediaType: Int = 0
    var mediaTagURL: String? = null
    var roomType: Int = 0
    var tagID: Int = 0

    override fun toString(): String {
        return "SimpleRoomInfo{" +
                "roomID=" + roomID +
                ", inPlayersNum=" + inPlayersNum +
                ", totalPlayersNum=" + totalPlayersNum +
                ", roomName='" + roomName + '\''.toString() +
                ", roomTagURL='" + roomTagURL + '\''.toString() +
                ", mediaType=" + mediaType +
                ", mediaTagURL='" + mediaTagURL + '\''.toString() +
                ", roomType=" + roomType +
                ", tagID=" + tagID +
                '}'.toString()
    }
}
