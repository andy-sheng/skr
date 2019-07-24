package com.module.playways.songmanager.model

import android.text.TextUtils

import com.component.live.proto.Common.StandPlayType

import java.io.Serializable

open class GrabRoomSongModel : Serializable {

    /**
     * itemName : 说爱你
     * owner : 沈以诚
     * roundSeq : 19
     * itemID : 4008
     * playType : 3
     * challengeAvailable : false
     */

    var itemName: String = ""
    var owner: String = ""
    var roundSeq: Int = 0
    var itemID: Int = 0
    var playType: Int = 0
    var isChallengeAvailable: Boolean = false
    var uniqTag: String = ""
    var isCouldDelete: Boolean = false
    var writer: String = ""    //作词人
    var composer: String = ""  //作曲人
    var uploaderName: String = "" //上传用户名

    val displaySongName: String
        get() {
            if (playType == StandPlayType.PT_SPK_TYPE.value) {
                if (!TextUtils.isEmpty(itemName) && itemName.contains("（PK版）")) {
                    return itemName.substring(0, itemName.length - 5)
                }
            } else if (playType == StandPlayType.PT_CHO_TYPE.value) {
                if (!TextUtils.isEmpty(itemName) && itemName.contains("（合唱版）")) {
                    return itemName.substring(0, itemName.length - 5)
                }
            }
            return itemName
        }

    val songDesc: String
        get() {
            var desc = ""
            if (!TextUtils.isEmpty(writer)) {
                desc = "词/$writer"
            }
            if (!TextUtils.isEmpty(desc)) {
                if (!TextUtils.isEmpty(composer)) {
                    desc = "$desc 曲/$composer"
                }
            } else {
                if (!TextUtils.isEmpty(composer)) {
                    desc = "曲/$composer"
                }
            }
            return desc
        }

    override fun toString(): String {
        return "GrabRoomSongModel{" +
                "itemName='" + itemName + '\''.toString() +
                ", owner='" + owner + '\''.toString() +
                ", roundSeq=" + roundSeq +
                ", itemID=" + itemID +
                ", playType=" + playType +
                ", challengeAvailable=" + isChallengeAvailable +
                '}'.toString()
    }
}
