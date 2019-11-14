package com.module.playways.race.room.model

import com.zq.live.proto.RaceRoom.FakeUserInfo
import java.io.Serializable

class FakeUserInfoModel : Serializable {

    var nickName: String? = null
    var avatarUrl: String? = null


    companion object {
        fun parseFromPB(model: FakeUserInfo): FakeUserInfoModel {
            val userInfoModel = FakeUserInfoModel()
            if (model != null) {
                userInfoModel.nickName = model.nickName
            }
            return userInfoModel
        }
        
        const val maleAvatar = "http://res-static.inframe.mobi/image/fake_male%403x.png"
        const val femaleAvatarUrl = "http://res-static.inframe.mobi/image/fake_femal%403x.png"
    }

    override fun toString(): String {
        return "FakeUserInfoModel(nickName=$nickName)"
    }

}