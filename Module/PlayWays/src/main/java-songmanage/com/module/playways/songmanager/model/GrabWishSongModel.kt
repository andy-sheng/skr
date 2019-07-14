package com.module.playways.songmanager.model

import com.common.core.userinfo.model.UserInfoModel

import java.io.Serializable

class GrabWishSongModel : GrabRoomSongModel(), Serializable {

    private var pID: String? = null

    var suggester: UserInfoModel? = null

    fun getpID(): String? {
        return pID
    }

    fun setpID(pID: String) {
        this.pID = pID
    }

    override fun toString(): String {
        return "GrabRoomSongModel{" +
                "itemName='" + itemName + '\''.toString() +
                ", owner='" + owner + '\''.toString() +
                ", roundSeq=" + roundSeq +
                ", itemID=" + itemID +
                ", playType=" + playType +
                ", challengeAvailable=" + isChallengeAvailable +
                ", pID=" + pID +
                ", suggester=" + suggester +
                '}'.toString()
    }
}
